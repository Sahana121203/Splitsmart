import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import toast from 'react-hot-toast'
import Modal from '../common/Modal'
import Input from '../common/Input'
import Button from '../common/Button'
import useTripStore from '../../store/tripStore'

const schema = z.object({
  phone: z.string().regex(/^[0-9]{10}$/, 'Phone must be 10 digits'),
  email: z.string().email().optional().or(z.literal('')),
  role: z.enum(['MEMBER', 'ADMIN', 'VIEWER']).default('MEMBER'),
})

const selectClass =
  'w-full h-11 rounded-md border border-gray-300 px-3 outline-none transition focus:ring-2 focus:ring-primary-100'

export default function InviteMemberModal({ isOpen, onClose, tripId }) {
  const inviteMember = useTripStore((s) => s.inviteMember)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { phone: '', email: '', role: 'MEMBER' },
  })

  const onSubmit = async (values) => {
    try {
      const payload = {
        phone: values.phone,
        role: values.role,
        ...(values.email ? { email: values.email } : {}),
      }
      const res = await inviteMember(tripId, payload)
      if (res.data?.lateJoinerWarning) {
        toast(res.data.lateJoinerWarning, { icon: 'ℹ️', duration: 6000 })
      }
      toast.success('Member invited!')
      reset()
      onClose()
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to invite member')
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Invite Member">
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <Input
          label="Phone Number"
          placeholder="10-digit phone number"
          register={register('phone')}
          error={errors.phone}
        />
        <Input
          label="Email"
          placeholder="Optional email"
          register={register('email')}
          error={errors.email}
        />
        <div className="space-y-1">
          <label className="block text-sm font-medium text-gray-700">Role</label>
          <select className={selectClass} {...register('role')}>
            <option value="MEMBER">MEMBER</option>
            <option value="ADMIN">ADMIN</option>
            <option value="VIEWER">VIEWER</option>
          </select>
        </div>
        <div className="flex justify-end gap-3 pt-2">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" loading={isSubmitting}>
            Invite
          </Button>
        </div>
      </form>
    </Modal>
  )
}
