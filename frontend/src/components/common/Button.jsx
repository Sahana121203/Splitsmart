import Spinner from './Spinner'

const variantClasses = {
  primary: 'btn-primary',
  secondary: 'btn-secondary',
  danger: 'btn-danger',
  warning: 'btn-warning',
}

export default function Button({
  children,
  onClick,
  type = 'button',
  variant = 'primary',
  loading = false,
  disabled = false,
  fullWidth = false,
  className = '',
}) {
  const isDisabled = disabled || loading
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={isDisabled}
      className={`transition-all duration-150 ${variantClasses[variant]} ${
        fullWidth ? 'w-full' : ''
      } ${className}`}
    >
      {loading ? (
        <span className="inline-flex items-center gap-2 justify-center">
          <Spinner />
          <span>Loading...</span>
        </span>
      ) : (
        children
      )}
    </button>
  )
}

