import client from './client';

export function assignSalaryStructure(assignment) {
  return client.post('/hr/salary-assignments', assignment).then((res) => res.data);
}

export function getCurrentAssignment(employeeId) {
  return client.get(`/hr/salary-assignments/employee/${employeeId}/current`).then((res) => res.data);
}

export function getAssignmentHistory(employeeId) {
  return client.get(`/hr/salary-assignments/employee/${employeeId}/history`).then((res) => res.data);
}