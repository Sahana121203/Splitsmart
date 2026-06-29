export default function MemberBalanceList({ memberBalances }) {
  if (!memberBalances?.length) return null

  return (
    <div className="space-y-2">
      <h3 className="text-xs uppercase tracking-wider text-gray-400 font-bold px-1">Member Balances</h3>
      <div className="bg-white/5 border border-white/10 rounded-2xl divide-y divide-white/10 overflow-hidden">
        {memberBalances.map((member) => {
          const balance = member.finalBalance ?? 0
          const role = member.role

          let badgeClass = 'bg-white/5 text-gray-400 border border-white/10'
          let badgeLabel = 'settled'
          let amountClass = 'text-gray-400 font-bold'
          let amountText = '₹0'

          if (role === 'CREDITOR') {
            badgeClass = 'bg-green-500/10 text-green-400 border border-green-500/20 shadow-[0_0_8px_rgba(34,197,94,0.1)]'
            badgeLabel = 'gets back'
            amountClass = 'text-green-400 font-extrabold'
            amountText = `₹${balance.toLocaleString('en-IN')}`
          } else if (role === 'DEBTOR') {
            badgeClass = 'bg-red-500/10 text-red-400 border border-red-500/20 shadow-[0_0_8px_rgba(239,68,68,0.1)]'
            badgeLabel = 'owes'
            amountClass = 'text-red-400 font-extrabold'
            amountText = `₹${Math.abs(balance).toLocaleString('en-IN')}`
          }

          return (
            <div
              key={member.userId}
              className="flex items-center justify-between py-3.5 px-4 hover:bg-white/[0.02] transition"
            >
              <div className="flex items-center gap-3">
                <div className="h-8 w-8 rounded-full bg-primary-900/30 border border-primary-500/30 text-primary-400 flex items-center justify-center text-sm font-semibold">
                  {member.userName?.charAt(0)?.toUpperCase()}
                </div>
                <span className="font-semibold text-white">
                  {member.userName}
                </span>
              </div>
              <div className="flex items-center gap-3">
                <span
                  className={`text-[9px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full ${badgeClass}`}
                >
                  {badgeLabel}
                </span>
                <span className={`text-sm ${amountClass}`}>
                  {amountText}
                </span>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

