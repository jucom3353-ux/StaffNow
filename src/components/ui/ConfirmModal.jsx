import { useEffect } from 'react'
import { AlertTriangle, X } from 'lucide-react'
import Button from './Button'

export default function ConfirmModal({
  open,
  title,
  description,
  confirmLabel = '삭제',
  confirmVariant = 'danger',
  onConfirm,
  onCancel,
}) {
  useEffect(() => {
    if (!open) return
    const onKey = (e) => { if (e.key === 'Escape') onCancel() }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [open, onCancel])

  if (!open) return null

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      onClick={onCancel}
    >
      {/* 배경 오버레이 */}
      <div className="absolute inset-0 bg-navy-900/40 backdrop-blur-sm" />

      {/* 모달 */}
      <div
        className="relative bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6"
        onClick={e => e.stopPropagation()}
      >
        <button
          onClick={onCancel}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
        >
          <X size={18} />
        </button>

        <div className="flex items-start gap-4">
          <div className="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center shrink-0">
            <AlertTriangle size={20} className="text-red-500" />
          </div>
          <div className="flex-1 min-w-0">
            <h3 className="text-base font-bold text-navy">{title}</h3>
            {description && (
              <p className="text-sm text-gray-500 mt-1 leading-relaxed">{description}</p>
            )}
          </div>
        </div>

        <div className="flex gap-2 justify-end mt-6">
          <Button variant="secondary" size="sm" onClick={onCancel}>
            취소
          </Button>
          <Button variant={confirmVariant} size="sm" onClick={onConfirm}>
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  )
}
