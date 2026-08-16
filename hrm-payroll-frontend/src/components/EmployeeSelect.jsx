import { useEffect, useState } from 'react';
import { getEmployees } from '../api/employees';

export default function EmployeeSelect({ value, onChange, label = 'Select Employee' }) {
  const [employees, setEmployees] = useState([]);

  useEffect(() => {
    getEmployees().then((data) => setEmployees(data.filter((e) => e.active)));
  }, []);

  return (
    <label className="field">
      <span>{label}</span>
      <select
        value={value}
        onChange={(e) => {
          const emp = employees.find((emp) => String(emp.id) === e.target.value);
          onChange(e.target.value, emp);
        }}
      >
        <option value="">— Choose an employee —</option>
        {employees.map((emp) => (
          <option key={emp.id} value={emp.id}>{emp.empCode} — {emp.name}</option>
        ))}
      </select>
    </label>
  );
}