import toast from 'react-hot-toast'
import Modal from '../common/Modal'
import Button from '../common/Button'
import TripStatusBadge from './TripStatusBadge'
import useTripStore from '../../store/tripStore'

export default function StatusChangeModal({ isOpen, onClose, trip }) {
  const updateStatus = useTripStore((s) => s.updateStatus)

  if (!trip) return null

  const handleStatusChange = async (newStatus) => {
    try {
      await updateStatus(trip.tripId, newStatus)
      toast.success('Status updated!')
      onClose()
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to update status')
    }
  }

  const renderAction = () => {
    switch (trip.status) {
      case 'PLANNING':
        return (
          <div className="space-y-2">
            <Button onClick={() => handleStatusChange('ACTIVE')}>
              Activate Trip
            </Button>
            <p className="text-xs text-gray-500">
              Kitty must be 80% funded first
            </p>
          </div>
        )
      case 'ACTIVE':
        return (
          <div className="space-y-2">
            <Button variant="warning" onClick={() => handleStatusChange('FROZEN')}>
              Freeze Trip
            </Button>
            <p className="text-xs text-gray-500">
              No new expenses after freezing
            </p>
          </div>
        )
      case 'FROZEN':
        return (
          <p className="text-sm text-gray-600">
            Go to Settlement page to finalise
          </p>
        )
      case 'SETTLED':
        return (
          <p className="text-sm text-green-600">
            This trip is fully settled
          </p>
        )
      default:
        return null
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Manage Trip Status">
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-600">Current status:</span>
          <TripStatusBadge status={trip.status} />
        </div>
        {renderAction()}
      </div>
    </Modal>
  )
}
