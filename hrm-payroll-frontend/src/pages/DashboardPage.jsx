import { useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { getDashboardSummary } from '../api/dashboard';
import { getLeaveBalances } from '../api/leaveBalances';

const monthNames = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

export default function DashboardPage() {
  const { user } = useAuth();

  if (user.role !== 'HR') {
    return <EmployeeDashboard />;
  }

  return <HrDashboard />;
}

function EmployeeDashboard() {
  const { user } = useAuth();
  const [balances, setBalances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    getLeaveBalances(user.employeeId)
      .then((data) => {
        if (!cancelled) setBalances(data);
      })
      .catch(() => {
        if (!cancelled) setError('Could not load your leave balances.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, [user.employeeId]);

  return (
    <div>
      <h2>Welcome, {user.employeeName || user.username}</h2>
      <p style={{ color: 'var(--text-muted)', margin: '4px 0 24px' }}>
        Here's your current leave balance.
      </p>

      {loading && <p style={{ color: 'var(--text-muted)' }}>Loading…</p>}
      {error && <div className="form-error">{error}</div>}

      {!loading && !error && (
        <div className="stat-grid">
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
      )}
    </div>
  );
}

function HrDashboard() {
  const today = new Date();
  const [month, setMonth] = useState(today.getMonth() + 1);
  const [year, setYear] = useState(today.getFullYear());
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');

    getDashboardSummary(month, year)
      .then((data) => {
        if (!cancelled) setSummary(data);
      })
      .catch(() => {
        if (!cancelled) setError('Could not load dashboard data.');
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [month, year]);

  const years = [today.getFullYear(), today.getFullYear() - 1, today.getFullYear() - 2];

  return (
    <div>
      <div className="page-header">
        <h2>Payroll Overview</h2>
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
      </div>

      {loading && <p style={{ color: 'var(--text-muted)' }}>Loading…</p>}
      {error && <div className="form-error">{error}</div>}

      {summary && !loading && (
        <>
          <div className="stat-grid">
            <StatCard label="Active Employees" value={summary.totalActiveEmployees} />
            <StatCard label="Processed This Month" value={summary.employeesProcessedThisMonth} />
            <StatCard label="Pending Payroll" value={summary.pendingPayroll} tone="warning" />
            <StatCard label="Payslips Sent" value={summary.payslipsSentSuccessfully} tone="success" />
            <StatCard label="Failed Deliveries" value={summary.failedEmailDeliveries} tone="danger" />
          </div>

          <h3 style={{ marginTop: 32, marginBottom: 12 }}>Leave Balance Summary</h3>
          <div className="card">
            <table>
              <thead>
                <tr className="table-header">
                  <th style={cellStyle}>Leave Type</th>
                  <th style={cellStyle}>Allocated</th>
                  <th style={cellStyle}>Used</th>
                  <th style={cellStyle}>Remaining</th>
                </tr>
              </thead>
              <tbody>
                {summary.leaveBalanceSummary.map((row) => (
                  <tr key={row.leaveTypeCode}>
                    <td style={cellStyle}>{row.leaveTypeCode}</td>
                    <td style={cellStyle} className="mono">{row.totalAllocated}</td>
                    <td style={cellStyle} className="mono">{row.totalUsed}</td>
                    <td style={cellStyle} className="mono">{row.totalRemaining}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}

function StatCard({ label, value, tone }) {
  return (
    <div className="stat-card">
      <span className="stat-label">{label}</span>
      <span className={`stat-value${tone ? ` tone-${tone}` : ''}`}>{value}</span>
    </div>
  );
}

const cellStyle = { padding: '10px 16px', textAlign: 'left', fontSize: 14 };