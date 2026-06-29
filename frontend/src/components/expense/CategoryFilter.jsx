const CATEGORIES = [
  { label: 'All', value: null },
  { label: 'Food', value: 'FOOD' },
  { label: 'Transport', value: 'TRANSPORT' },
  { label: 'Accommodation', value: 'ACCOMMODATION' },
  { label: 'Activity', value: 'ACTIVITY' },
  { label: 'Shopping', value: 'SHOPPING' },
  { label: 'Other', value: 'OTHER' },
]

export default function CategoryFilter({ activeFilter, onFilterChange }) {
  return (
    <div className="overflow-x-auto flex gap-2 pb-2 scrollbar-none">
      {CATEGORIES.map(({ label, value }) => {
        const isActive = activeFilter === value
        return (
          <button
            key={label}
            type="button"
            onClick={() => onFilterChange(value)}
            className={`px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider cursor-pointer transition-all duration-150 whitespace-nowrap border ${
              isActive
                ? 'bg-primary-600 text-white border-primary-500/50 shadow-[0_0_12px_rgba(37,99,235,0.3)]'
                : 'bg-white/5 border-white/10 text-gray-400 hover:bg-white/10 hover:border-white/15 hover:text-white'
            }`}
          >
            {label}
          </button>
        )
      })}
    </div>
  )
}

