import { createContext, useContext, useState } from 'react';
import { login as loginApi } from '../api/auth';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');
    const employeeId = localStorage.getItem('employeeId');
    const employeeName = localStorage.getItem('employeeName');
    return token ? { token, username, role, employeeId, employeeName } : null;
  });

  async function login(username, password) {
    const data = await loginApi(username, password);
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
    localStorage.setItem('role', data.role);
    if (data.employeeId != null) {
      localStorage.setItem('employeeId', data.employeeId);
      localStorage.setItem('employeeName', data.employeeName ?? '');
    } else {
      localStorage.removeItem('employeeId');
      localStorage.removeItem('employeeName');
    }
    setUser(data);
    return data;
  }

  function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    localStorage.removeItem('employeeId');
    localStorage.removeItem('employeeName');
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}