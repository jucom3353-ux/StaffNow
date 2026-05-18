import { CheckCircle, Info, AlertCircle, X } from 'lucide-react'
import { useAppData } from '../../context/AppDataContext'

const TOAST_META = {
  success: { icon: CheckCircle, color: 'text-green-600', bg: 'bg-green-50 border-green-200' },
  info:    { icon: Info,        color: 'text-blue-500',  bg: 'bg-blue-50 border-blue-200' },
  error:   { icon: AlertCircle, color: 'text-red-500',   bg: 'bg-red-50 border-red-200' },
}

export default function ToastContainer() {
  const { toasts, removeToast } = useAppData()

  if (!toasts.length) return null

  return (
    <div className="fixed bottom-5 right-5 z-[100] flex flex-col gap-2 pointer-events-none">
      {toasts.map(t => {
        const meta = TOAST_META[t.type] ?? TOAST_META.info
        const Icon = meta.icon
        return (
          <div
            key={t.id}
            className={`pointer-events-auto flex items-start gap-3 px-4 py-3 rounded-xl border shadow-lg min-w-[260px] max-w-xs animate-slide-up ${meta.bg}`}
          >
            <Icon size={16} className={`${meta.color} shrink-0 mt-0.5`} />
            <p className="text-sm font-medium text-navy flex-1 leading-snug">{t.message}</p>
            <button
              onClick={() => removeToast(t.id)}
              className="text-gray-400 hover:text-gray-600 shrink-0"
            >
              <X size={14} />
            </button>
          </div>
        )
      })}
    </div>
  )
}
