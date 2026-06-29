import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import Modal from '../common/Modal'
import Input from '../common/Input'
import Button from '../common/Button'
import useTripStore from '../../store/tripStore'

const schema = z.object({
  name: z.string().min(1, 'Trip name required'),
  destination: z.string().optional(),
  startDate: z.string().optional(),
  endDate: z.string().optional(),
  kittyTarget: z.coerce.number().min(0).optional().default(0),
  baseCurrency: z.string().default('INR'),
})

const selectClass =
  'w-full h-11 rounded-md border border-gray-300 px-3 outline-none transition focus:ring-2 focus:ring-primary-100'

export default function CreateTripModal({ isOpen, onClose }) {
  const navigate = useNavigate()
  const createTrip = useTripStore((s) => s.createTrip)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      name: '',
      destination: '',
      startDate: '',
      endDate: '',
      kittyTarget: 0,
      baseCurrency: 'INR',
    },
  })

  const onSubmit = async (values) => {
    try {
      const payload = {
        ...values,
        destination: values.destination || undefined,
        startDate: values.startDate || undefined,
        endDate: values.endDate || undefined,
      }
      const newTrip = await createTrip(payload)
      toast.success('Trip created!')
      reset()
      onClose()
      navigate(`/trips/${newTrip.tripId}`)
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to create trip')
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create New Trip">
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <Input
          label="Trip Name"
          placeholder="e.g. Goa Trip 2025"
          register={register('name')}
          error={errors.name}
        />
        <Input
          label="Destination"
          placeholder="e.g. Goa"
          register={register('destination')}
          error={errors.destination}
        />
        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Start Date"
            type="date"
            register={register('startDate')}
            error={errors.startDate}
          />
          <Input
            label="End Date"
            type="date"
            register={register('endDate')}
            error={errors.endDate}
          />
        </div>
        <div className="space-y-1">
          <Input
            label="Kitty Target (₹)"
            type="number"
            placeholder="0"
            register={register('kittyTarget')}
            error={errors.kittyTarget}
          />
          <p className="text-xs text-gray-400">
            Estimated total budget for the trip
          </p>
        </div>
        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700">
            Base Currency
          </label>
          <select className={selectClass} {...register('baseCurrency')}>
            <option value="INR">INR</option>
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
            <option value="GBP">GBP</option>
          </select>
        </div>
        <div className="flex justify-end gap-3 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={isSubmitting}>
            Create Trip
          </Button>
        </div>
      </form>
    </Modal>
  )
}
