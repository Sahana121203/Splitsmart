import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import toast from 'react-hot-toast'
import Navbar from '../components/common/Navbar'
import Button from '../components/common/Button'
import Spinner from '../components/common/Spinner'
import VoteStatusCard from '../components/vote/VoteStatusCard'
import BudgetVoteForm from '../components/vote/BudgetVoteForm'
import AlreadyVotedCard from '../components/vote/AlreadyVotedCard'
import BudgetResultCard from '../components/vote/BudgetResultCard'
import useTripStore from '../store/tripStore'
import useAuthStore from '../store/authStore'
import BottomNav from '../components/common/BottomNav'

export default function BudgetVotePage() {
  const { tripId } = useParams()
  const navigate = useNavigate()

  const currentTrip = useTripStore((s) => s.currentTrip)
  const members = useTripStore((s) => s.members)
  const voteStatus = useTripStore((s) => s.voteStatus)
  const voteResult = useTripStore((s) => s.voteResult)
  const loading = useTripStore((s) => s.loading)
  const fetchTrip = useTripStore((s) => s.fetchTrip)
  const fetchVoteStatus = useTripStore((s) => s.fetchVoteStatus)
  const fetchVoteResult = useTripStore((s) => s.fetchVoteResult)
  const closeVote = useTripStore((s) => s.closeVote)

  const user = useAuthStore((s) => s.user)
  const currentUserId = user?.userId

  useEffect(() => {
    fetchTrip(tripId)
    fetchVoteStatus(tripId)
    fetchVoteResult(tripId)
  }, [tripId, fetchTrip, fetchVoteStatus, fetchVoteResult])

  const isAdmin =
    currentTrip?.organizerId === currentUserId ||
    members.some(
      (m) => m.userId === currentUserId && m.role === 'ADMIN'
    )

  const handleVoteSubmitted = () => {
    fetchVoteStatus(tripId)
    fetchVoteResult(tripId)
  }

  const handleCloseVote = async () => {
    if (!window.confirm('Close the vote early?')) return
    try {
      await closeVote(tripId)
      await fetchVoteResult(tripId)
      toast.success('Vote closed')
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to close vote')
    }
  }

  if (loading && !voteStatus) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="pt-16 flex justify-center py-16">
          <Spinner className="h-8 w-8" />
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
      <main className="pt-16 pb-16 max-w-2xl mx-auto px-4 py-6 space-y-4 relative z-10">
        <Button
          variant="secondary"
          onClick={() => navigate(`/trips/${tripId}`)}
          className="border-white/10 bg-white/5 text-white hover:bg-white/10 hover:border-white/20 transition-all shadow-sm"
        >
          <span className="inline-flex items-center gap-1.5">
            <ChevronLeft size={16} />
            Back to Trip
          </span>
        </Button>

        <h1 className="text-heading mb-2">Budget Vote</h1>

        <VoteStatusCard
          voteStatus={voteStatus}
          isAdmin={isAdmin}
          onCloseVote={handleCloseVote}
        />

        {voteStatus &&
          !voteStatus.hasCurrentUserVoted &&
          !voteStatus.voteClosed && (
            <BudgetVoteForm
              tripId={tripId}
              onVoteSubmitted={handleVoteSubmitted}
            />
          )}

        {voteStatus &&
          voteStatus.hasCurrentUserVoted &&
          !voteStatus.voteClosed && (
            <AlreadyVotedCard voteStatus={voteStatus} />
          )}

        {voteResult && <BudgetResultCard voteResult={voteResult} />}
      </main>

      <BottomNav tripId={tripId} />
    </div>
  )
}

