import { useState, useEffect } from 'react'
import { ClipboardList, X } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { applicationApi } from '../../services/api'

const STATUS_CONFIG = {
  APPLIED:   { label: '검토 중', color: 'bg-yellow-100 text-yellow-700' },
  APPROVED:  { label: '합격',    color: 'bg-green-100 text-green-700' },
  REJECTED:  { label: '불합격',  color: 'bg-red-100 text-red-700' },
  COMPLETED: { label: '완료',    color: 'bg-blue-100 text-blue-700' },
  NO_SHOW:   { label: '노쇼',    color: 'bg-gray-100 text-gray-500' },
}

export default function IndividualApplicationsPage() {
  const navigate = useNavigate()
  const [applications, setApplications] = useState([])
  const [loading,      setLoading]      = useState(false)
  const [error,        setError]        = useState(null)
  const [confirmId,    setConfirmId]    = useState(null)

  useEffect(() => {
    fetchApplications()
  }, [])

  async function fetchApplications() {
    setLoading(true)
    setError(null)
    try {
      const res = await applicationApi.myList()
      if (!res.ok) throw new Error('지원 내역을 불러오지 못했습니다.')
      const data = await res.json()
      setApplications(Array.isArray(data) ? data : [])
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleCancel(applicationId) {
    try {
      const res = await applicationApi.cancel(applicationId)
      if (!res.ok) throw new Error('지원 취소에 실패했습니다.')
      setApplications(prev => prev.filter(a => a.id !== applicationId))
      setConfirmId(null)
    } catch (e) {
      alert(e.message)
    }
  }

  if (loading) {
    return (
      <div className="text-center py-16 text-gray-400">
        <p className="text-sm">지원 내역을 불러오는 중...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="text-center py-16 text-gray-400">
        <p className="text-sm text-red-400">{error}</p>
      </div>
    )
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
            const s = STATUS_CONFIG[app.status] ?? STATUS_CONFIG.APPLIED
            const isPending = app.status === 'APPLIED'
            const isConfirming = confirmId === app.id
            const appliedAt = app.createdAt ? app.createdAt.substring(0, 10) : ''

            return (
              <div key={app.id} className="bg-white rounded-2xl border border-offwhite-200 p-5">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3 flex-1 min-w-0">
                    <div className="w-10 h-10 rounded-xl bg-offwhite flex items-center justify-center shrink-0">
                      <ClipboardList size={18} className="text-navy" />
                    </div>
                    <div className="min-w-0">
                      <p className="font-semibold text-navy truncate">{app.jobPost?.title}</p>
                      <p className="text-sm text-gray-500">{app.jobPost?.companyName}</p>
                      <p className="text-xs text-gray-400 mt-1">지원일 {appliedAt}</p>
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
