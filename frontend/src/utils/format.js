export function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}

export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

export function formatDateRange(startDate, endDate) {
  if (!startDate) return ''
  const start = formatDate(startDate)
  if (!endDate) return start
  return `${start} – ${formatDate(endDate)}`
}

export function kittyBarColor(percent) {
  if (percent >= 80) return 'bg-green-500'
  if (percent >= 50) return 'bg-amber-500'
  return 'bg-red-400'
}
