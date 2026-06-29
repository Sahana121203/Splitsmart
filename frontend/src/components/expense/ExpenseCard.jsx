import { useState } from 'react'
import { Pencil, Trash2 } from 'lucide-react'

const CATEGORY_ICONS = {
  FOOD: '🍽️',
  TRANSPORT: '🚗',
  ACCOMMODATION: '🏨',
  ACTIVITY: '🎯',
  SHOPPING: '🛍️',
  OTHER: '📌',
}

export default function ExpenseCard({
  expense,
  isAdmin,
  currentUserId,
  onEdit,
  onDelete,
}) {
  const [expanded, setExpanded] = useState(false)

  const canAct =
    isAdmin || expense.paidByUserId === currentUserId
  const icon = CATEGORY_ICONS[expense.category] || '📌'

  const handleCardClick = () => setExpanded((v) => !v)

  return (
    <div
      className="glass-card hover:scale-[1.01] hover:border-white/15 transition-all duration-200 cursor-pointer"
      onClick={handleCardClick}
    >
      <div className="flex justify-between items-start">
        <div className="flex items-center gap-2 min-w-0">
          <span className="text-lg">{icon}</span>
          <span className="font-semibold text-white truncate">
            {expense.title}
          </span>
        </div>
        <span className="font-extrabold text-white shrink-0 ml-2">
          ₹{expense.amount?.toLocaleString('en-IN')}
        </span>
      </div>

      <div className="text-xs text-gray-400 mt-1.5 flex items-center gap-2 flex-wrap">
        <span className="font-medium">Paid by {expense.paidByUserName}</span>
        {expense.paidFrom === 'KITTY' ? (
          <span className="text-[9px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20 shadow-[0_0_8px_rgba(59,130,246,0.1)]">
            Kitty
          </span>
        ) : (
          <span className="text-[9px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-purple-500/10 text-purple-400 border border-purple-500/20 shadow-[0_0_8px_rgba(168,85,247,0.1)]">
            External
          </span>
        )}
        {expense.editPending && (
          <span className="text-[9px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20 shadow-[0_0_8px_rgba(245,158,11,0.1)]">
            Pending Approval
          </span>
        )}
      </div>

      {expanded && (
        <div className="mt-3 pt-3 border-t border-white/10 space-y-1.5">
          <p className="text-xs uppercase tracking-wider text-gray-400 font-bold mb-1">Split between</p>
          {(expense.participants || []).map((p) => (
            <div
              key={p.userId}
              className="flex justify-between text-sm py-1 border-b border-white/5 last:border-b-0 text-gray-300"
            >
              <span>{p.userName}</span>
              <span className="font-semibold text-white">₹{p.share?.toLocaleString('en-IN')}</span>
            </div>
          ))}
        </div>
      )}

      {canAct && (
        <div
          className="flex gap-2 mt-3 pt-3 border-t border-white/10 justify-end"
          onClick={(e) => e.stopPropagation()}
        >
          {isAdmin && (
            <button
              type="button"
              onClick={() => {
                if (window.confirm('Delete this expense?')) onDelete(expense)
              }}
              className="p-2 rounded-lg text-red-400 hover:bg-red-500/10 hover:text-red-300 transition duration-150 cursor-pointer"
              aria-label="Delete expense"
            >
              <Trash2 size={16} />
            </button>
          )}
          <button
            type="button"
            onClick={() => onEdit(expense)}
            className="p-2 rounded-lg text-gray-300 border border-white/10 bg-white/5 hover:bg-white/10 hover:text-white transition duration-150 cursor-pointer"
            aria-label="Edit expense"
          >
            <Pencil size={16} />
          </button>
        </div>
      )}
    </div>
  )
}

