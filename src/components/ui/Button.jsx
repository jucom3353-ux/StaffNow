import clsx from 'clsx'
import { Link } from 'react-router-dom'
import LoadingSpinner from './LoadingSpinner'

const variants = {
  primary:   'bg-orange text-white hover:bg-orange-600 active:bg-orange-700',
  secondary: 'bg-white text-navy border border-navy-100 hover:bg-offwhite active:bg-offwhite-200',
  ghost:     'bg-transparent text-navy hover:bg-offwhite-100 active:bg-offwhite-200',
  danger:    'bg-red-600 text-white hover:bg-red-700 active:bg-red-800',
  'danger-ghost': 'bg-transparent text-red-500 hover:bg-red-50 active:bg-red-100',
}

const sizes = {
  sm: 'px-3 py-1.5 text-sm gap-1.5',
  md: 'px-4 py-2 text-sm gap-2',
  lg: 'px-5 py-2.5 text-base gap-2',
}

export default function Button({
  variant = 'primary',
  size = 'md',
  icon: Icon,
  iconPosition = 'left',
  loading = false,
  disabled = false,
  children,
  onClick,
  type = 'button',
  as,
  to,
  className,
}) {
  const base = clsx(
    'inline-flex items-center justify-center font-medium rounded-lg transition-colors duration-150 cursor-pointer select-none whitespace-nowrap shrink-0',
    variants[variant],
    sizes[size],
    (disabled || loading) && 'opacity-50 cursor-not-allowed pointer-events-none',
    className
  )

  const content = (
    <>
      {loading && <LoadingSpinner size="sm" />}
      {!loading && Icon && iconPosition === 'left' && <Icon size={16} />}
      {children}
      {!loading && Icon && iconPosition === 'right' && <Icon size={16} />}
    </>
  )

  if (as === Link && to) {
    return <Link to={to} className={base}>{content}</Link>
  }

  return (
    <button type={type} className={base} onClick={onClick} disabled={disabled || loading}>
      {content}
    </button>
  )
}
