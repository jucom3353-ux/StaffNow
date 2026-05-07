import clsx from 'clsx'
import { STATUS_CONFIG } from '../../constants/statusConfig'

export default function StatusBadge({ status, size = 'md' }) {
  const config = STATUS_CONFIG[status]
  if (!config) return null

  return (
    <span
      className={clsx(
        'inline-flex items-center rounded-full font-medium',
        config.bg,
        config.text,
        size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-0.5 text-xs'
      )}
    >
      {config.label}
    </span>
  )
}
