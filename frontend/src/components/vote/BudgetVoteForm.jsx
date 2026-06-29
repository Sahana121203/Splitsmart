import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import toast from 'react-hot-toast'
import Input from '../common/Input'
import Button from '../common/Button'
import useTripStore from '../../store/tripStore'

const schema = z.object({
  maxBudget: z.coerce.number().min(1, 'Enter a valid budget'),
})

export default function BudgetVoteForm({ tripId, onVoteSubmitted }) {
  const submitVote = useTripStore((s) => s.submitVote)
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { maxBudget: '' },
  })

  const onSubmit = async (values) => {
    try {
      await submitVote(tripId, values.maxBudget)
      toast.success('Your vote has been submitted!')
      reset()
      onVoteSubmitted?.()
    } catch (err) {
      toast.error(err.response?.data?.error || 'Failed to submit vote')
    }
  }

  return (
    <div className="bg-blue-500/10 border border-blue-500/20 rounded-2xl p-5 space-y-4">
      <div>
        <h3 className="text-xs uppercase tracking-wider text-blue-300 font-bold mb-1">Submit Your Budget</h3>
        <p className="text-xs text-gray-400 font-medium leading-relaxed">
          Your vote is completely anonymous. No one will see your individual amount.
        </p>
      </div>
      <form className="space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <Input
          label="Your maximum budget (₹)"
          type="number"
          placeholder="e.g. 15000"
          register={register('maxBudget')}
          error={errors.maxBudget}
        />
        <Button type="submit" loading={isSubmitting} fullWidth>
          Submit My Vote
        </Button>
      </form>
    </div>
  )
}

