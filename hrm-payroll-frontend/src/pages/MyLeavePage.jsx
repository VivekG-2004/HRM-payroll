import { useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import StatusBadge from '../components/StatusBadge';
import { getLeaveBalances } from '../api/leaveBalances';
import { applyLeave, getEmployeeLeaveApplications } from '../api/leaveApplications';

const leaveTypes = ['CL', 'SL', 'EL'];

export default function MyLeavePage() {
  const { user } = useAuth();
  const [balances, setBalances] = useState([]);
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [form, setForm] = useState({ leaveTypeCode: 'CL', fromDate: '', toDate: '' });
  const [formError, setFormError] = useState('');
  const [formSuccess, setFormSuccess] = useState('');
  const [saving, setSaving] = useState(false);

  function load() {
    setLoading(true);
    setError('');
    Promise.all([
      getLeaveBalances(user.employeeId),
      getEmployeeLeaveApplications(user.employeeId),
    ])
      .then(([b, a]) => {
        setBalances(b);
        setApplications(a);
      })
      .catch(() => setError('Could not load leave data.'))
      .finally(() => setLoading(false));
  }

  useEffect(load, [user.employeeId]);

  async function handleSubmit(e) {
    e.preventDefault();
    setFormError('');
    setFormSuccess('');

    if (!form.fromDate || !form.toDate) {
      setFormError('Please select both from and to dates.');
      return;
    }
    if (form.toDate < form.fromDate) {
      setFormError('To date cannot be before from date.');
      return;
    }

    setSaving(true);
    try {
      await applyLeave({
        employeeId: user.employeeId,
        leaveTypeCode: form.leaveTypeCode,
        fromDate: form.fromDate,
        toDate: form.toDate,
      });
      setForm({ leaveTypeCode: 'CL', fromDate: '', toDate: '' });
      setFormSuccess('Leave application submitted.');
      load();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Could not submit leave application.');
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <p style={{ color: 'var(--text-muted)' }}>Loading…</p>;
  if (error) return <div className="form-error">{error}</div>;

  return (
    <div>
      <div className="page-header">
        <h2>My Leave</h2>
      </div>

      <div className="stat-grid" style={{ marginBottom: 32 }}>
        {balances.map((b) => (
          <div className="stat-card" key={b.leaveTypeCode}>
            <span className="stat-label">{b.leaveTypeName} ({b.leaveTypeCode})</span>
            <span className="stat-value">{b.remaining}</span>
            <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
              {b.used} used of {b.allocated}
            </span>
          </div>
        ))}
      </div>

      <div className="split-layout">
        <div>
          <h3 style={{ marginBottom: 12 }}>Apply for Leave</h3>
          <div className="card" style={{ padding: 24 }}>
            <form onSubmit={handleSubmit}>
              {formError && <div className="form-error">{formError}</div>}
              {formSuccess && <div className="form-success">{formSuccess}</div>}

              <label className="field">
                <span>Leave Type</span>
                <select
                  value={form.leaveTypeCode}
                  onChange={(e) => setForm({ ...form, leaveTypeCode: e.target.value })}
                >
                  {leaveTypes.map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
              </label>

              <div className="form-grid">
                <label className="field">
                  <span>From</span>
                  <input
                    type="date"
                    value={form.fromDate}
                    onChange={(e) => setForm({ ...form, fromDate: e.target.value })}
                  />
                </label>
                <label className="field">
                  <span>To</span>
                  <input
                    type="date"
                    value={form.toDate}
                    onChange={(e) => setForm({ ...form, toDate: e.target.value })}
                  />
                </label>
              </div>

              <button type="submit" className="btn-primary" disabled={saving}>
                {saving ? 'Submitting…' : 'Apply'}
              </button>
            </form>
          </div>
        </div>

        <div>
          <h3 style={{ marginBottom: 12 }}>My Applications</h3>
          <div className="card">
            <table>
              <thead>
                <tr className="table-header">
                  <th style={cellStyle}>Type</th>
                  <th style={cellStyle}>From</th>
                  <th style={cellStyle}>To</th>
                  <th style={cellStyle}>Days</th>
                  <th style={cellStyle}>Status</th>
                </tr>
              </thead>
              <tbody>
                {applications.map((app) => (
                  <tr key={app.id}>
                    <td style={cellStyle}>{app.leaveTypeCode}</td>
                    <td style={cellStyle} className="mono">{app.fromDate}</td>
                    <td style={cellStyle} className="mono">{app.toDate}</td>
                    <td style={cellStyle} className="mono">{app.daysCount}</td>
                    <td style={cellStyle}><StatusBadge status={app.status} /></td>
                  </tr>
                ))}
                {applications.length === 0 && (
                  <tr>
                    <td style={cellStyle} colSpan={5}>No applications yet.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

const cellStyle = { padding: '10px 16px', textAlign: 'left', fontSize: 14 };