import { useState } from 'react'
import { Flag, AlertTriangle, CheckCircle2 } from 'lucide-react'

const REPORTS = [
  { id: 1, target: '물류창고 야간 파트',     type: '허위 정보',   reporter: '이민준',  reportedAt: '2026-05-07', status: 'pending' },
  { id: 2, target: '(주)한빛물류 채용담당',  type: '비매너 행동', reporter: '박서연',  reportedAt: '2026-05-06', status: 'pending' },
  { id: 3, target: '강남 행사 스태프 모집',  type: '허위 정보',   reporter: '최지훈',  reportedAt: '2026-05-05', status: 'resolved' },
]

export default function AdminReportsPage() {
  const [reports, setReports] = useState(REPORTS)

  function resolve(id) {
    setReports(r => r.map(x => x.id === id ? { ...x, status: 'resolved' } : x))
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">신고/제재</h1>
        <p className="text-sm text-gray-500 mt-1">플랫폼 신고 접수 현황</p>
      </div>

      <div className="space-y-3">
        {reports.map(r => (
          <div key={r.id} className={`bg-white rounded-2xl border p-5 ${r.status === 'pending' ? 'border-red-200' : 'border-offwhite-200'}`}>
            <div className="flex items-start justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${r.status === 'pending' ? 'bg-red-50' : 'bg-green-50'}`}>
                  {r.status === 'pending'
                    ? <AlertTriangle size={18} className="text-red-500" />
                    : <CheckCircle2 size={18} className="text-green-500" />
                  }
                </div>
                <div>
                  <p className="font-bold text-navy">{r.target}</p>
                  <p className="text-sm text-gray-500">신고 유형: {r.type}</p>
                  <p className="text-xs text-gray-400 mt-0.5">신고자: {r.reporter} · {r.reportedAt}</p>
                </div>
              </div>
              <div className="shrink-0">
                {r.status === 'pending' ? (
                  <button
                    onClick={() => resolve(r.id)}
                    className="text-sm font-semibold bg-navy text-white px-3.5 py-1.5 rounded-xl hover:bg-navy-700 transition-colors"
                  >
                    처리 완료
                  </button>
                ) : (
                  <span className="text-sm font-semibold text-green-600 bg-green-50 px-3 py-1.5 rounded-full">처리됨</span>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
