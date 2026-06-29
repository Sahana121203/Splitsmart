import { formatDateTime } from '../../utils/format'

export default function DepositHistory({ history = [] }) {
  return (
    <div>
      <h3 className="text-xs uppercase tracking-wider text-gray-400 font-bold mb-2 px-1">Deposit History</h3>
      {history.length === 0 ? (
        <p className="text-sm text-gray-400 font-medium text-center py-4">No deposits yet</p>
      ) : (
        <div className="bg-white/5 border border-white/10 rounded-2xl divide-y divide-white/10 overflow-hidden">
          {history.map((payment) => (
            <div
              key={payment.paymentId}
              className="flex justify-between items-center py-3 px-4 gap-3 hover:bg-white/[0.02] transition"
            >
              <div className="min-w-0">
                <p className="text-sm font-semibold text-white">
                  {payment.userName}
                </p>
                <span className="inline-flex px-1.5 py-0.5 text-[9px] font-bold uppercase tracking-wider rounded-md bg-white/5 text-gray-400 border border-white/10 mt-1">
                  {payment.method}
                </span>
                {payment.reference && (
                  <p className="text-[10px] text-gray-400 mt-0.5 truncate font-medium">
                    Ref: {payment.reference}
                  </p>
                )}
              </div>
              <div className="text-right shrink-0">
                <p className="text-sm font-extrabold text-green-400">
                  +₹{(payment.amount ?? 0).toLocaleString('en-IN')}
                </p>
                <p className="text-[10px] font-medium text-gray-400 mt-0.5">
                  {formatDateTime(payment.createdAt)}
                </p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

