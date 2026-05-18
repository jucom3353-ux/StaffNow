import { AlertCircle } from 'lucide-react'

export default function ErrorBanner({ message, onRetry }) {
  return (
    <div className="flex items-start gap-3 bg-red-50 border border-red-200 text-red-700 rounded-lg p-4">
      <AlertCircle size={18} className="mt-0.5 shrink-0" />
      <div className="flex-1 text-sm">{message}</div>
      {onRetry && (
        <button
          onClick={onRetry}
          className="text-sm font-medium underline underline-offset-2 hover:no-underline shrink-0"
        >
          다시 시도
        </button>
      )}
    </div>
  )
}
