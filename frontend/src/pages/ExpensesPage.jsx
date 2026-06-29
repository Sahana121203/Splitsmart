import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft, Plus, Receipt } from 'lucide-react'
import toast from 'react-hot-toast'
import Navbar from '../components/common/Navbar'
import BottomNav from '../components/common/BottomNav'
import Button from '../components/common/Button'
import Spinner from '../components/common/Spinner'
import ExpenseSummaryBar from '../components/expense/ExpenseSummaryBar'
import PendingEditsPanel from '../components/expense/PendingEditsPanel'
import CategoryFilter from '../components/expense/CategoryFilter'
import ExpenseCard from '../components/expense/ExpenseCard'
import AddExpenseModal from '../components/expense/AddExpenseModal'
import EditExpenseModal from '../components/expense/EditExpenseModal'
import useTripStore from '../store/tripStore'
import useAuthStore from '../store/authStore'
import useExpenseStore from '../store/expenseStore'
import useWebSocket from '../hooks/useWebSocket'

export default function ExpensesPage() {
  const { tripId } = useParams()
  const navigate = useNavigate()

  const currentTrip = useTripStore((s) => s.currentTrip)
  const members = useTripStore((s) => s.members)
  const tripLoading = useTripStore((s) => s.loading)
  const fetchTrip = useTripStore((s) => s.fetchTrip)

  const user = useAuthStore((s) => s.user)
  const currentUserId = user?.userId

  const summary = useExpenseStore((s) => s.summary)
  const pendingEdits = useExpenseStore((s) => s.pendingEdits)
  const loading = useExpenseStore((s) => s.loading)
  const error = useExpenseStore((s) => s.error)
  const fetchExpenses = useExpenseStore((s) => s.fetchExpenses)
  const fetchPendingEdits = useExpenseStore((s) => s.fetchPendingEdits)
  const deleteExpense = useExpenseStore((s) => s.deleteExpense)
  const refreshAfterExpenseEvent = useExpenseStore(
    (s) => s.refreshAfterExpenseEvent
  )
  const clearError = useExpenseStore((s) => s.clearError)
  const clearSummary = useExpenseStore((s) => s.clearSummary)

  const [activeFilter, setActiveFilter] = useState(null)
  const [showAddModal, setShowAddModal] = useState(false)
  const [editingExpense, setEditingExpense] = useState(null)

  const isAdmin =
    currentTrip?.organizerId === currentUserId ||
    members.some(
      (m) => m.userId === currentUserId && m.role === 'ADMIN'
    )

  useEffect(() => {
    fetchTrip(tripId)
  }, [tripId, fetchTrip])

  useEffect(() => {
    clearSummary()
    fetchExpenses(tripId)
    fetchPendingEdits(tripId)
  }, [tripId, fetchExpenses, fetchPendingEdits, clearSummary])

  useWebSocket(
    tripId,
    null,
    async (event) => {
      await refreshAfterExpenseEvent(tripId)
      if (event.eventType === 'ADDED') {
        toast(`New: ${event.expenseTitle} ₹${event.amount}`, { icon: '🧾' })
      }
      if (event.eventType === 'DELETED') {
        toast(`Deleted: ${event.expenseTitle}`, { icon: '🗑️' })
      }
      if (event.eventType === 'EDIT_APPROVED') {
        toast(`Edit approved: ${event.expenseTitle}`, { icon: '✅' })
      }
    },
    null,
    async (event) => {
      if (event.eventType === 'EDIT_REQUESTED') {
        toast(
          `${event.requestedByUserName} requested edit on ${event.expenseTitle}`,
          { icon: '✏️' }
        )
        if (isAdmin) {
          await fetchPendingEdits(tripId)
        }
      }
    },
    null
  )

  const handleFilterChange = (category) => {
    const newFilter = activeFilter === category ? null : category
    setActiveFilter(newFilter)
    fetchExpenses(tripId, { category: newFilter })
  }

  const handleDelete = useCallback(
    async (expense) => {
      try {
        await deleteExpense(tripId, expense.expenseId)
        toast.success('Expense deleted')
      } catch (err) {
        toast.error(err.response?.data?.error || 'Failed to delete expense')
      }
    },
    [tripId, deleteExpense]
  )

  const tripReady = currentTrip?.tripId === tripId

  if (loading && !summary) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Spinner className="h-8 w-8" />
      </div>
    )
  }

  if (error && !summary) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-6">
        <div className="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
          <p className="text-red-700 font-medium">{error}</p>
          <Button
            variant="secondary"
            className="mt-4"
            onClick={() => {
              clearError()
              fetchExpenses(tripId)
            }}
          >
            Try Again
          </Button>
        </div>
      </div>
    )
  }

  const expenses = summary?.expenses || []
  const isActive = tripReady && currentTrip?.status === 'ACTIVE'

  return (
    <div className="min-h-screen bg-[#0a0f1d] text-gray-100 relative overflow-hidden pb-12">
      {/* Background glow effects */}
      <div className="absolute top-0 left-1/4 w-96 h-96 bg-cyan-500/15 rounded-full blur-[120px] -z-10 pointer-events-none" />
      <div className="absolute top-1/3 right-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-[120px] -z-10 pointer-events-none" />
      <div className="absolute bottom-10 left-10 w-96 h-96 bg-purple-600/15 rounded-full blur-[120px] -z-10 pointer-events-none" />
      <div className="absolute bottom-0 right-10 w-80 h-80 bg-pink-500/10 rounded-full blur-[100px] -z-10 pointer-events-none" />

      <Navbar dark={true} />
      <main className="pt-16 pb-16 max-w-2xl mx-auto px-4 py-6 relative z-10 space-y-6">
        <div className="flex justify-between items-center mb-2">
          <button
            type="button"
            onClick={() => navigate(`/trips/${tripId}`)}
            className="inline-flex items-center gap-1.5 h-9 px-3.5 text-sm font-semibold rounded-lg border border-white/10 bg-white/5 text-white hover:bg-white/10 transition-all cursor-pointer hover:scale-[1.02] shadow-sm"
          >
            <ChevronLeft size={16} />
            <span>Back</span>
          </button>
          {!tripReady && tripLoading ? (
            <div className="h-11 w-36 bg-white/10 animate-pulse rounded-md" />
          ) : isActive ? (
            <Button onClick={() => setShowAddModal(true)}>
              <span className="inline-flex items-center gap-1.5">
                <Plus size={16} />
                Add Expense
              </span>
            </Button>
          ) : tripReady && currentTrip?.status ? (
            <span className="text-xs font-bold uppercase tracking-wider text-amber-400 bg-amber-500/10 border border-amber-500/20 rounded-full px-3.5 py-1">
              Trip must be Active to add expenses
            </span>
          ) : null}
        </div>

        <ExpenseSummaryBar summary={summary} />

        {isAdmin && pendingEdits.length > 0 && (
          <PendingEditsPanel
            pendingEdits={pendingEdits}
            tripId={tripId}
            onAction={() => fetchPendingEdits(tripId)}
          />
        )}

        <CategoryFilter
          activeFilter={activeFilter}
          onFilterChange={handleFilterChange}
        />

        {loading ? (
          <div className="flex justify-center py-8">
            <Spinner className="h-8 w-8" />
          </div>
        ) : expenses.length === 0 ? (
          <div className="glass-card text-center py-12 hover:border-white/10 space-y-3">
            <Receipt className="mx-auto text-primary-400 animate-pulse" size={48} />
            <p className="text-white font-bold text-lg">No expenses yet</p>
            {isActive && (
              <p className="text-sm text-gray-400">
                Add your first expense using the button above
              </p>
            )}
          </div>
        ) : (
          <div className="space-y-4">
            {expenses.map((expense) => (
              <ExpenseCard
                key={expense.expenseId}
                expense={expense}
                isAdmin={isAdmin}
                currentUserId={currentUserId}
                tripId={tripId}
                onEdit={setEditingExpense}
                onDelete={handleDelete}
              />
            ))}
          </div>
        )}
      </main>

      <AddExpenseModal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        tripId={tripId}
        members={members}
      />

      <EditExpenseModal
        isOpen={!!editingExpense}
        onClose={() => setEditingExpense(null)}
        expense={editingExpense}
        tripId={tripId}
        members={members}
        currentUserId={currentUserId}
      />

      <BottomNav tripId={tripId} />
    </div>
  )
}

