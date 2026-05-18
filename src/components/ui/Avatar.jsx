import clsx from 'clsx'

const sizes = {
  sm: 'w-7 h-7 text-xs',
  md: 'w-9 h-9 text-sm',
  lg: 'w-11 h-11 text-base',
}

export default function Avatar({ initials, src, size = 'md', className }) {
  if (src) {
    return (
      <img
        src={src}
        alt={initials}
        className={clsx('rounded-full object-cover', sizes[size], className)}
      />
    )
  }
  return (
    <div
      className={clsx(
        'rounded-full bg-navy text-white font-semibold flex items-center justify-center select-none',
        sizes[size],
        className
      )}
    >
      {initials?.slice(0, 2)}
    </div>
  )
}
