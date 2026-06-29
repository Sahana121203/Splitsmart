import Badge from '../common/Badge'

export default function VoteStatusCard({
  voteStatus,
  isAdmin,
  onCloseVote,
}) {
  if (!voteStatus) return null

  const progress =
    voteStatus.memberCount > 0
      ? (voteStatus.voteCount / voteStatus.memberCount) * 100
      : 0

  return (
    <div className="glass-card hover:border-white/10 p-5 space-y-4">
      <h3 className="text-xs uppercase tracking-wider text-gray-400 font-bold mb-1">Vote Progress</h3>

      <p className="text-sm text-white font-medium">
        {voteStatus.voteCount} of {voteStatus.memberCount} members voted
      </p>

      <div className="bg-white/10 rounded-full h-2 w-full overflow-hidden">
        <div
          className="bg-primary-500 rounded-full h-2 transition-all duration-500"
          style={{ width: `${progress}%` }}
        />
      </div>

      <p className="text-xs text-gray-400 font-medium">
        {voteStatus.quorumRequired} votes needed for result
      </p>

      {voteStatus.message && (
        <p className="text-xs text-gray-400 italic mt-1 font-medium">{voteStatus.message}</p>
      )}

      <div className="flex gap-2 mt-2">
        {voteStatus.voteClosed && (
          <Badge variant="warning" dark={true}>Vote Closed</Badge>
        )}
        {voteStatus.quorumReached && !voteStatus.voteClosed && (
          <Badge variant="success" dark={true}>Quorum Reached</Badge>
        )}
      </div>

      {isAdmin && !voteStatus.voteClosed && (
        <button
          type="button"
          onClick={onCloseVote}
          className="w-full h-9 text-xs font-bold uppercase tracking-wider rounded-lg border border-white/10 bg-white/5 text-white hover:bg-white/10 transition cursor-pointer hover:scale-[1.02] active:scale-95 shadow-sm mt-2"
        >
          Close Vote Early
        </button>
      )}
    </div>
  )
}

