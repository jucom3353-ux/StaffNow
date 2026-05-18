import { useState } from 'react'
import { Star, X } from 'lucide-react'

export default function RatingModal({ record, onSubmit, onClose }) {
  const [stars, setStars] = useState(0)
  const [hovered, setHovered] = useState(0)
  const [comment, setComment] = useState('')

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4" onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 space-y-4" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-navy">업무 평가</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
            <X size={16} />
          </button>
        </div>

        <div className="p-3 bg-offwhite rounded-xl">
          <p className="text-sm font-semibold text-navy">{record.staff}</p>
          <p className="text-xs text-gray-400 mt-0.5">{record.shift}</p>
        </div>

        <div className="flex flex-col items-center gap-2 py-1">
          <p className="text-xs text-gray-500">근무 만족도를 선택해 주세요</p>
          <div className="flex gap-1.5">
            {[1, 2, 3, 4, 5].map(n => (
              <button
                key={n}
                onMouseEnter={() => setHovered(n)}
                onMouseLeave={() => setHovered(0)}
                onClick={() => setStars(n)}
                className="transition-transform hover:scale-110 active:scale-95"
              >
                <Star
                  size={38}
                  className={`transition-colors ${
                    n <= (hovered || stars)
                      ? 'fill-yellow-400 text-yellow-400'
                      : 'fill-gray-100 text-gray-200'
                  }`}
                />
              </button>
            ))}
          </div>
          {stars > 0 && (
            <p className="text-sm font-bold text-navy">{stars}.0 / 5.0</p>
          )}
        </div>

        <div>
          <label className="block text-xs font-semibold text-gray-500 mb-1.5">
            코멘트 <span className="font-normal text-gray-400">(선택)</span>
          </label>
          <textarea
            value={comment}
            onChange={e => setComment(e.target.value)}
            placeholder="이 스태프에 대한 의견을 자유롭게 남겨주세요."
            rows={3}
            className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy placeholder-gray-300 outline-none focus:border-navy resize-none"
          />
        </div>

        <div className="flex gap-2 pt-1">
          <button
            onClick={onClose}
            className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:bg-offwhite transition-colors"
          >
            취소
          </button>
          <button
            onClick={() => { if (stars) onSubmit({ stars, comment: comment.trim() }) }}
            disabled={!stars}
            className="flex-1 py-2.5 rounded-xl bg-orange text-white text-sm font-bold hover:bg-orange-600 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
          >
            평가 완료
          </button>
        </div>
      </div>
    </div>
  )
}
