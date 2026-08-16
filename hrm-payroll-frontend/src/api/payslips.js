import client from './client';

export function generatePayslip(payload) {
  return client.post('/hr/payslips/generate', payload).then((res) => res.data);
}

export function getPayslip(id) {
  return client.get(`/hr/payslips/${id}`).then((res) => res.data);
}

export function getEmployeePayslips(employeeId) {
  return client.get(`/hr/payslips/employee/${employeeId}`).then((res) => res.data);
}

export function getPayslips(month, year) {
  return client.get('/hr/payslips', { params: { month, year } }).then((res) => res.data);
}

export function getMyPayslips(employeeId) {
  return client.get(`/employee/payslips/my/${employeeId}`).then((res) => res.data);
}

async function downloadFile(url, filename) {
  const res = await client.get(url, { responseType: 'blob' });
  const blobUrl = window.URL.createObjectURL(new Blob([res.data]));
  const link = document.createElement('a');
  link.href = blobUrl;
  link.download = filename || 'payslip.pdf';
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(blobUrl);
}

export function downloadPayslip(id, filename) {
  return downloadFile(`/hr/payslips/${id}/download`, filename);
}

export function downloadMyPayslip(id, filename) {
  return downloadFile(`/employee/payslips/${id}/download`, filename);
}
 