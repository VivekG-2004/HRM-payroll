import client from './client';

export function getLeaveBalances(employeeId, year) {
  return client
    .get(`/employee/leave-balances/${employeeId}`, { params: year ? { year } : {} })
    .then((res) => res.data);
}