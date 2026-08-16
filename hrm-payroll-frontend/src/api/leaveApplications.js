import client from './client';

export function applyLeave(application) {
  return client.post('/employee/leave-applications', application).then((res) => res.data);
}

export function approveLeave(id) {
  return client.put(`/employee/leave-applications/${id}/approve`).then((res) => res.data);
}

export function rejectLeave(id) {
  return client.put(`/employee/leave-applications/${id}/reject`).then((res) => res.data);
}

export function getEmployeeLeaveApplications(employeeId) {
  return client.get(`/employee/leave-applications/employee/${employeeId}`).then((res) => res.data);
}

export function getPendingLeaveApplications() {
  return client.get('/employee/leave-applications/pending').then((res) => res.data);
}