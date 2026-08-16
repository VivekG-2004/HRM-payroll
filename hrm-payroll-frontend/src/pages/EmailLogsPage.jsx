import { useEffect, useState } from 'react';
import StatusBadge from '../components/StatusBadge';
import { getEmailLogs, getFailedEmailLogs, retryEmailLog } from '../api/emailLogs';

export default function EmailLogsPage() {
  const [tab, setTab] = useState('all');
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [retryingId, setRetryingId] = useState(null);

  function load() {
    setLoading(true);
    setError('');
    const fetcher = tab === 'failed' ? getFailedEmailLogs : getEmailLogs;
    fetcher()
      .then(setLogs)
      .catch(() => setError('Could not load email logs.'))
      .finally(() => setLoading(false));
  }

  useEffect(load, [tab]);

  async function handleRetry(log) {
    setRetryingId(log.id);
    try {
      await retryEmailLog(log.id);
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Could not retry this email.');
    } finally {
      setRetryingId(null);
    }
  }

  function canRetry(log) {
    return log.status === 'FAILED' && log.retryable && log.attemptCount < 3;
  }

  return (
    <div>
      <div className="page-header">
        <h2>Email Logs</h2>
      </div>

      <div className="tab-bar">
        <button
          className={`tab-btn${tab === 'all' ? ' active' : ''}`}
          onClick={() => setTab('all')}
        >
          All
        </button>
        <button
          className={`tab-btn${tab === 'failed' ? ' active' : ''}`}
          onClick={() => setTab('failed')}
        >
          Failed
        </button>
      </div>

      {loading && <p style={{ color: 'var(--text-muted)' }}>Loading…</p>}
      {error && <div className="form-error">{error}</div>}

      {!loading && !error && (
        <div className="card">
          <table>
            <thead>
              <tr className="table-header">
                <th style={cellStyle}>Sent To</th>
                <th style={cellStyle}>Status</th>
                <th style={cellStyle}>Error Category</th>
                <th style={cellStyle}>Attempts</th>
                <th style={cellStyle}>Last Attempt</th>
                <th style={cellStyle}></th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td style={cellStyle}>{log.sentTo}</td>
                  <td style={cellStyle}><StatusBadge status={log.status} /></td>
                  <td style={cellStyle}>
                    {log.errorCategory ? (
                      <span className="mono" style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                        {log.errorCategory}
                      </span>
                    ) : (
                      '—'
                    )}
                  </td>
                  <td style={cellStyle} className="mono">{log.attemptCount} / 3</td>
                  <td style={cellStyle} className="mono">{log.lastAttemptAt || '—'}</td>
                  <td style={{ ...cellStyle, textAlign: 'right' }}>
                    {canRetry(log) ? (
                      <button
                        className="btn-outline"
                        disabled={retryingId === log.id}
                        onClick={() => handleRetry(log)}
                      >
                        {retryingId === log.id ? 'Retrying…' : 'Retry'}
                      </button>
                    ) : log.status === 'FAILED' ? (
                      <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                        {!log.retryable ? 'Not retryable' : 'Max retries reached'}
                      </span>
                    ) : null}
                  </td>
                </tr>
              ))}
              {logs.length === 0 && (
                <tr>
                  <td style={cellStyle} colSpan={6}>No email logs found.</td>
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