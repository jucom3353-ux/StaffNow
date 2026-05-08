import { useState } from 'react'
import { ClipboardList, X } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useIndividualData } from '../../hooks/useIndividualData'

const STATUS_CONFIG = {
  pending:  { label: '검토 중', color: 'bg-yellow-100 text-yellow-700' },
  accepted: { label: '합격',    color: 'bg-green-100 text-green-700' },
  rejected: { label: '불합격',  color: 'bg-red-100 text-red-700' },
}

export default function IndividualApplicationsPage() {
  const navigate = useNavigate()
  const { applications, cancelApplication } = useIndividualData()
  const [confirmId, setConfirmId] = useState(null)

  function handleCancel(appId) {
    cancelApplication(appId)
    setConfirmId(null)
  }

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
            const isPending = app.status === 'pending'
            const isConfirming = confirmId === app.id

            return (
              <div key={app.id} className="bg-white rounded-2xl border border-offwhite-200 p-5">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3 flex-1 min-w-0">
                    <div className="w-10 h-10 rounded-xl bg-offwhite flex items-center justify-center shrink-0">
                      <ClipboardList size={18} className="text-navy" />
                    </div>
                    <div className="min-w-0">
                      <p className="font-semibold text-navy truncate">{app.jobTitle}</p>
                      <p className="text-sm text-gray-500">{app.company}</p>
                      <p className="text-xs text-gray-400 mt-1">지원일 {app.appliedAt}</p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${s.color}`}>
                      {s.label}
                    </span>

                    {isPending && !isConfirming && (
                      <button
                        onClick={() => setConfirmId(app.id)}
                        className="flex items-center gap-1 text-xs text-gray-400 hover:text-red-500 border border-offwhite-200 hover:border-red-300 px-2.5 py-1 rounded-lg transition-colors"
                      >
                        <X size={12} />취소
                      </button>
                    )}
                  </div>
                </div>

                {/* 취소 확인 */}
                {isConfirming && (
                  <div className="mt-3 pt-3 border-t border-offwhite-200 flex items-center justify-between gap-3">
                    <p className="text-xs text-gray-500">정말 지원을 취소하시겠습니까?</p>
                    <div className="flex gap-2 shrink-0">
                      <button
                        onClick={() => setConfirmId(null)}
                        className="text-xs px-3 py-1.5 rounded-lg border border-offwhite-200 text-gray-500 hover:border-gray-400 transition-colors"
                      >
                        아니요
                      </button>
                      <button
                        onClick={() => handleCancel(app.id)}
                        className="text-xs px-3 py-1.5 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-colors font-semibold"
                      >
                        취소하기
                      </button>
                    </div>
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
