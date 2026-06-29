import { useNavigate } from 'react-router-dom'
import { kittyBarColor } from '../../utils/format'

export default function KittyMiniCard({ kitty, tripId }) {
  const navigate = useNavigate()

  if (!kitty) {
    return (
      <div className="bg-white/5 border border-white/10 rounded-xl p-4 animate-pulse space-y-3">
        <div className="h-4 bg-white/10 rounded w-1/3" />
        <div className="h-2 bg-white/10 rounded w-full" />
        <div className="h-4 bg-white/10 rounded w-1/2" />
      </div>
    )
  }

  const percent = Math.min(kitty.kittyFundedPercent ?? 0, 100)

  return (
    <div className="bg-white/5 border border-white/10 backdrop-blur-md rounded-xl p-4 text-white shadow-lg transition hover:border-white/15 duration-200">
      <div className="flex justify-between items-center">
        <span className="font-semibold text-gray-300 text-sm tracking-wide">Kitty Pool</span>
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

      <div className="bg-white/10 rounded-full h-2 w-full mt-3 overflow-hidden">
        <div
          className={`${kittyBarColor(percent)} rounded-full h-2 transition-all duration-500`}
          style={{ width: `${percent}%` }}
        />
      </div>

      <div className="flex justify-between items-baseline mt-3">
        <span className="font-extrabold text-lg text-white">
          ₹{(kitty.kittyBalance ?? 0).toLocaleString('en-IN')}
        </span>
        <span className="text-xs text-gray-400 font-semibold">{percent}% funded</span>
      </div>

      <button
        type="button"
        onClick={() => navigate(`/trips/${tripId}/kitty`)}
        className="mt-4 w-full h-9 text-xs font-bold uppercase tracking-wider rounded-lg border border-white/10 bg-white/5 text-white hover:bg-white/10 transition cursor-pointer hover:scale-[1.02] active:scale-95 shadow-sm"
      >
        Add to Pool
      </button>
    </div>
  )
}

