import { Check } from 'lucide-react'

export default function MemberContributions({ memberContributions = [] }) {
  return (
    <div>
      <h3 className="text-xs uppercase tracking-wider text-gray-400 font-bold mb-2 px-1">Member Contributions</h3>
      <div className="bg-white/5 border border-white/10 rounded-2xl divide-y divide-white/10 overflow-hidden">
        {memberContributions.length === 0 ? (
          <p className="p-4 text-sm text-gray-400 font-medium text-center">No contributions yet</p>
        ) : (
          memberContributions.map((member) => {
            const sharePercent =
              member.expectedShare > 0
                ? Math.min(
                    (member.contributed / member.expectedShare) * 100,
                    100
                  )
                : 0
            return (
              <div
                key={member.userId}
                className="flex items-center gap-3 py-3 px-4 hover:bg-white/[0.02] transition"
              >
                <div className="w-8 h-8 rounded-full bg-primary-900/30 border border-primary-500/30 flex items-center justify-center text-primary-400 text-sm font-semibold shrink-0">
                  {(member.name || '?').charAt(0).toUpperCase()}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-white truncate">
                    {member.name}
                  </p>
                  <div className="bg-white/10 rounded-full h-1.5 w-full mt-1.5 overflow-hidden">
                    <div
                      className="bg-primary-500 rounded-full h-1.5 transition-all duration-300"
                      style={{ width: `${sharePercent}%` }}
                    />
                  </div>
                </div>
                <div className="text-right shrink-0">
                  <p className="text-sm font-extrabold text-white">
                    ₹{(member.contributed ?? 0).toLocaleString('en-IN')}
                  </p>
                  {member.remainingShare > 0 && (
                    <p className="text-[10px] font-bold text-red-400 uppercase tracking-wider mt-0.5">
                      ₹{member.remainingShare.toLocaleString('en-IN')} remaining
                    </p>
                  )}
                  {member.fullyContributed && (
                    <div className="flex justify-end mt-0.5">
                      <Check size={14} className="text-green-400 shadow-[0_0_8px_rgba(34,197,94,0.3)] shrink-0" />
                    </div>
                  )}
                </div>
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}
