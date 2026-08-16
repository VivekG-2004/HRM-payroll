import client from './client';

export function getDashboardSummary(month, year) {
  return client.get('/hr/dashboard/summary', { params: { month, year } }).then((res) => res.data);
}