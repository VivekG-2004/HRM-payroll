import { useEffect, useState } from 'react';
import Modal from '../components/Modal';
import {
  getSalaryStructures,
  createSalaryStructure,
  updateSalaryStructure,
  deleteSalaryStructure,
} from '../api/salaryStructures';

const emptyForm = {
  structureName: '',
  basicSalary: '',
  hra: '',
  specialAllowance: '',
  deductions: '',
};

export default function SalaryStructuresPage() {
  const [structures, setStructures] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [formError, setFormError] = useState('');
  const [saving, setSaving] = useState(false);

  function loadStructures() {
    setLoading(true);
    getSalaryStructures()
      .then(setStructures)
      .catch(() => setError('Could not load salary structures.'))
      .finally(() => setLoading(false));
  }

  useEffect(loadStructures, []);

  function openAddModal() {
    setEditingId(null);
    setForm(emptyForm);
    setFormError('');
    setModalOpen(true);
  }

  function openEditModal(s) {
    setEditingId(s.id);
    setForm({
      structureName: s.structureName,
      basicSalary: s.basicSalary,
      hra: s.hra,
      specialAllowance: s.specialAllowance,
      deductions: s.deductions,
    });
    setFormError('');
    setModalOpen(true);
  }

  // Client-side preview only — actual gross/net always come from the server response.
  const num = (v) => Number(v) || 0;
  const previewGross = num(form.basicSalary) + num(form.hra) + num(form.specialAllowance);
  const previewNet = previewGross - num(form.deductions);

  async function handleSubmit(e) {
    e.preventDefault();
    setFormError('');

    if (!form.structureName || form.basicSalary === '' || form.hra === '' || form.specialAllowance === '') {
      setFormError('Structure name, basic salary, HRA, and special allowance are required.');
      return;
    }

    const payload = {
      structureName: form.structureName,
      basicSalary: num(form.basicSalary),
      hra: num(form.hra),
      specialAllowance: num(form.specialAllowance),
      deductions: num(form.deductions),
    };

    setSaving(true);
    try {
      if (editingId) {
        await updateSalaryStructure(editingId, payload);
      } else {
        await createSalaryStructure(payload);
      }
      setModalOpen(false);
      loadStructures();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Could not save salary structure.');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(s) {
    if (!confirm(`Delete salary structure "${s.structureName}"? This cannot be undone.`)) return;
    try {
      await deleteSalaryStructure(s.id);
      loadStructures();
    } catch (err) {
      alert(err.response?.data?.message || 'Could not delete — it may be assigned to an employee.');
    }
  }

  return (
    <div>
      <div className="page-header">
        <h2>Salary Structures</h2>
        <button className="btn-primary" style={{ width: 'auto' }} onClick={openAddModal}>
          + Add Structure
        </button>
      </div>

      {loading && <p style={{ color: 'var(--text-muted)' }}>Loading…</p>}
      {error && <div className="form-error">{error}</div>}

      {!loading && !error && (
        <div className="card">
          <table>
            <thead>
              <tr className="table-header">
                <th style={cellStyle}>Name</th>
                <th style={cellStyle}>Basic</th>
                <th style={cellStyle}>HRA</th>
                <th style={cellStyle}>Special Allow.</th>
                <th style={cellStyle}>Deductions</th>
                <th style={cellStyle}>Gross</th>
                <th style={cellStyle}>Net</th>
                <th style={cellStyle}></th>
              </tr>
            </thead>
            <tbody>
              {structures.map((s) => (
                <tr key={s.id}>
                  <td style={cellStyle}>{s.structureName}</td>
                  <td style={cellStyle} className="mono">₹{s.basicSalary}</td>
                  <td style={cellStyle} className="mono">₹{s.hra}</td>
                  <td style={cellStyle} className="mono">₹{s.specialAllowance}</td>
                  <td style={cellStyle} className="mono">₹{s.deductions}</td>
                  <td style={cellStyle} className="mono">₹{s.grossSalary}</td>
                  <td style={cellStyle} className="mono">₹{s.netSalary}</td>
                  <td style={{ ...cellStyle, textAlign: 'right' }}>
                    <button className="btn-outline" onClick={() => openEditModal(s)}>Edit</button>
                    <button
                      className="btn-outline"
                      style={{ marginLeft: 8, borderColor: 'var(--danger)', color: 'var(--danger)' }}
                      onClick={() => handleDelete(s)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {structures.length === 0 && (
                <tr>
                  <td style={cellStyle} colSpan={8}>No salary structures yet.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {modalOpen && (
        <Modal title={editingId ? 'Edit Salary Structure' : 'Add Salary Structure'} onClose={() => setModalOpen(false)}>
          <form onSubmit={handleSubmit}>
            {formError && <div className="form-error">{formError}</div>}

            <label className="field">
              <span>Structure Name</span>
              <input
                value={form.structureName}
                onChange={(e) => setForm({ ...form, structureName: e.target.value })}
              />
            </label>

            <div className="form-grid">
              <label className="field">
                <span>Basic Salary</span>
                <input
                  type="number"
                  value={form.basicSalary}
                  onChange={(e) => setForm({ ...form, basicSalary: e.target.value })}
                />
              </label>
              <label className="field">
                <span>HRA</span>
                <input
                  type="number"
                  value={form.hra}
                  onChange={(e) => setForm({ ...form, hra: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Special Allowance</span>
                <input
                  type="number"
                  value={form.specialAllowance}
                  onChange={(e) => setForm({ ...form, specialAllowance: e.target.value })}
                />
              </label>
              <label className="field">
                <span>Deductions</span>
                <input
                  type="number"
                  value={form.deductions}
                  onChange={(e) => setForm({ ...form, deductions: e.target.value })}
                />
              </label>
            </div>

            <div className="preview-box">
              <div>
                <span className="stat-label">Gross Salary</span>
                <div className="mono" style={{ fontSize: 18, fontWeight: 600 }}>₹{previewGross}</div>
              </div>
              <div>
                <span className="stat-label">Net Salary</span>
                <div className="mono" style={{ fontSize: 18, fontWeight: 600, color: 'var(--success)' }}>₹{previewNet}</div>
              </div>
            </div>

            <button type="submit" className="btn-primary" disabled={saving}>
              {saving ? 'Saving…' : editingId ? 'Save Changes' : 'Add Structure'}
            </button>
          </form>
        </Modal>
      )}
    </div>
  );
}

const cellStyle = { padding: '10px 16px', textAlign: 'left', fontSize: 14 };