import { useState } from 'react'
import { UserPlus } from 'lucide-react'
import InviteMemberModal from './InviteMemberModal'

export default function MemberList({
  members = [],
  isAdmin,
  tripId,
  tripStatus,
}) {
  const [inviteOpen, setInviteOpen] = useState(false)

  // Sort members by netBalance descending to make it a leaderboard
  const sortedMembers = [...members].sort((a, b) => (b.netBalance ?? 0) - (a.netBalance ?? 0))

  const rank1 = sortedMembers[0]
  const rank2 = sortedMembers[1]
  const rank3 = sortedMembers[2]

  return (
    <div className="space-y-4">
      {/* Title Header */}
      <div className="flex justify-between items-center mb-1">
        <div>
          <h3 className="font-bold text-white text-lg flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-green-500 animate-pulse shadow-[0_0_8px_#22c55e]" />
            Trip Standings
          </h3>
          <p className="text-xs text-gray-400 mt-0.5">{members.length} participants</p>
        </div>
        {isAdmin && tripStatus === 'PLANNING' && (
          <button
            type="button"
            onClick={() => setInviteOpen(true)}
            className="inline-flex items-center gap-1.5 h-9 px-3.5 text-sm font-semibold rounded-lg border border-white/10 bg-white/5 text-white hover:bg-white/10 transition-all cursor-pointer shadow-sm hover:scale-[1.02]"
          >
            <UserPlus size={14} />
            Invite
          </button>
        )}
      </div>

      {/* Top 3 Podium (Only render if there are members) */}
      {sortedMembers.length > 0 && (
        <div className="grid grid-cols-3 gap-3 items-end py-4 border-b border-white/10 bg-white/[0.02] rounded-2xl p-4">
          {/* Rank 2 (Left) */}
          {rank2 ? (
            <div className="flex flex-col items-center p-3 rounded-xl bg-white/5 border border-slate-400/30 shadow-[0_0_15px_rgba(148,163,184,0.08)] hover:scale-[1.03] transition-transform duration-200 text-center h-[160px] justify-end relative">
              <div className="absolute top-2 right-2 text-[9px] font-black text-slate-400 bg-slate-400/10 px-2 py-0.5 rounded-full border border-slate-400/20">
                2ND
              </div>
              <div className="w-11 h-11 rounded-full bg-slate-400/20 border-2 border-slate-400 flex items-center justify-center text-slate-300 font-bold shadow-[0_0_10px_rgba(148,163,184,0.2)] mb-2 shrink-0">
                {(rank2.name || '?').charAt(0).toUpperCase()}
              </div>
              <p className="text-xs font-semibold text-white truncate w-full px-1">{rank2.name}</p>
              <p className="text-xs font-bold text-gray-200 mt-1">
                ₹{Math.abs(rank2.netBalance ?? 0).toLocaleString('en-IN')}
              </p>
              <span className={`text-[8px] font-bold uppercase tracking-wider mt-0.5 ${rank2.netBalance >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                {rank2.netBalance >= 0 ? 'surplus' : 'owes'}
              </span>
            </div>
          ) : (
            <div className="h-[160px] flex items-center justify-center text-gray-600 text-xs border border-dashed border-white/5 rounded-xl bg-white/[0.01]">
              Empty
            </div>
          )}

          {/* Rank 1 (Center) */}
          {rank1 ? (
            <div className="flex flex-col items-center p-4 rounded-xl bg-white/5 border border-yellow-500/40 shadow-[0_0_25px_rgba(234,179,8,0.12)] hover:scale-[1.05] transition-transform duration-200 text-center h-[185px] justify-end relative z-10">
              <div className="absolute -top-3 flex items-center gap-0.5 bg-gradient-to-r from-yellow-500 to-amber-500 text-black text-[9px] font-black px-2.5 py-0.5 rounded-full border border-yellow-400 shadow-[0_0_10px_rgba(234,179,8,0.3)] uppercase tracking-wider">
                👑 1ST
              </div>
              <div className="w-13 h-13 rounded-full bg-yellow-500/20 border-2 border-yellow-500 flex items-center justify-center text-yellow-400 font-black shadow-[0_0_15px_rgba(234,179,8,0.3)] mb-2 shrink-0">
                {(rank1.name || '?').charAt(0).toUpperCase()}
              </div>
              <p className="text-sm font-bold text-white truncate w-full px-1">{rank1.name}</p>
              <p className="text-sm font-extrabold text-white mt-1">
                ₹{Math.abs(rank1.netBalance ?? 0).toLocaleString('en-IN')}
              </p>
              <span className={`text-[9px] font-bold uppercase tracking-wider mt-0.5 ${rank1.netBalance >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                {rank1.netBalance >= 0 ? 'surplus' : 'owes'}
              </span>
            </div>
          ) : (
            <div className="h-[185px] flex items-center justify-center text-gray-600 text-xs border border-dashed border-white/5 rounded-xl bg-white/[0.01]" />
          )}

          {/* Rank 3 (Right) */}
          {rank3 ? (
            <div className="flex flex-col items-center p-3 rounded-xl bg-white/5 border border-amber-700/30 shadow-[0_0_15px_rgba(180,83,9,0.08)] hover:scale-[1.03] transition-transform duration-200 text-center h-[150px] justify-end relative">
              <div className="absolute top-2 left-2 text-[9px] font-bold text-amber-500 bg-amber-700/10 px-2 py-0.5 rounded-full border border-amber-700/20">
                3RD
              </div>
              <div className="w-10 h-10 rounded-full bg-amber-700/20 border-2 border-amber-700 flex items-center justify-center text-amber-600 font-bold shadow-[0_0_10px_rgba(180,83,9,0.2)] mb-2 shrink-0">
                {(rank3.name || '?').charAt(0).toUpperCase()}
              </div>
              <p className="text-xs font-semibold text-white truncate w-full px-1">{rank3.name}</p>
              <p className="text-xs font-bold text-gray-200 mt-1">
                ₹{Math.abs(rank3.netBalance ?? 0).toLocaleString('en-IN')}
              </p>
              <span className={`text-[8px] font-bold uppercase tracking-wider mt-0.5 ${rank3.netBalance >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                {rank3.netBalance >= 0 ? 'surplus' : 'owes'}
              </span>
            </div>
          ) : (
            <div className="h-[150px] flex items-center justify-center text-gray-600 text-xs border border-dashed border-white/5 rounded-xl bg-white/[0.01]">
              Empty
            </div>
          )}
        </div>
      )}

      {/* Standings List/Table */}
      <div className="space-y-2">
        <p className="text-[10px] uppercase tracking-wider text-gray-400 font-bold px-1 mb-2">Full Standings</p>
        <div className="flex flex-col gap-2">
          {sortedMembers.map((member, index) => {
            let rankBadgeClass = 'bg-white/5 text-gray-400 border border-white/10'
            if (index === 0) rankBadgeClass = 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30 font-black'
            else if (index === 1) rankBadgeClass = 'bg-slate-400/20 text-slate-300 border border-slate-400/30 font-bold'
            else if (index === 2) rankBadgeClass = 'bg-amber-700/20 text-amber-500 border border-amber-700/30 font-bold'

            let dotClass = 'bg-gray-500 shadow-[0_0_8px_#6b7280]'
            if (member.netBalance > 0) dotClass = 'bg-green-500 shadow-[0_0_8px_#22c55e]'
            else if (member.netBalance < 0) dotClass = 'bg-red-500 shadow-[0_0_8px_#ef4444]'

            return (
              <div
                key={member.memberId || member.userId}
                className="flex items-center gap-3 py-3 px-4 rounded-xl bg-white/5 border border-white/10 hover:bg-white/10 transition duration-150 hover:scale-[1.01] shadow-sm"
              >
                {/* Rank Number */}
                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs shrink-0 ${rankBadgeClass}`}>
                  {index + 1}
                </div>

                {/* Avatar initial */}
                <div className="w-8 h-8 rounded-full bg-primary-900/30 border border-primary-500/30 flex items-center justify-center text-primary-400 text-sm font-semibold shrink-0">
                  {(member.name || '?').charAt(0).toUpperCase()}
                </div>

                {/* Participant name and role */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2">
                    <p className="text-sm font-semibold text-white truncate">
                      {member.name}
                    </p>
                    <span
                      className={`inline-flex px-1.5 py-0.5 text-[9px] font-bold uppercase tracking-wider rounded-md ${
                        member.role === 'ADMIN'
                          ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                          : 'bg-white/5 text-gray-400'
                      }`}
                    >
                      {member.role}
                    </span>
                  </div>
                  <div className="flex items-center gap-1.5 mt-0.5">
                    {/* Status Dot */}
                    <span className={`w-1.5 h-1.5 rounded-full ${dotClass}`} />
                    <span className="text-[10px] text-gray-400 uppercase tracking-wider">
                      {member.netBalance > 0 ? 'surplus' : member.netBalance < 0 ? 'owes' : 'settled'}
                    </span>
                  </div>
                </div>

                {/* Net balance (points/metric) */}
                <div className="text-right shrink-0">
                  <p
                    className={`text-sm font-bold ${
                      member.netBalance >= 0 ? 'text-green-400' : 'text-red-400'
                    }`}
                  >
                    ₹{Math.abs(member.netBalance ?? 0).toLocaleString('en-IN')}
                  </p>
                </div>
              </div>
            )
          })}
        </div>
      </div>

      <InviteMemberModal
        isOpen={inviteOpen}
        onClose={() => setInviteOpen(false)}
        tripId={tripId}
      />
    </div>
  )
}

