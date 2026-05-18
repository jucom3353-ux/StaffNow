import clsx from 'clsx'

const sizes = {
  sm: 'w-4 h-4 border-2',
  md: 'w-6 h-6 border-2',
  lg: 'w-8 h-8 border-3',
}

export default function LoadingSpinner({ size = 'md', className }) {
  return (
    <div
      className={clsx(
        'rounded-full border-navy-200 border-t-navy animate-spin',
        sizes[size],
        className
      )}
    />
  )
}
