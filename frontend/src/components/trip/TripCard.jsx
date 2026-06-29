import { useNavigate } from 'react-router-dom'
import { Calendar, MapPin, Users } from 'lucide-react'
import TripStatusBadge from './TripStatusBadge'
import { formatDateRange } from '../../utils/format'

export default function TripCard({ trip }) {
  const navigate = useNavigate()

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={() => navigate(`/trips/${trip.tripId}`)}
      onKeyDown={(e) => {
        if (e.key === 'Enter') navigate(`/trips/${trip.tripId}`)
      }}
      className="glass-card cursor-pointer hover:scale-[1.01] hover:border-primary-500/40 hover:shadow-[0_0_15px_rgba(59,130,246,0.1)] transition-all duration-200"
    >
      <div className="flex justify-between items-start gap-3">
        <p className="font-semibold text-lg text-white">{trip.name}</p>
        <TripStatusBadge status={trip.status} dark={true} />
      </div>

      {trip.destination ? (
        <div className="flex items-center gap-1 mt-2">
          <MapPin size={14} className="text-primary-400 shrink-0" />
          <span className="text-sm text-gray-400">{trip.destination}</span>
        </div>
      ) : null}

      {trip.startDate ? (
        <div className="flex items-center gap-1 mt-1.5">
          <Calendar size={14} className="text-primary-400 shrink-0" />
          <span className="text-sm text-gray-400">
            {formatDateRange(trip.startDate, trip.endDate)}
          </span>
        </div>
      ) : null}

      <div className="border-t border-white/10 my-3" />

      <div className="flex justify-between items-center text-sm">
        <span className="flex items-center gap-1.5 text-gray-400">
          <Users size={14} className="text-primary-400" />
          {trip.memberCount} members
        </span>
        <span className="text-white font-semibold">
          Pool: <span className="text-primary-400">₹{(trip.kittyBalance ?? 0).toLocaleString('en-IN')}</span>
        </span>
      </div>
    </div>
  )
}

