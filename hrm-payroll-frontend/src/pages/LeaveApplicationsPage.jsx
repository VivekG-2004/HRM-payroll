import { useEffect, useState } from 'react';
import StatusBadge from '../components/StatusBadge';
import EmployeeSelect from '../components/EmployeeSelect';
import {
  getPendingLeaveApplications,
  getEmployeeLeaveApplications,
  approveLeave,
  rejectLeave,
} from '../api/leaveApplications';

export default function LeaveApplicationsPage() {
  const [tab, setTab] = useState('pending');

  return (
    <div>
      <div className="page-header">
        <h2>Leave Applications</h2>
      </div>

      <div className="tab-bar">
        <button
          className={`tab-btn${tab === 'pending' ? ' active' : ''}`}
          onClick={() => setTab('pending')}
        >
          Pending Approvals
        </button>
        <button
          className={`tab-btn${tab === 'employee' ? ' active' : ''}`}
          onClick={() => setTab('employee')}
        >
          By Employee
        </button>
      </div>

      {tab === 'pending' ? <PendingTab /> : <ByEmployeeTab />}
    </div>
  );
}

function PendingTab() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actingId, setActingId] = useState(null);

  function load() {
    setLoading(true);
    getPendingLeaveApplications()
      .then(setApplications)
      .catch(() => setError('Could not load pending applications.'))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function handleApprove(app) {
    setActingId(app.id);
    try {
      await approveLeave(app.id);
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Could not approve this application.');
    } finally {
      setActingId(null);
    }
  }

  async function handleReject(app) {
    if (!confirm(`Reject leave application for ${app.employeeName}?`)) return;
    setActingId(app.id);
    try {
      await rejectLeave(app.id);
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Could not reject this application.');
    } finally {
      setActingId(null);
    }
  }

  if (loading) return <p style={{ color: 'var(--text-muted)' }}>Loading…</p>;
  if (error) return <div className="form-error">{error}</div>;

  return (
    <div className="card">
      <table>
        <thead>
          <tr className="table-header">
            <th style={cellStyle}>Employee</th>
            <th style={cellStyle}>Leave Type</th>
            <th style={cellStyle}>From</th>
            <th style={cellStyle}>To</th>
            <th style={cellStyle}>Days</th>
            <th style={cellStyle}>Applied On</th>
            <th style={cellStyle}></th>
          </tr>
        </thead>
        <tbody>
          {applications.map((app) => (
            <tr key={app.id}>
              <td style={cellStyle}>{app.employeeName}</td>
              <td style={cellStyle}>{app.leaveTypeCode}</td>
              <td style={cellStyle} className="mono">{app.fromDate}</td>
              <td style={cellStyle} className="mono">{app.toDate}</td>
              <td style={cellStyle} className="mono">{app.daysCount}</td>
              <td style={cellStyle} className="mono">{app.appliedOn}</td>
              <td style={{ ...cellStyle, textAlign: 'right' }}>
                <button
                  className="btn-outline"
                  disabled={actingId === app.id}
                  onClick={() => handleApprove(app)}
                >
                  Approve
                </button>
                <button
                  className="btn-outline"
                  style={{ marginLeft: 8, borderColor: 'var(--danger)', color: 'var(--danger)' }}
                  disabled={actingId === app.id}
                  onClick={() => handleReject(app)}
                >
                  Reject
                </button>
              </td>
            </tr>
          ))}
          {applications.length === 0 && (
            <tr>
              <td style={cellStyle} colSpan={7}>No pending applications.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function ByEmployeeTab() {
  const [selectedEmployeeId, setSelectedEmployeeId] = useState('');
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!selectedEmployeeId) {
      setApplications([]);
      return;
    }
    setLoading(true);
    setError('');
    getEmployeeLeaveApplications(selectedEmployeeId)
      .then(setApplications)
      .catch(() => setError('Could not load leave history.'))
      .finally(() => setLoading(false));
  }, [selectedEmployeeId]);

  return (
    <div>
      <div style={{ maxWidth: 320, marginBottom: 24 }}>
        <EmployeeSelect value={selectedEmployeeId} onChange={setSelectedEmployeeId} />
      </div>

      {!selectedEmployeeId && (
        <p style={{ color: 'var(--text-muted)' }}>Select an employee to view their leave history.</p>
      )}

      {loading && <p style={{ color: 'var(--text-muted)' }}>Loading…</p>}
      {error && <div className="form-error">{error}</div>}

      {selectedEmployeeId && !loading && !error && (
        <div className="card">
          <table>
            <thead>
              <tr className="table-header">
                <th style={cellStyle}>Leave Type</th>
                <th style={cellStyle}>From</th>
                <th style={cellStyle}>To</th>
                <th style={cellStyle}>Days</th>
                <th style={cellStyle}>Status</th>
                <th style={cellStyle}>Applied On</th>
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
                  <td style={cellStyle} className="mono">{app.appliedOn}</td>
                </tr>
              ))}
              {applications.length === 0 && (
                <tr>
                  <td style={cellStyle} colSpan={6}>No leave applications for this employee.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

const cellStyle = { padding: '10px 16px', textAlign: 'left', fontSize: 14 };