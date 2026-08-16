import { useEffect, useState } from 'react';
import Modal from '../components/Modal';
import StatusBadge from '../components/StatusBadge';
import {
  getEmployees,
  createEmployee,
  updateEmployee,
  deleteEmployee,
} from '../api/employees';

const emptyForm = {
  empCode: '',
  name: '',
  email: '',
  department: '',
  designation: '',
  joiningDate: '',
};

export default function EmployeesPage() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);
  const [newCredentials, setNewCredentials] = useState(null);

  function loadEmployees() {
    setLoading(true);
    getEmployees()
      .then(setEmployees)
      .catch(() => setError('Could not load employees.'))
      .finally(() => setLoading(false));
  }

  useEffect(loadEmployees, []);

  function openAddModal() {
    setEditingId(null);
    setForm(emptyForm);
    setFormError('');
    setModalOpen(true);
  }

  function openEditModal(emp) {
    setEditingId(emp.id);
    setForm({
      empCode: emp.empCode,
      name: emp.name,
      email: emp.email,
      department: emp.department,
      designation: emp.designation,
      joiningDate: emp.joiningDate,
    });
    setFormError('');
    setModalOpen(true);
  }

  async function handleSubmit(e) {
  e.preventDefault();
  setFormError('');

  if (!form.empCode || !form.name || !form.email || !form.joiningDate) {
    setFormError('Emp code, name, email, and joining date are required.');
    return;
  }

  setSaving(true);
  try {
    if (editingId) {
      await updateEmployee(editingId, form);
      setModalOpen(false);
      loadEmployees();
    } else {
      const created = await createEmployee(form);
      setModalOpen(false);
      loadEmployees();
      if (created.username) {
        setNewCredentials({ username: created.username, password: created.temporaryPassword });
      }
    }
  } catch (err) {
    setFormError(err.response?.data?.message || 'Could not save employee.');
  } finally {
    setSaving(false);
  }
}

  async function handleDeactivate(emp) {
    if (!confirm(`Deactivate ${emp.name}? This does not permanently delete their record.`)) return;
    try {
      await deleteEmployee(emp.id);
      loadEmployees();
    } catch {
      alert('Could not deactivate employee.');
    }
  }

  async function handleReactivate(emp) {
  if (!confirm(`Reactivate ${emp.name}?`)) return;
  try {
    await updateEmployee(emp.id, {
      empCode: emp.empCode,
      name: emp.name,
      email: emp.email,
      department: emp.department,
      designation: emp.designation,
      joiningDate: emp.joiningDate,
      active: true,
    });
    loadEmployees();
  } catch {
    alert('Could not reactivate employee.');
  }
}

  return (
    <div>
      <div className="page-header">
        <h2>Employees</h2>
        <button className="btn-primary" style={{ width: 'auto' }} onClick={openAddModal}>
          + Add Employee
        </button>
      </div>

      {loading && <p style={{ color: 'var(--text-muted)' }}>Loading…</p>}
      {error && <div className="form-error">{error}</div>}

      {!loading && !error && (
        <div className="card">
          <table>
            <thead>
              <tr className="table-header">
                <th style={cellStyle}>Code</th>
                <th style={cellStyle}>Name</th>
                <th style={cellStyle}>Email</th>
                <th style={cellStyle}>Department</th>
                <th style={cellStyle}>Designation</th>
                <th style={cellStyle}>Status</th>
                <th style={cellStyle}></th>
              </tr>
            </thead>
            <tbody>
              {employees.map((emp) => (
                <tr key={emp.id}>
                  <td style={cellStyle} className="mono">{emp.empCode}</td>
                  <td style={cellStyle}>{emp.name}</td>
                  <td style={cellStyle}>{emp.email}</td>
                  <td style={cellStyle}>{emp.department}</td>
                  <td style={cellStyle}>{emp.designation}</td>
                  <td style={cellStyle}>
                    <StatusBadge status={emp.active ? 'Active' : 'Inactive'} />
                  </td>
                  <td style={{ ...cellStyle, textAlign: 'right' }}>
                    <button className="btn-outline" onClick={() => openEditModal(emp)}>Edit</button>
                    {emp.active ? (
                      <button
                         className="btn-outline"
                          style={{ marginLeft: 8, borderColor: 'var(--danger)', color: 'var(--danger)' }}
                          onClick={() => handleDeactivate(emp)}
                      >
                       Deactivate
                      </button>
                    ) : (
                    <button
                    className="btn-outline"
                    style={{ marginLeft: 8, borderColor: 'var(--success)', color: 'var(--success)' }}
                    onClick={() => handleReactivate(emp)}
                    >
                Reactivate
                </button>
                  )}
                </td>
                </tr>
              ))}
              {employees.length === 0 && (
                <tr>
                  <td style={cellStyle} colSpan={7}>No employees yet.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {modalOpen && (
        <Modal title={editingId ? 'Edit Employee' : 'Add Employee'} onClose={() => setModalOpen(false)}>
          <form onSubmit={handleSubmit}>
            {formError && <div className="form-error">{formError}</div>}

            <div className="form-grid">
              <label className="field">
                <span>Employee Code</span>
                <input
                  value={form.empCode}
                  onChange={(e) => setForm({ ...form, empCode: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Name</span>
                <input
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Email</span>
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Department</span>
                <input
                  value={form.department}
                  onChange={(e) => setForm({ ...form, department: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Designation</span>
                <input
                  value={form.designation}
                  onChange={(e) => setForm({ ...form, designation: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Joining Date</span>
                <input
                  type="date"
                  value={form.joiningDate}
                  onChange={(e) => setForm({ ...form, joiningDate: e.target.value })}
                />
              </label>
            </div>

            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Saving…' : editingId ? 'Save Changes' : 'Add Employee'}
            </button>
          </form>
        </Modal>
        
      )}
      {newCredentials && (
  <Modal title="Employee Created" onClose={() => setNewCredentials(null)}>
    <p style={{ marginBottom: 16, color: 'var(--text-muted)', fontSize: 14 }}>
      Share these login details with the employee. For security, this password will not be shown again.
    </p>

    <div className="credentials-box">
      <div className="credentials-row">
        <span className="stat-label">Username</span>
        <span className="mono" style={{ fontSize: 16 }}>{newCredentials.username}</span>
      </div>
      <div className="credentials-row">
        <span className="stat-label">Temporary Password</span>
        <span className="mono" style={{ fontSize: 16 }}>{newCredentials.password}</span>
      </div>
    </div>

    <button
      type="button"
      className="btn-outline"
      style={{ width: '100%', marginBottom: 10 }}
      onClick={() => {
        navigator.clipboard.writeText(
          `Username: ${newCredentials.username}\nPassword: ${newCredentials.password}`
        );
      }}
    >
      Copy to Clipboard
    </button>

    <button
      type="button"
      className="btn-primary"
      onClick={() => setNewCredentials(null)}
    >
      Done
    </button>
  </Modal>
)}
    </div>
  );
}

const cellStyle = { padding: '10px 16px', textAlign: 'left', fontSize: 14 };