import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import toast from 'react-hot-toast'
import Navbar from '../components/common/Navbar'
import Button from '../components/common/Button'
import KittyMeter from '../components/kitty/KittyMeter'
import MemberContributions from '../components/kitty/MemberContributions'
import DepositForm from '../components/kitty/DepositForm'
import DepositHistory from '../components/kitty/DepositHistory'
import useTripStore from '../store/tripStore'
import useWebSocket from '../hooks/useWebSocket'
import BottomNav from '../components/common/BottomNav'
import { getDepositHistory } from '../api/kitty'

export default function KittyPage() {
  const { tripId } = useParams()
  const navigate = useNavigate()
  const [history, setHistory] = useState([])

  const kitty = useTripStore((s) => s.kitty)
  const fetchKitty = useTripStore((s) => s.fetchKitty)
  const updateKittyFromEvent = useTripStore((s) => s.updateKittyFromEvent)

  const loadHistory = () => {
    getDepositHistory(tripId)
      .then((res) => setHistory(res.data || []))
      .catch(() => setHistory([]))
  }

  useEffect(() => {
    fetchKitty(tripId)
    loadHistory()
  }, [tripId, fetchKitty])

  useWebSocket(
    tripId,
    (event) => {
      updateKittyFromEvent(event)
      fetchKitty(tripId)
      toast(`${event.depositorName} added ₹${event.depositAmount} to kitty`, {
        icon: '💰',
      })
      getDepositHistory(tripId)
        .then((res) => setHistory(res.data || []))
        .catch(() => {})
    },
    null,
    null,
    null,
    null
  )

  return (
    <div className="min-h-screen bg-[#0a0f1d] text-gray-100 relative overflow-hidden pb-12">
      {/* Background glow effects */}
      <div className="absolute top-10 left-1/4 w-80 h-80 bg-primary-600/10 rounded-full blur-3xl -z-10 pointer-events-none" />
      <div className="absolute bottom-10 right-1/4 w-80 h-80 bg-purple-600/10 rounded-full blur-3xl -z-10 pointer-events-none" />

      <Navbar dark={true} />
      <main className="pt-16 pb-16 max-w-2xl mx-auto px-4 py-6 space-y-6 relative z-10">
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

        <KittyMeter kitty={kitty} />
        <MemberContributions
          memberContributions={kitty?.memberContributions}
        />
        <DepositForm tripId={tripId} onDeposited={loadHistory} />
        <DepositHistory history={history} />
      </main>

      <BottomNav tripId={tripId} />
    </div>
  )
}

