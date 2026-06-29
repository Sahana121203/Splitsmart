import { ArrowRight } from 'lucide-react'

export default function TransferInstructionList({
  transferInstructions,
  mode,
}) {
  const transfers = transferInstructions || []
  const allSettled = mode === 'BALANCED' && transfers.length === 0

  return (
    <div>
      <h3 className="text-xs uppercase tracking-wider text-gray-400 font-bold mb-2.5 px-1">Transfers Needed</h3>

      {transfers.length === 0 ? (
        allSettled ? (
          <div className="bg-green-500/10 border border-green-500/20 rounded-2xl p-5 text-center">
            <p className="font-bold text-green-400">
              🎉 No transfers needed!
            </p>
            <p className="text-xs text-gray-400 mt-1 font-medium">
              Everyone is already settled.
            </p>
          </div>
        ) : (
          <div className="bg-amber-500/10 border border-amber-500/20 rounded-2xl p-5 text-center">
            <p className="font-bold text-amber-300">
              No transfer pairs could be calculated
            </p>
            <p className="text-xs text-gray-400 mt-1.5 leading-relaxed font-medium">
              Review member balances above — kitty contributions or external
              payments may need to be recorded.
            </p>
          </div>
        )
      ) : (
        transfers.map((t, i) => (
          <div
            key={`${t.fromUserId}-${t.toUserId}-${i}`}
            className="glass-card hover:border-white/10 p-4 mb-3"
          >
            <div className="flex items-center gap-3">
              <span className="font-semibold text-white">
                {t.fromUserName}
              </span>
              <ArrowRight className="text-primary-400 shrink-0" size={16} />
              <span className="font-semibold text-white">
                {t.toUserName}
              </span>
              <span className="ml-auto font-extrabold text-primary-400">
                ₹{t.amount?.toLocaleString('en-IN')}
              </span>
            </div>
            {t.description && (
              <p className="text-xs text-gray-400 mt-1.5 font-medium">{t.description}</p>
            )}
          </div>
        ))
      )}
    </div>
  )
}

