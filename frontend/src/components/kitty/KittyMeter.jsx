import { CheckCircle } from 'lucide-react'
import { kittyBarColor } from '../../utils/format'

export default function KittyMeter({ kitty }) {
  if (!kitty) return null

  const percent = Math.min(kitty.kittyFundedPercent ?? 0, 100)

  return (
    <div className="glass-card hover:border-white/10 p-6 space-y-5">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold text-white">Kitty Pool</h2>
        <span
          className={`text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider ${
            kitty.readyToActivate
              ? 'bg-green-500/10 text-green-400 border border-green-500/20'
              : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
          }`}
        >
          {kitty.readyToActivate ? 'Ready' : 'Not Ready'}
        </span>
      </div>

      <div>
        <p className="text-3xl font-extrabold text-white">
          ₹{(kitty.kittyBalance ?? 0).toLocaleString('en-IN')}
        </p>
        {kitty.kittyTarget > 0 && (
          <p className="text-xs font-bold uppercase tracking-wider text-gray-400 mt-1">
            Target: <span className="text-white">₹{kitty.kittyTarget.toLocaleString('en-IN')}</span>
          </p>
        )}
      </div>

      <div className="bg-white/10 rounded-full h-3 w-full overflow-hidden">
        <div
          className={`${kittyBarColor(percent)} rounded-full h-3 transition-all duration-500`}
          style={{ width: `${percent}%` }}
        />
      </div>

      <p className="text-center text-xs font-bold uppercase tracking-wider text-gray-400">{percent}% funded</p>

      {kitty.readyToActivate && (
        <div className="flex items-center gap-2 bg-green-500/10 border border-green-500/20 rounded-xl p-3.5 text-sm text-green-400">
          <CheckCircle size={18} className="shrink-0" />
          <span>Kitty is funded! You can now activate the trip.</span>
        </div>
      )}
    </div>
  )
}

