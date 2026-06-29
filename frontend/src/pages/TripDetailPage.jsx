import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  BarChart3,
  ChevronLeft,
  PiggyBank,
  Receipt,
  Scale,
} from 'lucide-react'
import toast from 'react-hot-toast'
import Navbar from '../components/common/Navbar'
import Button from '../components/common/Button'
import Spinner from '../components/common/Spinner'
import TripHeader from '../components/trip/TripHeader'
import StatusChangeModal from '../components/trip/StatusChangeModal'
import KittyMiniCard from '../components/kitty/KittyMiniCard'
import MemberList from '../components/trip/MemberList'
import useTripStore from '../store/tripStore'
import useAuthStore from '../store/authStore'
import useWebSocket from '../hooks/useWebSocket'
import BottomNav from '../components/common/BottomNav'

const navItems = [
  { label: 'Kitty', icon: PiggyBank, path: 'kitty' },
  { label: 'Expenses', icon: Receipt, path: 'expenses' },
  { label: 'Vote', icon: BarChart3, path: 'vote' },
  { label: 'Settlement', icon: Scale, path: 'settlement' },
]

export default function TripDetailPage() {
  const { tripId } = useParams()
  const navigate = useNavigate()
  const [statusModalOpen, setStatusModalOpen] = useState(false)

  const currentTrip = useTripStore((s) => s.currentTrip)
  const members = useTripStore((s) => s.members)
  const kitty = useTripStore((s) => s.kitty)
  const loading = useTripStore((s) => s.loading)
  const fetchTrip = useTripStore((s) => s.fetchTrip)
  const fetchKitty = useTripStore((s) => s.fetchKitty)
  const updateKittyFromEvent = useTripStore((s) => s.updateKittyFromEvent)

  const user = useAuthStore((s) => s.user)
  const currentUserId = user?.userId

  useEffect(() => {
    fetchTrip(tripId)
    fetchKitty(tripId)
  }, [tripId, fetchTrip, fetchKitty])

  useWebSocket(
    tripId,
    (event) => {
      updateKittyFromEvent(event)
      toast(`${event.depositorName} added ₹${event.depositAmount} to kitty`, {
        icon: '💰',
      })
    },
    null,
    (event) => {
      toast(`Trip is now ${event.newStatus}`, { icon: '🔄' })
      fetchTrip(tripId)
    },
    null,
    null
  )

  const isAdmin =
    currentTrip?.organizerId === currentUserId ||
    members.some(
      (m) => m.userId === currentUserId && m.role === 'ADMIN'
    )

  if (loading && !currentTrip) {
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
      <main className="pt-16 pb-16 max-w-2xl mx-auto px-4 py-6 space-y-6 relative z-10">
        <Button 
          variant="secondary" 
          onClick={() => navigate('/dashboard')}
          className="border-white/10 bg-white/5 text-white hover:bg-white/10 hover:border-white/20 transition-all shadow-sm"
        >
          <span className="inline-flex items-center gap-1.5">
            <ChevronLeft size={16} />
            My Trips
          </span>
        </Button>

        <TripHeader
          trip={currentTrip}
          isAdmin={isAdmin}
          onStatusChange={() => setStatusModalOpen(true)}
          dark={true}
        />

        <div>
          <p className="text-[10px] uppercase tracking-wider text-gray-400 font-bold mb-2 px-1">
            Quick Navigation
          </p>
          <div className="grid grid-cols-4 gap-3">
            {navItems.map(({ label, icon: Icon, path }) => (
              <button
                key={path}
                type="button"
                onClick={() => navigate(`/trips/${tripId}/${path}`)}
                className="bg-white/5 border border-white/10 rounded-xl p-3.5 flex flex-col items-center gap-1.5 cursor-pointer hover:bg-white/10 hover:border-primary-500/40 hover:shadow-[0_0_15px_rgba(59,130,246,0.15)] transition duration-200 hover:scale-[1.03] text-white"
              >
                <Icon size={24} className="text-primary-400" />
                <span className="text-xs text-gray-300 font-medium">{label}</span>
              </button>
            ))}
          </div>
        </div>

        <KittyMiniCard kitty={kitty} tripId={tripId} />

        <MemberList
          members={members}
          isAdmin={isAdmin}
          tripId={tripId}
          tripStatus={currentTrip?.status}
        />
      </main>

      <StatusChangeModal
        isOpen={statusModalOpen}
        onClose={() => setStatusModalOpen(false)}
        trip={currentTrip}
      />

      <BottomNav tripId={tripId} />
    </div>
  )
}

