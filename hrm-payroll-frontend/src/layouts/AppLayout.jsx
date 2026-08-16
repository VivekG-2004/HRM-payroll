import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const hrNavItems = [
  { to: '/', label: 'Dashboard' },
  { to: '/employees', label: 'Employees' },
  { to: '/salary-structures', label: 'Salary Structures' },
  { to: '/salary-assignments', label: 'Salary Assignments' },
  { to: '/leave-applications', label: 'Leave Applications' },
  { to: '/payslips', label: 'Payslips' },
  { to: '/email-logs', label: 'Email Logs' },
];

const employeeNavItems = [
  { to: '/', label: 'Leave Balances' },
  { to: '/my-leave', label: 'My Leave' },
  { to: '/my-payslips', label: 'My Payslips' },
];

export default function AppLayout() {
  const { user, logout } = useAuth();
  const navItems = user.role === 'HR' ? hrNavItems : employeeNavItems;

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h1 className="brand">Payroll HRM</h1>
        <nav>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="main-area">
        <header className="topbar">
          <span className="topbar-user">
            {user.username} <span className="role-tag">{user.role}</span>
          </span>
          <button className="btn-link" onClick={logout}>Log out</button>
        </header>

        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}