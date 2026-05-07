import { useState } from 'react'
import { Building2, CheckCircle2, XCircle } from 'lucide-react'
import { PENDING_BUSINESSES } from '../../data/mockAdmin'

export default function AdminBusinessesPage() {
  const [businesses, setBusinesses] = useState(PENDING_BUSINESSES)
  const [processed, setProcessed] = useState({})

  function handle(id, action) {
    setProcessed(p => ({ ...p, [id]: action }))
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">기업 인증</h1>
        <p className="text-sm text-gray-500 mt-1">기업 회원 인증 요청 처리</p>
      </div>

      <div className="space-y-3">
        {businesses.map(b => {
          const result = processed[b.id]
          return (
            <div key={b.id} className="bg-white rounded-2xl border border-offwhite-200 p-5">
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center shrink-0">
                    <Building2 size={22} className="text-blue-500" />
                  </div>
                  <div>
                    <p className="font-bold text-navy">{b.company}</p>
                    <p className="text-sm text-gray-500">대표자: {b.representative}</p>
                    <p className="text-xs text-gray-400 mt-0.5">사업자등록번호: {b.businessNumber}</p>
                    <p className="text-xs text-gray-400">요청일: {b.requestedAt}</p>
                  </div>
                </div>
                <div className="shrink-0">
                  {result ? (
                    <span className={`text-sm font-bold px-3 py-1.5 rounded-full ${result === 'approved' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                      {result === 'approved' ? '승인됨' : '거절됨'}
                    </span>
                  ) : (
                    <div className="flex gap-2">
                      <button
                        onClick={() => handle(b.id, 'approved')}
                        className="flex items-center gap-1.5 text-sm font-semibold bg-green-50 text-green-700 border border-green-200 px-3 py-1.5 rounded-xl hover:bg-green-100 transition-colors"
                      >
                        <CheckCircle2 size={15} />
                        승인
                      </button>
                      <button
                        onClick={() => handle(b.id, 'rejected')}
                        className="flex items-center gap-1.5 text-sm font-semibold bg-red-50 text-red-700 border border-red-200 px-3 py-1.5 rounded-xl hover:bg-red-100 transition-colors"
                      >
                        <XCircle size={15} />
                        거절
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
