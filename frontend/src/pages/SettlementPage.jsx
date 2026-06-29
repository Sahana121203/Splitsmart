import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  AlertTriangle,
  CheckCircle,
  ChevronLeft,
  Lock,
} from 'lucide-react'
import toast from 'react-hot-toast'
import Navbar from '../components/common/Navbar'
import BottomNav from '../components/common/BottomNav'
import Button from '../components/common/Button'
import Spinner from '../components/common/Spinner'
import SettlementModeCard from '../components/settlement/SettlementModeCard'
import MemberBalanceList from '../components/settlement/MemberBalanceList'
import TransferInstructionList from '../components/settlement/TransferInstructionList'
import useTripStore from '../store/tripStore'
import useAuthStore from '../store/authStore'
import useExpenseStore from '../store/expenseStore'
import useWebSocket from '../hooks/useWebSocket'
import { getSettlementResult } from '../api/settlement'

export default function SettlementPage() {
  const { tripId } = useParams()
  const navigate = useNavigate()

  const currentTrip = useTripStore((s) => s.currentTrip)
  const members = useTripStore((s) => s.members)
  const tripLoading = useTripStore((s) => s.loading)
  const fetchTrip = useTripStore((s) => s.fetchTrip)

  const user = useAuthStore((s) => s.user)
  const currentUserId = user?.userId

  const settlement = useExpenseStore((s) => s.settlement)
  const settlementLoading = useExpenseStore((s) => s.settlementLoading)
  const error = useExpenseStore((s) => s.error)
  const fetchSettlement = useExpenseStore((s) => s.fetchSettlement)
  const finaliseSettlement = useExpenseStore((s) => s.finaliseSettlement)
  const clearError = useExpenseStore((s) => s.clearError)

  const [showConfirm, setShowConfirm] = useState(false)
  const [finalisedAt, setFinalisedAt] = useState(null)

  const tripReady = currentTrip?.tripId === tripId
  const isAdmin =
    tripReady &&
    (currentTrip?.organizerId === currentUserId ||
      members.some(
        (m) => m.userId === currentUserId && m.role === 'ADMIN'
      ))

  useEffect(() => {
    fetchTrip(tripId)
  }, [tripId, fetchTrip])

  useEffect(() => {
    if (!tripReady) return
    const status = currentTrip?.status
    if (status === 'FROZEN' || status === 'SETTLED') {
      fetchSettlement(tripId)
    }
    if (status === 'SETTLED') {
      getSettlementResult(tripId)
        .then((res) => setFinalisedAt(res.data?.finalisedAt))
        .catch(() => setFinalisedAt(null))
    }
  }, [tripId, tripReady, currentTrip?.status, fetchSettlement])

  useWebSocket(
    tripId,
    null,
    null,
    null,
    null,
    (event) => {
      if (event.eventType === 'FINALISED') {
        toast.success('Settlement finalised! 🎉')
        fetchSettlement(tripId)
        fetchTrip(tripId)
      }
    }
  )

  const handleFinalise = async () => {
    try {
      const res = await finaliseSettlement(tripId)
      toast.success('Trip settled! 🎉')
      if (res?.finalisedAt) setFinalisedAt(res.finalisedAt)
      navigate(`/trips/${tripId}`)
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to finalise settlement')
    }
  }

  if (tripLoading && !tripReady) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Spinner className="h-8 w-8" />
      </div>
    )
  }

  const status = tripReady ? currentTrip?.status : null
  const isBlocked = status === 'PLANNING' || status === 'ACTIVE'

  if (isBlocked) {
    return (
      <div className="min-h-screen bg-[#0a0f1d] text-gray-100 relative overflow-hidden pb-12">
        {/* Background glow effects */}
        <div className="absolute top-0 left-1/4 w-96 h-96 bg-cyan-500/15 rounded-full blur-[120px] -z-10 pointer-events-none" />
        <div className="absolute top-1/3 right-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-[120px] -z-10 pointer-events-none" />
        <div className="absolute bottom-10 left-10 w-96 h-96 bg-purple-600/15 rounded-full blur-[120px] -z-10 pointer-events-none" />
        <div className="absolute bottom-0 right-10 w-80 h-80 bg-pink-500/10 rounded-full blur-[100px] -z-10 pointer-events-none" />

        <Navbar dark={true} />
        <main className="pt-16 pb-16 max-w-2xl mx-auto px-4 py-6 relative z-10 space-y-6">
          <button
            type="button"
            onClick={() => navigate(`/trips/${tripId}`)}
            className="inline-flex items-center gap-1.5 h-9 px-3.5 text-sm font-semibold rounded-lg border border-white/10 bg-white/5 text-white hover:bg-white/10 transition-all cursor-pointer hover:scale-[1.02] shadow-sm"
          >
            <ChevronLeft size={16} />
            <span>Back</span>
          </button>

          <h1 className="text-heading">Settlement</h1>

          <div className="glass-card bg-amber-500/5 border border-amber-500/20 p-6 text-center space-y-4 hover:border-amber-500/30">
            <Lock className="mx-auto text-amber-400 mb-2" size={40} />
            <p className="text-lg font-bold text-amber-300">
              Trip must be frozen first
            </p>
            <p className="text-sm text-gray-400">
              Freeze the trip from the trip detail page before viewing settlement.
            </p>
            <Button
              variant="secondary"
              className="mx-auto"
              onClick={() => navigate(`/trips/${tripId}`)}
            >
              Go to Trip Detail
            </Button>
          </div>
        </main>
        <BottomNav tripId={tripId} />
      </div>
    )
  }

  if (settlementLoading && !settlement) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-[#0a0f1d]">
        <Spinner className="h-8 w-8" />
      </div>
    )
  }

  if (error && !settlement) {
    return (
      <div className="min-h-screen bg-[#0a0f1d] text-gray-100 flex items-center justify-center p-4">
        <div className="w-full max-w-md glass-card p-6 text-center space-y-4">
          <p className="text-red-400 font-semibold">{error}</p>
          <Button
            variant="secondary"
            className="mx-auto"
            onClick={() => {
              clearError()
              fetchSettlement(tripId)
            }}
          >
            Try Again
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-[#0a0f1d] text-gray-100 relative overflow-hidden pb-12">
      {/* Background glow effects */}
      <div className="absolute top-0 left-1/4 w-96 h-96 bg-cyan-500/15 rounded-full blur-[120px] -z-10 pointer-events-none" />
      <div className="absolute top-1/3 right-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-[120px] -z-10 pointer-events-none" />
      <div className="absolute bottom-10 left-10 w-96 h-96 bg-purple-600/15 rounded-full blur-[120px] -z-10 pointer-events-none" />
      <div className="absolute bottom-0 right-10 w-80 h-80 bg-pink-500/10 rounded-full blur-[100px] -z-10 pointer-events-none" />

      <Navbar dark={true} />
      <main className="pt-16 pb-16 max-w-2xl mx-auto px-4 py-6 relative z-10 space-y-6">
        <button
          type="button"
          onClick={() => navigate(`/trips/${tripId}`)}
          className="inline-flex items-center gap-1.5 h-9 px-3.5 text-sm font-semibold rounded-lg border border-white/10 bg-white/5 text-white hover:bg-white/10 transition-all cursor-pointer hover:scale-[1.02] shadow-sm"
        >
          <ChevronLeft size={16} />
          <span>Back</span>
        </button>

        <h1 className="text-heading">Settlement</h1>

        {settlement && (
          <div className="space-y-6">
            <SettlementModeCard settlement={settlement} />
            <MemberBalanceList
              memberBalances={settlement.memberBalances}
            />
            <TransferInstructionList
              transferInstructions={settlement.transferInstructions}
              mode={settlement.mode}
            />

            {status === 'SETTLED' ? (
              <div className="bg-green-500/10 border border-green-500/20 rounded-2xl p-6 text-center">
                <CheckCircle
                  className="mx-auto text-green-400 mb-2 shadow-[0_0_8px_rgba(34,197,94,0.3)]"
                  size={28}
                />
                <p className="font-bold text-green-400">
                  This trip is fully settled.
                </p>
                {finalisedAt && (
                  <p className="text-xs text-gray-400 mt-1.5 font-medium">
                    Finalised on{' '}
                    {new Date(finalisedAt).toLocaleString('en-IN')}
                  </p>
                )}
              </div>
            ) : (
              isAdmin &&
              settlement.canFinalise && (
                <>
                  {!showConfirm ? (
                    <Button
                      fullWidth
                      className="mt-6"
                      onClick={() => setShowConfirm(true)}
                    >
                      Finalise Settlement
                    </Button>
                  ) : (
                    <div className="bg-red-500/10 border border-red-500/20 rounded-2xl p-4 mt-6">
                      <div className="flex items-start gap-2.5">
                        <AlertTriangle
                          className="text-red-400 shrink-0 mt-0.5"
                          size={20}
                        />
                        <div>
                          <p className="font-bold text-red-300 text-sm uppercase tracking-wider">
                            Are you sure?
                          </p>
                          <p className="text-xs text-gray-400 mt-1 font-medium">
                            This will mark the trip as SETTLED and cannot be
                            undone.
                          </p>
                        </div>
                      </div>
                      <div className="flex gap-3 mt-4">
                        <Button
                          variant="secondary"
                          onClick={() => setShowConfirm(false)}
                          fullWidth
                        >
                          Cancel
                        </Button>
                        <Button
                          variant="danger"
                          onClick={handleFinalise}
                          fullWidth
                        >
                          Yes, Finalise
                        </Button>
                      </div>
                    </div>
                  )}
                </>
              )
            )}
          </div>
        )}

        {settlementLoading && settlement && (
          <div className="flex justify-center py-4">
            <Spinner className="h-6 w-6" />
          </div>
        )}
      </main>
      <BottomNav tripId={tripId} />
    </div>
  )
}
