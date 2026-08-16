import { useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import Modal from '../components/Modal';
import { getMyPayslips, downloadMyPayslip } from '../api/payslips';
import { PayslipDetail } from './PayslipsPage';

const monthNames = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

export default function MyPayslipsPage() {
  const { user } = useAuth();
  const [payslips, setPayslips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [detailPayslip, setDetailPayslip] = useState(null);
  const [downloadingId, setDownloadingId] = useState(null);

  useEffect(() => {
    getMyPayslips(user.employeeId)
      .then(setPayslips)
      .catch(() => setError('Could not load your payslips.'))
      .finally(() => setLoading(false));
  }, [user.employeeId]);

  async function handleDownload(payslip) {
    setDownloadingId(payslip.id);
    try {
      await downloadMyPayslip(payslip.id, `${payslip.empCode}-${payslip.payMonth}-${payslip.payYear}.pdf`);
    } catch {
      alert('Could not download payslip.');
    } finally {
      setDownloadingId(null);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h2>My Payslips</h2>
      </div>

      {loading && <p style={{ color: 'var(--text-muted)' }}>Loading…</p>}
      {error && <div className="form-error">{error}</div>}

      {!loading && !error && (
        <div className="card">
          <table>
            <thead>
              <tr className="table-header">
                <th style={cellStyle}>Pay Period</th>
                <th style={cellStyle}>Gross</th>
                <th style={cellStyle}>Net Pay</th>
                <th style={cellStyle}>Generated On</th>
                <th style={cellStyle}></th>
              </tr>
            </thead>
            <tbody>
              {payslips.map((p) => (
                <tr key={p.id}>
                  <td style={cellStyle}>{monthNames[p.payMonth - 1]} {p.payYear}</td>
                  <td style={cellStyle} className="mono">₹{p.grossSalary}</td>
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
                  <td style={cellStyle} colSpan={5}>No payslips yet.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {detailPayslip && (
        <Modal title="Payslip Detail" onClose={() => setDetailPayslip(null)}>
          <PayslipDetail payslip={detailPayslip} />
        </Modal>
      )}
    </div>
  );
}

const cellStyle = { padding: '10px 16px', textAlign: 'left', fontSize: 14 };