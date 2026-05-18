import { useEffect, useState } from 'react'
import { CheckCircle, AlertCircle, Info, X } from 'lucide-react'
import clsx from 'clsx'
import { useAppData } from '../../context/AppDataContext'

const TYPE_STYLES = {
  success: {
    bar:   'bg-green-500',
    icon:  CheckCircle,
    iconColor: 'text-green-500',
    bg:    'bg-white',
  },
  error: {
    bar:   'bg-red-500',
    icon:  AlertCircle,
    iconColor: 'text-red-500',
    bg:    'bg-white',
  },
  info: {
    bar:   'bg-navy',
    icon:  Info,
    iconColor: 'text-navy',
    bg:    'bg-white',
  },
}

function ToastItem({ id, type = 'success', message }) {
  const { removeToast } = useAppData()
  const [visible, setVisible] = useState(false)
  const cfg = TYPE_STYLES[type] ?? TYPE_STYLES.success
  const Icon = cfg.icon

  useEffect(() => {
    const show = setTimeout(() => setVisible(true), 10)
    const hide = setTimeout(() => {
      setVisible(false)
      setTimeout(() => removeToast(id), 300)
    }, 3700)
    return () => { clearTimeout(show); clearTimeout(hide) }
  }, [id, removeToast])

  return (
    <div
      className={clsx(
        'flex items-start gap-3 min-w-[280px] max-w-sm rounded-xl overflow-hidden',
        'shadow-[0_4px_20px_rgba(27,43,72,0.15)] border border-offwhite-200',
        cfg.bg,
        'transition-all duration-300',
        visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2'
      )}
    >
      <div className={clsx('w-1 self-stretch shrink-0', cfg.bar)} />
      <div className="flex items-start gap-2.5 py-3 pr-3 flex-1">
        <Icon size={17} className={clsx('mt-0.5 shrink-0', cfg.iconColor)} />
        <p className="text-sm text-navy leading-snug flex-1">{message}</p>
        <button
          onClick={() => removeToast(id)}
          className="text-gray-400 hover:text-gray-600 transition-colors shrink-0 mt-0.5"
        >
          <X size={14} />
        </button>
      </div>
    </div>
  )
}

export default function ToastContainer() {
  const { toasts } = useAppData()
  if (toasts.length === 0) return null

  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col gap-2 items-end">
      {toasts.map(t => (
        <ToastItem key={t.id} {...t} />
      ))}
    </div>
  )
}
