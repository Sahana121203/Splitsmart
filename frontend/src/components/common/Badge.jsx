const badgeMap = {
  success: 'bg-green-100 text-green-700',
  warning: 'bg-yellow-100 text-yellow-700',
  danger: 'bg-red-100 text-red-700',
  info: 'bg-blue-100 text-blue-700',
  default: 'bg-gray-100 text-gray-700',
}

const darkBadgeMap = {
  success: 'bg-green-500/10 text-green-400 border border-green-500/20 shadow-[0_0_10px_rgba(34,197,94,0.1)]',
  warning: 'bg-yellow-500/10 text-yellow-400 border border-yellow-500/20 shadow-[0_0_10px_rgba(234,179,8,0.1)]',
  danger: 'bg-red-500/10 text-red-400 border border-red-500/20 shadow-[0_0_10px_rgba(239,68,68,0.1)]',
  info: 'bg-blue-500/10 text-blue-400 border border-blue-500/20 shadow-[0_0_10px_rgba(59,130,246,0.1)]',
  default: 'bg-white/5 text-gray-300 border border-white/10',
}

export default function Badge({ children, variant = 'default', dark = false }) {
  const classes = dark ? darkBadgeMap[variant] : badgeMap[variant]
  return (
    <span className={`inline-flex px-2 py-0.5 text-xs rounded-full font-medium ${classes}`}>
      {children}
    </span>
  )
}

