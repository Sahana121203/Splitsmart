export default function Spinner({ className = '' }) {
  return (
    <div
      className={`h-4 w-4 border-2 border-current border-t-transparent rounded-full animate-spin ${className}`}
    />
  )
}
