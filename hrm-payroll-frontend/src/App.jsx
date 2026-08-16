import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from './auth/ProtectedRoute';
import AppLayout from './layouts/AppLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import EmployeesPage from './pages/EmployeesPage';
import SalaryStructuresPage from './pages/SalaryStructuresPage';
import SalaryAssignmentsPage from './pages/SalaryAssignmentsPage';
import LeaveApplicationsPage from './pages/LeaveApplicationsPage';
import PayslipsPage from './pages/PayslipsPage';
import EmailLogsPage from './pages/EmailLogsPage';
import MyLeavePage from './pages/MyLeavePage';
import MyPayslipsPage from './pages/MyPayslipsPage';


export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<DashboardPage />} />

        <Route
          path="/employees"
          element={
            <ProtectedRoute allowedRoles={['HR']}>
              <EmployeesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/salary-structures"
          element={
            <ProtectedRoute allowedRoles={['HR']}>
              <SalaryStructuresPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/salary-assignments"
          element={
            <ProtectedRoute allowedRoles={['HR']}>
              <SalaryAssignmentsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/leave-applications"
          element={
            <ProtectedRoute allowedRoles={['HR']}>
              <LeaveApplicationsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/payslips"
          element={
            <ProtectedRoute allowedRoles={['HR']}>
              <PayslipsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/email-logs"
          element={
            <ProtectedRoute allowedRoles={['HR']}>
              <EmailLogsPage />
            </ProtectedRoute>
          }
        />

        <Route path="/my-leave" element={<MyLeavePage />} />
        <Route path="/my-payslips" element={<MyPayslipsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}