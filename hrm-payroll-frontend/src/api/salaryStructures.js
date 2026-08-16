import client from './client';

export function getSalaryStructures() {
  return client.get('/hr/salary-structures').then((res) => res.data);
}

export function getSalaryStructure(id) {
  return client.get(`/hr/salary-structures/${id}`).then((res) => res.data);
}

export function createSalaryStructure(structure) {
  return client.post('/hr/salary-structures', structure).then((res) => res.data);
}

export function updateSalaryStructure(id, structure) {
  return client.put(`/hr/salary-structures/${id}`, structure).then((res) => res.data);
}

export function deleteSalaryStructure(id) {
  return client.delete(`/hr/salary-structures/${id}`).then((res) => res.data);
}