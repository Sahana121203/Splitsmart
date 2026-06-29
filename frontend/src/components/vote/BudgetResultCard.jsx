export default function BudgetResultCard({ voteResult }) {
  if (!voteResult) return null

  return (
    <div className="glass-card border-primary-500/30 p-6 mb-4 shadow-[0_0_20px_rgba(59,130,246,0.1)] hover:border-primary-500/40">
      <p className="text-[10px] uppercase tracking-wider text-primary-400 font-bold mb-2">
        Group Budget Range
      </p>
      <p className="text-3xl font-extrabold text-white">
        ₹{voteResult.rangeMin.toLocaleString('en-IN')}
        {' – '}
        ₹{voteResult.rangeMax.toLocaleString('en-IN')}
      </p>
      {voteResult.suggestion && (
        <p className="text-xs text-gray-400 mt-3.5 italic font-medium border-t border-white/5 pt-3">
          * {voteResult.suggestion}
        </p>
      )}
      <div className="flex gap-3 mt-4">
        <div className="flex-1 bg-white/5 border border-white/10 rounded-xl p-3.5 text-center">
          <p className="text-[10px] uppercase tracking-wider text-gray-400 font-bold mb-1">Median</p>
          <p className="font-extrabold text-white">
            ₹{(voteResult.medianBudget ?? 0).toLocaleString('en-IN')}
          </p>
        </div>
        <div className="flex-1 bg-white/5 border border-white/10 rounded-xl p-3.5 text-center">
          <p className="text-[10px] uppercase tracking-wider text-gray-400 font-bold mb-1">Votes</p>
          <p className="font-extrabold text-white">{voteResult.voteCount}</p>
        </div>
        <div className="flex-1 bg-white/5 border border-white/10 rounded-xl p-3.5 text-center">
          <p className="text-[10px] uppercase tracking-wider text-gray-400 font-bold mb-1">Average</p>
          <p className="font-extrabold text-white">
            ₹{(voteResult.avgBudget ?? 0).toLocaleString('en-IN')}
          </p>
        </div>
      </div>
    </div>
  )
}

