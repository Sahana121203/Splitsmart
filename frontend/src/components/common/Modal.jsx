import { X } from 'lucide-react'

export default function Modal({ isOpen, onClose, title, children }) {
  if (!isOpen) return null

  return (
    <div
      className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4 transition-all"
      onClick={onClose}
    >
      <div
        className="w-full max-w-md bg-[#0d1425] rounded-2xl shadow-2xl border border-white/10 relative overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-white/10">
          <h3 className="font-bold text-white text-base">{title}</h3>
          <button type="button" onClick={onClose} className="p-1.5 rounded-lg text-gray-400 hover:bg-white/10 hover:text-white transition cursor-pointer">
            <X size={18} />
          </button>
        </div>
        <div className="p-5 text-gray-300">{children}</div>
      </div>
    </div>
  )
}

