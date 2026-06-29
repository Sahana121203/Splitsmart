import { useLocation, useNavigate } from 'react-router-dom'
import { House, PiggyBank, Receipt, Scale } from 'lucide-react'

const tabs = [
  { key: 'home', label: 'Home', icon: House, match: (path, tripId) =>
    path === `/trips/${tripId}` || path === '/dashboard',
    path: () => '/dashboard' },
  { key: 'kitty', label: 'Kitty', icon: PiggyBank, match: (path) =>
    path.includes('/kitty'),
    path: (tripId) => `/trips/${tripId}/kitty` },
  { key: 'expenses', label: 'Expenses', icon: Receipt, match: (path) =>
    path.includes('/expenses'),
    path: (tripId) => `/trips/${tripId}/expenses` },
  { key: 'settlement', label: 'Settlement', icon: Scale, match: (path) =>
    path.includes('/settlement'),
    path: (tripId) => `/trips/${tripId}/settlement` },
]

export default function BottomNav({ tripId }) {
  const location = useLocation()
  const navigate = useNavigate()

  if (!tripId) return null

  const pathname = location.pathname

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-[#0a0f1d]/95 border-t border-white/10 flex items-center justify-around h-16 md:hidden px-2 backdrop-blur-md">
      {tabs.map(({ key, label, icon: Icon, match, path }) => {
        const active = match(pathname, tripId)
        return (
          <button
            key={key}
            type="button"
            onClick={() => navigate(path(tripId))}
            className={`flex flex-col items-center gap-1 py-2 px-3 cursor-pointer text-xs transition-colors ${
              active ? 'text-primary-400 font-semibold' : 'text-gray-400 hover:text-gray-300'
            }`}
          >
            <Icon size={22} />
            <span>{label}</span>
          </button>
        )
      })}
    </nav>
  )
}

