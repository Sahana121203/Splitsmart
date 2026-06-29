import { useEffect, useState } from 'react'
import { PlaneTakeoff, Plus } from 'lucide-react'
import Navbar from '../components/common/Navbar'
import Button from '../components/common/Button'
import Spinner from '../components/common/Spinner'
import TripCard from '../components/trip/TripCard'
import CreateTripModal from '../components/trip/CreateTripModal'
import useTripStore from '../store/tripStore'

export default function DashboardPage() {
  const [modalOpen, setModalOpen] = useState(false)
  const trips = useTripStore((s) => s.trips)
  const loading = useTripStore((s) => s.loading)
  const fetchMyTrips = useTripStore((s) => s.fetchMyTrips)

  useEffect(() => {
    fetchMyTrips()
  }, [fetchMyTrips])

  return (
    <div className="min-h-screen bg-[#0a0f1d] text-gray-100 relative overflow-hidden pb-12">
      {/* Background glow effects */}
      <div className="absolute top-10 left-1/4 w-80 h-80 bg-primary-600/10 rounded-full blur-3xl -z-10 pointer-events-none" />
      <div className="absolute bottom-10 right-1/4 w-80 h-80 bg-purple-600/10 rounded-full blur-3xl -z-10 pointer-events-none" />

      <Navbar dark={true} />
      <main className="pt-16 max-w-2xl mx-auto px-4 py-6 relative z-10">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-heading">My Trips</h1>
          <Button onClick={() => setModalOpen(true)}>
            <span className="inline-flex items-center gap-2">
              <Plus size={18} />
              New Trip
            </span>
          </Button>
        </div>

        {loading ? (
          <div className="flex justify-center py-16">
            <Spinner className="h-8 w-8" />
          </div>
        ) : trips.length === 0 ? (
          <div className="glass-card p-10 text-center space-y-4 hover:border-white/10">
            <PlaneTakeoff size={48} className="mx-auto text-primary-400 animate-bounce" />
            <p className="text-lg text-white font-bold">No trips yet</p>
            <p className="text-sm text-gray-400">
              Create your first trip to get started
            </p>
            <Button onClick={() => setModalOpen(true)} className="mx-auto mt-2">
              Create Trip
            </Button>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {trips.map((trip) => (
              <TripCard key={trip.tripId} trip={trip} />
            ))}
          </div>
        )}
      </main>
      <CreateTripModal isOpen={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  )
}

