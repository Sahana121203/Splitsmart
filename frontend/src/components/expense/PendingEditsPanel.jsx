import { AlertCircle } from 'lucide-react'
import toast from 'react-hot-toast'
import useExpenseStore from '../../store/expenseStore'

export default function PendingEditsPanel({ pendingEdits, tripId, onAction }) {
  const approveEdit = useExpenseStore((s) => s.approveEdit)
  const rejectEdit = useExpenseStore((s) => s.rejectEdit)

  const handleApprove = async (expenseId) => {
    await approveEdit(tripId, expenseId)
    toast.success('Edit approved')
    onAction?.()
  }

  const handleReject = async (expenseId) => {
    await rejectEdit(tripId, expenseId)
    toast('Edit rejected', { icon: '❌' })
    onAction?.()
  }

  return (
    <div className="bg-amber-500/10 border border-amber-500/20 rounded-xl p-4">
      <div className="flex items-center gap-2">
        <AlertCircle className="text-amber-400 shrink-0" size={18} />
        <p className="text-xs font-bold uppercase tracking-wider text-amber-300">
          {pendingEdits.length} pending edit request(s)
        </p>
      </div>
      {pendingEdits.map((edit) => (
        <div
          key={edit.expenseId}
          className="bg-white/5 border border-white/10 rounded-xl p-3.5 mt-3 space-y-1.5"
        >
          <p className="font-semibold text-white text-sm">{edit.expenseTitle}</p>
          <p className="text-xs text-gray-400 font-medium">
            Requested by: <span className="text-gray-300 font-semibold">{edit.requestedByUserName}</span>
          </p>
          <div className="flex gap-2 mt-2">
            <button
              type="button"
              onClick={() => handleApprove(edit.expenseId)}
              className="btn-primary !h-8 !text-xs !px-3"
            >
              Approve
            </button>
            <button
              type="button"
              onClick={() => handleReject(edit.expenseId)}
              className="btn-danger !h-8 !text-xs !px-3"
            >
              Reject
            </button>
          </div>
        </div>
      ))}
    </div>
  )
}

