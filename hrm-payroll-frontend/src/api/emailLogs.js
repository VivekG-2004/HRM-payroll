import client from './client';

export function getEmailLogs() {
  return client.get('/hr/email-logs').then((res) => res.data);
}

export function getFailedEmailLogs() {
  return client.get('/hr/email-logs/failed').then((res) => res.data);
}

export function retryEmailLog(id) {
  return client.post(`/hr/email-logs/${id}/retry`).then((res) => res.data);
}