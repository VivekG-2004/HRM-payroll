import { useEffect, useState } from 'react';
import Modal from '../components/Modal';
import EmployeeSelect from '../components/EmployeeSelect';
import { getSalaryStructures } from '../api/salaryStructures';
import {
  assignSalaryStructure,
  getCurrentAssignment,
  getAssignmentHistory,
} from '../api/salaryAssignments';

export default function SalaryAssignmentsPage() {
  const [structures, setStructures] = useState([]);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState(null);

  const [current, setCurrent] = useState(null);
  const [history, setHistory] = useState([]);
  const [loadingAssignments, setLoadingAssignments] = useState(false);
  const [error, setError] = useState('');

  const [modalOpen, setModalOpen] = useState(false);
  const [form, setForm] = useState({ salaryStructureId: '', effectiveFrom: '' });
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    getSalaryStructures().then(setStructures);
  }, []);

  function loadAssignments(employeeId) {
    if (!employeeId) {
      setCurrent(null);
      setHistory([]);
      return;
    }
    setLoadingAssignments(true);
    setError('');
    Promise.all([
      getCurrentAssignment(employeeId).catch(() => null),
      getAssignmentHistory(employeeId).catch(() => []),
    ])
      .then(([currentData, historyData]) => {
        setCurrent(currentData);
        setHistory(historyData);
      })
      .catch(() => setError('Could not load assignment data.'))
      .finally(() => setLoadingAssignments(false));
  }

  useEffect(() => {
    loadAssignments(selectedEmployeeId);
  }, [selectedEmployeeId]);

  function openAssignModal() {
    setForm({ salaryStructureId: '', effectiveFrom: '' });
    setFormError('');
    setModalOpen(true);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setFormError('');

    if (!form.salaryStructureId || !form.effectiveFrom) {
      setFormError('Please select a structure and an effective date.');
      return;
    }

    setSaving(true);
    try {
      await assignSalaryStructure({
        employeeId: selectedEmployeeId,
        salaryStructureId: form.salaryStructureId,
        effectiveFrom: form.effectiveFrom,
      });
      setModalOpen(false);
      loadAssignments(selectedEmployeeId);
    } catch (err) {
      setFormError(err.response?.data?.message || 'Could not assign salary structure.');
    } finally {
      setSaving(false);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h2>Salary Assignments</h2>
      </div>

      <div style={{ maxWidth: 320, marginBottom: 24 }}>
        <EmployeeSelect
          value={selectedEmployeeId}
          onChange={(id, emp) => {
            setSelectedEmployeeId(id);
            setSelectedEmployee(emp || null);
          }}
        />
      </div>

      {error && <div className="form-error">{error}</div>}

      {!selectedEmployeeId && (
        <p style={{ color: 'var(--text-muted)' }}>Select an employee to view or assign a salary structure.</p>
      )}

      {selectedEmployeeId && loadingAssignments && (
        <p style={{ color: 'var(--text-muted)' }}>Loading…</p>
      )}

      {selectedEmployeeId && !loadingAssignments && (
        <>
          <div className="page-header" style={{ marginTop: 8 }}>
            <h3>Current Assignment</h3>
            <button className="btn-primary" style={{ width: 'auto' }} onClick={openAssignModal}>
              + Assign New Structure
            </button>
          </div>

          {current ? (
            <div className="card" style={{ padding: 20, marginBottom: 32 }}>
              <div className="stat-grid">
                <div>
                  <span className="stat-label">Structure</span>
                  <div style={{ fontSize: 16, fontWeight: 500, marginTop: 4 }}>{current.structureName}</div>
                </div>
                <div>
                  <span className="stat-label">Effective From</span>
                  <div className="mono" style={{ fontSize: 16, marginTop: 4 }}>{current.effectiveFrom}</div>
                </div>
                <div>
                  <span className="stat-label">Effective To</span>
                  <div className="mono" style={{ fontSize: 16, marginTop: 4 }}>{current.effectiveTo || 'Ongoing'}</div>
                </div>
              </div>
            </div>
          ) : (
            <p style={{ color: 'var(--text-muted)', marginBottom: 32 }}>
              No active salary structure assigned to {selectedEmployee?.name}.
            </p>
          )}

          <h3 style={{ marginBottom: 12 }}>Assignment History</h3>
          <div className="card">
            <table>
              <thead>
                <tr className="table-header">
                  <th style={cellStyle}>Structure</th>
                  <th style={cellStyle}>Effective From</th>
                  <th style={cellStyle}>Effective To</th>
                </tr>
              </thead>
              <tbody>
                {history.map((h) => (
                  <tr key={h.id}>
                    <td style={cellStyle}>{h.structureName}</td>
                    <td style={cellStyle} className="mono">{h.effectiveFrom}</td>
                    <td style={cellStyle} className="mono">{h.effectiveTo || 'Ongoing'}</td>
                  </tr>
                ))}
                {history.length === 0 && (
                  <tr>
                    <td style={cellStyle} colSpan={3}>No assignment history.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}

      {modalOpen && (
        <Modal title={`Assign Structure — ${selectedEmployee?.name}`} onClose={() => setModalOpen(false)}>
          <form onSubmit={handleSubmit}>
            {formError && <div className="form-error">{formError}</div>}

            <label className="field">
              <span>Salary Structure</span>
              <select
                value={form.salaryStructureId}
                onChange={(e) => setForm({ ...form, salaryStructureId: e.target.value })}
              >
                <option value="">— Select structure —</option>
                {structures.map((s) => (
                  <option key={s.id} value={s.id}>{s.structureName} (₹{s.netSalary} net)</option>
                ))}
              </select>
            </label>

            <label className="field">
              <span>Effective From</span>
              <input
                type="date"
                value={form.effectiveFrom}
                onChange={(e) => setForm({ ...form, effectiveFrom: e.target.value })}
              />
            </label>

            {current && (
              <p style={{ fontSize: 12, color: 'var(--text-muted)', margin: '4px 0 16px' }}>
                Note: this will automatically close the current "{current.structureName}" assignment the day before this new effective date.
              </p>
            )}

            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Assigning…' : 'Assign Structure'}
            </button>
          </form>
        </Modal>
      )}
    </div>
  );
}

const cellStyle = { padding: '10px 16px', textAlign: 'left', fontSize: 14 };