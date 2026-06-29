import { useEffect, useMemo, useState } from 'react'
import toast from 'react-hot-toast'
import Modal from '../common/Modal'
import Button from '../common/Button'
import useExpenseStore from '../../store/expenseStore'
import useAuthStore from '../../store/authStore'

const CATEGORIES = [
  'FOOD',
  'TRANSPORT',
  'ACCOMMODATION',
  'ACTIVITY',
  'SHOPPING',
  'OTHER',
]

const CATEGORY_LABELS = {
  FOOD: 'Food',
  TRANSPORT: 'Transport',
  ACCOMMODATION: 'Accommodation',
  ACTIVITY: 'Activity',
  SHOPPING: 'Shopping',
  OTHER: 'Other',
}

const selectClass =
  'w-full h-11 rounded-md border border-gray-300 px-3 outline-none transition focus:ring-2 focus:ring-primary-100'

const inputClass =
  'w-full h-11 rounded-md border border-gray-300 px-3 outline-none transition focus:ring-2 focus:ring-primary-100'

export default function AddExpenseModal({
  isOpen,
  onClose,
  tripId,
  members,
}) {
  const addExpense = useExpenseStore((s) => s.addExpense)
  const currentUserId = useAuthStore((s) => s.user?.userId)

  const [title, setTitle] = useState('')
  const [amount, setAmount] = useState('')
  const [category, setCategory] = useState('OTHER')
  const [paidByUserId, setPaidByUserId] = useState(currentUserId || '')
  const [paidFrom, setPaidFrom] = useState('KITTY')
  const [splitTab, setSplitTab] = useState('equal')
  const [checkedMembers, setCheckedMembers] = useState([])
  const [customShares, setCustomShares] = useState({})
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (!isOpen) return
    setTitle('')
    setAmount('')
    setCategory('OTHER')
    setPaidByUserId(currentUserId || members[0]?.userId || '')
    setPaidFrom('KITTY')
    setSplitTab('equal')
    setCheckedMembers(members.map((m) => m.userId))
    setCustomShares(
      Object.fromEntries(members.map((m) => [m.userId, '']))
    )
  }, [isOpen, members, currentUserId])

  const numAmount = Number(amount) || 0
  const checkedCount = checkedMembers.length
  const equalShare =
    checkedCount > 0 ? numAmount / checkedCount : 0

  const customTotal = useMemo(
    () =>
      members.reduce(
        (sum, m) => sum + (Number(customShares[m.userId]) || 0),
        0
      ),
    [members, customShares]
  )

  const sharesValid =
    splitTab === 'equal' ||
    (numAmount > 0 && Math.abs(customTotal - numAmount) < 0.01)

  const toggleMember = (userId) => {
    setCheckedMembers((prev) =>
      prev.includes(userId)
        ? prev.filter((id) => id !== userId)
        : [...prev, userId]
    )
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!title.trim() || numAmount < 1) return
    if (splitTab === 'equal' && checkedCount === 0) return
    if (!sharesValid) return

    const participants =
      splitTab === 'equal'
        ? checkedMembers.map((userId) => ({ userId }))
        : members.map((m) => ({
            userId: m.userId,
            share: Number(customShares[m.userId]) || 0,
          }))

    setSubmitting(true)
    try {
      await addExpense(tripId, {
        title: title.trim(),
        amount: numAmount,
        category,
        paidByUserId,
        paidFrom,
        equalSplit: splitTab === 'equal',
        participants,
      })
      toast.success('Expense added!')
      onClose()
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to add expense')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add Expense">
      <form className="space-y-4" onSubmit={handleSubmit}>
        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700">
            Title
          </label>
          <input
            className={inputClass}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </div>

        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700">
            Amount (₹)
          </label>
          <input
            type="number"
            min={1}
            className={inputClass}
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            required
          />
        </div>

        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700">
            Category
          </label>
          <select
            className={selectClass}
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          >
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {CATEGORY_LABELS[c]}
              </option>
            ))}
          </select>
        </div>

        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700">
            Paid By
          </label>
          <select
            className={selectClass}
            value={paidByUserId}
            onChange={(e) => setPaidByUserId(e.target.value)}
          >
            {members.map((m) => (
              <option key={m.userId} value={m.userId}>
                {m.name}
              </option>
            ))}
          </select>
        </div>

        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700">
            Paid From
          </label>
          <select
            className={selectClass}
            value={paidFrom}
            onChange={(e) => setPaidFrom(e.target.value)}
          >
            <option value="KITTY">Kitty</option>
            <option value="EXTERNAL">External</option>
          </select>
        </div>

        <div>
          <div className="flex border-b border-gray-200 mb-3">
            <button
              type="button"
              onClick={() => setSplitTab('equal')}
              className={`flex-1 py-2 text-sm font-medium border-b-2 transition ${
                splitTab === 'equal'
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-500'
              }`}
            >
              Equal Split
            </button>
            <button
              type="button"
              onClick={() => setSplitTab('custom')}
              className={`flex-1 py-2 text-sm font-medium border-b-2 transition ${
                splitTab === 'custom'
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-gray-500'
              }`}
            >
              Custom Split
            </button>
          </div>

          {splitTab === 'equal' ? (
            <div>
              <p className="text-sm font-medium text-gray-700 mb-2">
                Split between
              </p>
              <div className="space-y-2">
                {members.map((m) => (
                  <label
                    key={m.userId}
                    className="flex items-center gap-2 text-sm"
                  >
                    <input
                      type="checkbox"
                      checked={checkedMembers.includes(m.userId)}
                      onChange={() => toggleMember(m.userId)}
                    />
                    {m.name}
                  </label>
                ))}
              </div>
              {checkedCount > 0 && numAmount > 0 && (
                <p className="text-sm text-gray-500 mt-2">
                  ₹{equalShare.toLocaleString('en-IN')} each
                </p>
              )}
            </div>
          ) : (
            <div>
              {members.map((m) => (
                <div
                  key={m.userId}
                  className="flex items-center gap-3 mb-2"
                >
                  <div className="h-8 w-8 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center text-sm font-medium shrink-0">
                    {m.name?.charAt(0)?.toUpperCase()}
                  </div>
                  <span className="flex-1 text-sm">{m.name}</span>
                  <input
                    type="number"
                    min={0}
                    placeholder="0"
                    className="w-24 h-9 rounded-md border border-gray-300 px-2 text-sm"
                    value={customShares[m.userId] ?? ''}
                    onChange={(e) =>
                      setCustomShares((prev) => ({
                        ...prev,
                        [m.userId]: e.target.value,
                      }))
                    }
                  />
                </div>
              ))}
              <p
                className={`text-sm mt-2 ${
                  Math.abs(customTotal - numAmount) < 0.01
                    ? 'text-green-600'
                    : 'text-red-600'
                }`}
              >
                Total: ₹{customTotal.toLocaleString('en-IN')} / ₹
                {numAmount.toLocaleString('en-IN')}
              </p>
              {!sharesValid && numAmount > 0 && (
                <p className="text-sm text-red-500 mt-1">
                  Shares must sum to ₹{numAmount.toLocaleString('en-IN')}
                </p>
              )}
            </div>
          )}
        </div>

        <div className="flex gap-3 pt-2">
          <Button variant="secondary" onClick={onClose} fullWidth>
            Cancel
          </Button>
          <Button
            type="submit"
            fullWidth
            loading={submitting}
            disabled={!sharesValid}
          >
            Add Expense
          </Button>
        </div>
      </form>
    </Modal>
  )
}
