import { ClipboardList } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useIndividualData } from '../../hooks/useIndividualData'

const STATUS_CONFIG = {
  pending:  { label: '검토 중', color: 'bg-yellow-100 text-yellow-700' },
  accepted: { label: '합격',    color: 'bg-green-100 text-green-700' },
  rejected: { label: '불합격',  color: 'bg-red-100 text-red-700' },
}

export default function IndividualApplicationsPage() {
  const navigate = useNavigate()
  const { applications } = useIndividualData()

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">지원 현황</h1>
        <p className="text-sm text-gray-500 mt-1">내가 지원한 공고 {applications.length}건</p>
      </div>

      {applications.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <ClipboardList size={32} className="mx-auto mb-3 opacity-30" />
          <p className="text-sm font-semibold">아직 지원한 공고가 없습니다.</p>
          <p className="text-xs mt-1 mb-4">공고를 찾아 지원해보세요!</p>
          <button
            onClick={() => navigate('/individual/jobs')}
            className="text-sm font-semibold text-orange hover:underline"
          >
            공고 보러 가기 →
          </button>
        </div>
      ) : (
        <div className="space-y-3">
          {applications.map(app => {
            const s = STATUS_CONFIG[app.status] ?? STATUS_CONFIG.pending
            return (
              <div key={app.id} className="bg-white rounded-2xl border border-offwhite-200 p-5">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-offwhite flex items-center justify-center shrink-0">
                      <ClipboardList size={18} className="text-navy" />
                    </div>
                    <div>
                      <p className="font-semibold text-navy">{app.jobTitle}</p>
                      <p className="text-sm text-gray-500">{app.company}</p>
                      <p className="text-xs text-gray-400 mt-1">지원일 {app.appliedAt}</p>
                    </div>
                  </div>
                  <span className={`text-xs font-semibold px-2.5 py-1 rounded-full shrink-0 ${s.color}`}>{s.label}</span>
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
