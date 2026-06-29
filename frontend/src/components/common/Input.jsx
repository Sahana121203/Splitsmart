export default function Input({
  label,
  error,
  register,
  type = 'text',
  placeholder = '',
}) {
  return (
    <div className="space-y-1.5">
      {label && (
        <label className="block text-xs font-bold uppercase tracking-wider text-gray-400">
          {label}
        </label>
      )}
      <input
        type={type}
        placeholder={placeholder}
        {...register}
        className={`w-full h-11 rounded-xl border px-3 outline-none transition-all duration-150 text-sm ${
          error
            ? 'border-red-500/50 bg-red-500/5 text-red-100 placeholder-red-300/40 focus:border-red-500/80 focus:shadow-[0_0_10px_rgba(239,68,68,0.2)]'
            : 'border-white/10 bg-white/5 text-white placeholder-gray-500 focus:border-primary-500/50 focus:shadow-[0_0_10px_rgba(59,130,246,0.15)]'
        }`}
      />
      {error ? <p className="text-xs text-red-400 mt-0.5">{error.message}</p> : null}
    </div>
  )
}

