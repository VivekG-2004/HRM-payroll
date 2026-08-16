import client from './client';

export function getEmployees() {
  return client.get('/hr/employees').then((res) => res.data);
}

export function getEmployee(id) {
  return client.get(`/hr/employees/${id}`).then((res) => res.data);
}

export function createEmployee(employee) {
  return client.post('/hr/employees', employee).then((res) => res.data);
}

export function updateEmployee(id, employee) {
  return client.put(`/hr/employees/${id}`, employee).then((res) => res.data);
}

export function deleteEmployee(id) {
  return client.delete(`/hr/employees/${id}`).then((res) => res.data);
}