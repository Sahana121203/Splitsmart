export default function ExpenseSummaryBar({ summary }) {
  if (!summary) return null

  const { totalAmount = 0, totalExpenses = 0, myTotalShare = 0 } = summary

  return (
    <div className="grid grid-cols-3 gap-3">
      <div className="glass-card-cyan p-3.5 text-center flex flex-col justify-center">
        <p className="text-[10px] uppercase tracking-wider text-cyan-300 font-bold mb-1">Total</p>
        <p className="font-extrabold text-lg text-white">
          ₹{totalAmount.toLocaleString('en-IN')}
        </p>
      </div>
      <div className="glass-card p-3.5 text-center flex flex-col justify-center">
        <p className="text-[10px] uppercase tracking-wider text-gray-300 font-bold mb-1">Expenses</p>
        <p className="font-extrabold text-lg text-white">{totalExpenses}</p>
      </div>
      <div className="glass-card-purple p-3.5 text-center flex flex-col justify-center">
        <p className="text-[10px] uppercase tracking-wider text-purple-300 font-bold mb-1">My Share</p>
        <p className="font-extrabold text-lg text-purple-400">
          ₹{myTotalShare.toLocaleString('en-IN')}
        </p>
      </div>
    </div>
  )
}


