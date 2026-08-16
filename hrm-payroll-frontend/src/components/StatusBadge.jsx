const toneMap = {
  active: 'success',
  approved: 'success',
  sent: 'success',
  inactive: 'muted',
  pending: 'warning',
  rejected: 'danger',
  failed: 'danger',
};

export default function StatusBadge({ status }) {
  const key = String(status).toLowerCase();
  const tone = toneMap[key] || 'muted';
  return <span className={`badge badge-${tone}`}>{status}</span>;
}