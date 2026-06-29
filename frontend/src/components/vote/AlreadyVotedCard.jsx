import { CheckCircle } from 'lucide-react'

export default function AlreadyVotedCard({ voteStatus }) {
  if (!voteStatus) return null

  const remaining = voteStatus.quorumRequired - voteStatus.voteCount

  return (
    <div className="bg-green-500/10 border border-green-500/20 rounded-2xl p-5 mb-4 shadow-sm shadow-green-500/5">
      <div className="flex items-center gap-2">
        <CheckCircle size={20} className="text-green-400" />
        <p className="font-bold text-green-400 text-sm">You have voted!</p>
      </div>
      {remaining > 0 && (
        <p className="text-xs text-gray-400 font-medium mt-1.5 ml-7">
          Waiting for {remaining} more members to vote...
        </p>
      )}
    </div>
  )
}

