import { useState, useRef, useEffect } from 'react'
import { ChevronDown } from 'lucide-react'
import clsx from 'clsx'
import { STATUS_CONFIG } from '../../constants/statusConfig'

export default function StatusSelector({ status, options, onChange, size = 'md' }) {
  const [open, setOpen] = useState(false)
  const ref = useRef(null)

  useEffect(() => {
    if (!open) return
    const handler = (e) => { if (!ref.current?.contains(e.target)) setOpen(false) }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [open])

  const config = STATUS_CONFIG[status]
  if (!config) return null

  return (
    <div className="relative inline-block" ref={ref}>
      <button
        type="button"
        onClick={e => { e.preventDefault(); e.stopPropagation(); setOpen(o => !o) }}
        className={clsx(
          'inline-flex items-center gap-1 rounded-full font-medium transition-all',
          'hover:ring-2 hover:ring-offset-1 hover:ring-current/30 cursor-pointer',
          config.bg,
          config.text,
          size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-2.5 py-0.5 text-xs'
        )}
      >
        {config.label}
        <ChevronDown size={10} className={clsx('transition-transform', open && 'rotate-180')} />
      </button>

      {open && (
        <div className="absolute left-0 top-full mt-1 z-30 bg-white border border-offwhite-200 rounded-xl shadow-lg py-1 min-w-[110px]">
          {options.map(key => {
            const cfg = STATUS_CONFIG[key]
            if (!cfg) return null
            return (
              <button
                key={key}
                type="button"
                onClick={e => { e.stopPropagation(); onChange(key); setOpen(false) }}
                className={clsx(
                  'w-full flex items-center gap-2 px-3 py-1.5 text-xs font-medium hover:bg-offwhite-100 transition-colors',
                  key === status ? 'opacity-40 cursor-default' : 'cursor-pointer'
                )}
              >
                <span className={clsx('w-1.5 h-1.5 rounded-full', cfg.bg.replace('bg-', 'bg-').replace('-100', '-400').replace('-50', '-300'))} />
                <span className={cfg.text}>{cfg.label}</span>
              </button>
            )
          })}
        </div>
      )}
    </div>
  )
}
