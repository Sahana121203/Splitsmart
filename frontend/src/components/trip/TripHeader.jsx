import { Settings } from 'lucide-react'
import TripStatusBadge from './TripStatusBadge'
import Button from '../common/Button'
import { formatDateRange } from '../../utils/format'

export default function TripHeader({ trip, isAdmin, onStatusChange, dark = false }) {
  if (!trip) return null

  return (
    <div className="space-y-2">
      <div className="flex justify-between items-start gap-3">
        <h1 className={`text-2xl font-bold ${dark ? 'text-white' : 'text-gray-900'}`}>{trip.name}</h1>
        <TripStatusBadge status={trip.status} dark={dark} />
      </div>
      {(trip.destination || trip.startDate) && (
        <p className={`text-sm ${dark ? 'text-gray-400' : 'text-gray-500'}`}>
          {[trip.destination, formatDateRange(trip.startDate, trip.endDate)]
            .filter(Boolean)
            .join(' · ')}
        </p>
      )}
      {isAdmin && trip.status !== 'SETTLED' && (
        <Button 
          variant="secondary" 
          onClick={onStatusChange}
          className={dark ? 'border-white/10 bg-white/5 text-white hover:bg-white/10 hover:border-white/20' : ''}
        >
          <span className="inline-flex items-center gap-2">
            <Settings size={16} />
            Manage Status
          </span>
        </Button>
      )}
    </div>
  )
}

