import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import toast from 'react-hot-toast'
import Input from '../common/Input'
import Button from '../common/Button'
import useTripStore from '../../store/tripStore'

const schema = z.object({
  amount: z.coerce.number().min(1, 'Minimum amount is ₹1'),
  method: z.enum(['UPI', 'CARD', 'MANUAL']),
  reference: z.string().optional(),
  note: z.string().optional(),
})

const selectClass =
  'w-full h-11 rounded-xl border border-white/10 bg-[#0d1425] text-white px-3 outline-none transition focus:border-primary-500/50 focus:shadow-[0_0_10px_rgba(59,130,246,0.15)] text-sm cursor-pointer'

export default function DepositForm({ tripId, onDeposited }) {
  const deposit = useTripStore((s) => s.deposit)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      amount: '',
      method: 'UPI',
      reference: '',
      note: '',
    },
  })

  const onSubmit = async (values) => {
    try {
      await deposit(tripId, {
        amount: values.amount,
        method: values.method,
        reference: values.reference || undefined,
        note: values.note || undefined,
      })
      toast.success(`₹${values.amount} added to kitty!`)
      reset({ amount: '', method: 'UPI', reference: '', note: '' })
      onDeposited?.()
    } catch (err) {
      toast.error(err.response?.data?.error || 'Deposit failed')
    }
  }

  return (
    <div className="glass-card hover:border-white/10 p-5 space-y-4">
      <h3 className="text-xs uppercase tracking-wider text-gray-400 font-bold mb-2">Add to Pool</h3>
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <Input
          label="Amount (₹)"
          type="number"
          placeholder="e.g. 8000"
          register={register('amount')}
          error={errors.amount}
        />
        <div className="space-y-1.5">
          <label className="block text-xs font-bold uppercase tracking-wider text-gray-400">Method</label>
          <select className={selectClass} {...register('method')}>
            <option value="UPI" className="bg-[#0d1425] text-white">UPI</option>
            <option value="CARD" className="bg-[#0d1425] text-white">CARD</option>
            <option value="MANUAL" className="bg-[#0d1425] text-white">MANUAL</option>
          </select>
        </div>
        <Input
          label="Reference"
          placeholder="UPI transaction ID"
          register={register('reference')}
          error={errors.reference}
        />
        <Input
          label="Note"
          placeholder="Optional note"
          register={register('note')}
          error={errors.note}
        />
        <Button type="submit" fullWidth loading={isSubmitting}>
          Deposit
        </Button>
      </form>
    </div>
  )
}

