import {
  AlertCircle,
  ArrowDownCircle,
  CheckCircle,
} from 'lucide-react'

const MODE_CONFIG = {
  BALANCED: {
    bg: 'bg-green-500/10 border-green-500/20 shadow-[0_0_12px_rgba(34,197,94,0.05)]',
    icon: CheckCircle,
    iconColor: 'text-green-400',
    titleColor: 'text-white',
    label: 'All Settled',
    messageColor: 'text-green-300',
  },
  REFUND: {
    bg: 'bg-blue-500/10 border-blue-500/20 shadow-[0_0_12px_rgba(59,130,246,0.05)]',
    icon: ArrowDownCircle,
    iconColor: 'text-blue-400',
    titleColor: 'text-white',
    label: 'Refund Mode',
    messageColor: 'text-blue-300',
  },
  DEFICIT: {
    bg: 'bg-amber-500/10 border-amber-500/20 shadow-[0_0_12px_rgba(245,158,11,0.05)]',
    icon: AlertCircle,
    iconColor: 'text-amber-400',
    titleColor: 'text-white',
    label: 'Deficit Mode',
    messageColor: 'text-amber-300',
  },
}

export default function SettlementModeCard({ settlement }) {
  if (!settlement) return null

  const config = MODE_CONFIG[settlement.mode] || MODE_CONFIG.BALANCED
  const Icon = config.icon
  const surplus = settlement.surplus ?? 0

  return (
    <div className={`rounded-2xl p-5 border ${config.bg}`}>
      <div className="flex items-center gap-2">
        <Icon className={config.iconColor} size={22} />
        <h2 className={`font-bold text-lg ${config.titleColor}`}>
          {config.label}
        </h2>
      </div>

      <div className="grid grid-cols-3 gap-3 mt-4 pt-3 border-t border-white/5">
        <div className="text-center">
          <p className="text-[10px] uppercase tracking-wider text-gray-400 font-bold">Contributed</p>
          <p className="font-extrabold text-white mt-0.5">
            ₹{(settlement.totalContribution ?? 0).toLocaleString('en-IN')}
          </p>
        </div>
        <div className="text-center">
          <p className="text-[10px] uppercase tracking-wider text-gray-400 font-bold">Consumed</p>
          <p className="font-extrabold text-white mt-0.5">
            ₹{(settlement.totalConsumption ?? 0).toLocaleString('en-IN')}
          </p>
        </div>
        <div className="text-center">
          <p className="text-[10px] uppercase tracking-wider text-gray-400 font-bold">Surplus</p>
          <p
            className={`font-extrabold mt-0.5 ${
              surplus >= 0 ? 'text-green-400' : 'text-red-400'
            }`}
          >
            ₹{surplus.toLocaleString('en-IN')}
          </p>
        </div>
      </div>

      {settlement.statusMessage && (
        <p className={`text-xs italic mt-3.5 font-medium ${config.messageColor}`}>
          * {settlement.statusMessage}
        </p>
      )}
    </div>
  )
}

