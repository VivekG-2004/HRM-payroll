import { useEffect, useState } from 'react';
import Modal from '../components/Modal';
import EmployeeSelect from '../components/EmployeeSelect';
import { generatePayslip, getPayslips, downloadPayslip } from '../api/payslips';

const monthNames = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

export default function PayslipsPage() {
  const today = new Date();
  const [month, setMonth] = useState(today.getMonth() + 1);
  const [year, setYear] = useState(today.getFullYear());
  const [payslips, setPayslips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [genModalOpen, setGenModalOpen] = useState(false);
  const [genForm, setGenForm] = useState({ employeeId: '', payMonth: month, payYear: year });
  const [genError, setGenError] = useState('');
  const [generating, setGenerating] = useState(false);

  const [detailPayslip, setDetailPayslip] = useState(null);
  const [downloadingId, setDownloadingId] = useState(null);

  function load() {
    setLoading(true);
    getPayslips(month, year)
      .then(setPayslips)
      .catch(() => setError('Could not load payslips.'))
      .finally(() => setLoading(false));
  }

  useEffect(load, [month, year]);

  const years = [today.getFullYear(), today.getFullYear() - 1, today.getFullYear() - 2];

  function openGenModal() {
    setGenForm({ employeeId: '', payMonth: month, payYear: year });
    setGenError('');
    setGenModalOpen(true);
  }

  async function handleGenerate(e) {
    e.preventDefault();
    setGenError('');

    if (!genForm.employeeId) {
      setGenError('Please select an employee.');
      return;
    }

    setGenerating(true);
    try {
      await generatePayslip({
        employeeId: genForm.employeeId,
        payMonth: Number(genForm.payMonth),
        payYear: Number(genForm.payYear),
      });
      setGenModalOpen(false);
      load();
    } catch (err) {
      setGenError(err.response?.data?.message || 'Could not generate payslip — it may already exist for this month.');
    } finally {
      setGenerating(false);
    }
  }

  async function handleDownload(payslip) {
    setDownloadingId(payslip.id);
    try {
      await downloadPayslip(payslip.id, `${payslip.empCode}-${payslip.payMonth}-${payslip.payYear}.pdf`);
    } catch {
      alert('Could not download payslip.');
    } finally {
      setDownloadingId(null);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h2>Payslips</h2>
        <div style={{ display: 'flex', gap: 12 }}>
          <div className="period-select">
            <select value={month} onChange={(e) => setMonth(Number(e.target.value))}>
              {monthNames.map((name, i) => (
                <option key={name} value={i + 1}>{name}</option>
              ))}
            </select>
            <select value={year} onChange={(e) => setYear(Number(e.target.value))}>
              {years.map((y) => (
                <option key={y} value={y}>{y}</option>
              ))}
            </select>
          </div>
          <button className="btn-primary" style={{ width: 'auto' }} onClick={openGenModal}>
            + Generate Payslip
          </button>
        </div>
      </div>

      {loading && <p style={{ color: 'var(--text-muted)' }}>Loading…</p>}
      {error && <div className="form-error">{error}</div>}

      {!loading && !error && (
        <div className="card">
          <table>
            <thead>
              <tr className="table-header">
                <th style={cellStyle}>Emp Code</th>
                <th style={cellStyle}>Name</th>
                <th style={cellStyle}>Department</th>
                <th style={cellStyle}>Gross</th>
                <th style={cellStyle}>LOP Days</th>
                <th style={cellStyle}>Net Pay</th>
                <th style={cellStyle}>Generated On</th>
                <th style={cellStyle}></th>
              </tr>
            </thead>
            <tbody>
              {payslips.map((p) => (
                <tr key={p.id}>
                  <td style={cellStyle} className="mono">{p.empCode}</td>
                  <td style={cellStyle}>{p.employeeName}</td>
                  <td style={cellStyle}>{p.department}</td>
                  <td style={cellStyle} className="mono">₹{p.grossSalary}</td>
                  <td style={cellStyle} className="mono">{p.lopDays}</td>
                  <td style={cellStyle} className="mono">₹{p.netSalary}</td>
                  <td style={cellStyle} className="mono">{p.generatedOn}</td>
                  <td style={{ ...cellStyle, textAlign: 'right' }}>
                    <button className="btn-outline" onClick={() => setDetailPayslip(p)}>View</button>
                    <button
                      className="btn-outline"
                      style={{ marginLeft: 8 }}
                      disabled={downloadingId === p.id}
                      onClick={() => handleDownload(p)}
                    >
                      {downloadingId === p.id ? 'Downloading…' : 'Download'}
                    </button>
                  </td>
                </tr>
              ))}
              {payslips.length === 0 && (
                <tr>
                  <td style={cellStyle} colSpan={8}>No payslips generated for this period.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {genModalOpen && (
        <Modal title="Generate Payslip" onClose={() => setGenModalOpen(false)}>
          <form onSubmit={handleGenerate}>
            {genError && <div className="form-error">{genError}</div>}

            <EmployeeSelect
              value={genForm.employeeId}
              onChange={(id) => setGenForm({ ...genForm, employeeId: id })}
              label="Employee"
            />

            <div className="form-grid">
              <label className="field">
                <span>Pay Month</span>
                <select
                  value={genForm.payMonth}
                  onChange={(e) => setGenForm({ ...genForm, payMonth: e.target.value })}
                >
                  {monthNames.map((name, i) => (
                    <option key={name} value={i + 1}>{name}</option>
                  ))}
                </select>
              </label>
              <label className="field">
                <span>Pay Year</span>
                <select
                  value={genForm.payYear}
                  onChange={(e) => setGenForm({ ...genForm, payYear: e.target.value })}
                >
                  {years.map((y) => (
                    <option key={y} value={y}>{y}</option>
                  ))}
                </select>
              </label>
            </div>

            <p style={{ fontSize: 12, color: 'var(--text-muted)', margin: '4px 0 16px' }}>
              This will compute LOP from approved leave, generate the PDF, and email it to the employee automatically.
            </p>

            <button type="submit" className="btn-primary" disabled={generating}>
              {generating ? 'Generating…' : 'Generate & Send'}
            </button>
          </form>
        </Modal>
      )}

      {detailPayslip && (
        <Modal title="Payslip Detail" onClose={() => setDetailPayslip(null)}>
          <PayslipDetail payslip={detailPayslip} />
        </Modal>
      )}
    </div>
  );
}

export function PayslipDetail({ payslip }) {
  return (
    <div className="payslip-sheet">
      <div className="payslip-header">
        <div>
          <h3>{payslip.employeeName}</h3>
          <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>
            {payslip.empCode} · {payslip.department} · {payslip.designation}
          </span>
        </div>
        <div style={{ textAlign: 'right' }}>
          <span className="stat-label">Pay Period</span>
          <div style={{ fontWeight: 600 }}>{monthNames[payslip.payMonth - 1]} {payslip.payYear}</div>
        </div>
      </div>

      <div className="payslip-divider" />

      <div className="payslip-columns">
        <div>
          <h3 style={{ fontSize: 14, marginBottom: 10 }}>Earnings</h3>
          <PayslipRow label="Basic Salary" value={payslip.basicSalary} />
          <PayslipRow label="HRA" value={payslip.hra} />
          <PayslipRow label="Special Allowance" value={payslip.specialAllowance} />
          <PayslipRow label="Gross Salary" value={payslip.grossSalary} bold />
        </div>
        <div>
          <h3 style={{ fontSize: 14, marginBottom: 10 }}>Deductions</h3>
          <PayslipRow label="Deductions" value={payslip.deductions} />
          <PayslipRow label={`LOP (${payslip.lopDays} day${payslip.lopDays === 1 ? '' : 's'})`} value={payslip.lopAmount} />
        </div>
      </div>

      <div className="payslip-divider" />

      <div className="payslip-columns">
        <div>
          <h3 style={{ fontSize: 14, marginBottom: 10 }}>Leave Balance</h3>
          <PayslipRow label="CL" value={payslip.clBalance} isCount />
          <PayslipRow label="SL" value={payslip.slBalance} isCount />
          <PayslipRow label="EL" value={payslip.elBalance} isCount />
        </div>
        <div>
          <div className="net-pay-box">
            <span className="stat-label">Net Pay</span>
            <div className="mono" style={{ fontSize: 24, fontWeight: 600, color: 'var(--success)' }}>
              ₹{payslip.netSalary}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function PayslipRow({ label, value, bold, isCount }) {
  return (
    <div className="payslip-row">
      <span style={{ color: 'var(--text-muted)' }}>{label}</span>
      <span className="mono" style={{ fontWeight: bold ? 600 : 400 }}>
        {isCount ? value : `₹${value}`}
      </span>
    </div>
  );
}

const cellStyle = { padding: '10px 16px', textAlign: 'left', fontSize: 14 };