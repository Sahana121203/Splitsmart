import Badge from '../common/Badge'

const statusMap = {
  PLANNING: { variant: 'info', text: 'Planning' },
  ACTIVE: { variant: 'success', text: 'Active' },
  FROZEN: { variant: 'warning', text: 'Frozen' },
  SETTLED: { variant: 'default', text: 'Settled' },
}

export default function TripStatusBadge({ status, dark = false }) {
  const config = statusMap[status] || statusMap.PLANNING
  return <Badge variant={config.variant} dark={dark}>{config.text}</Badge>
}

