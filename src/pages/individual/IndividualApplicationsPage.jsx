import { useState, useEffect } from 'react'
import { ClipboardList, X, ChevronRight, CheckCircle2, XCircle, Clock, Star, FileText } from 'lucide-react'
import { useNavigate, Link } from 'react-router-dom'
import { applicationApi } from '../../services/api'

const STATUS_CONFIG = {
  APPLIED:   { label: '검토 중',  color: 'text-amber-700 bg-amber-50 border-amber-200',  icon: Clock },
  APPROVED:  { label: '합격',     color: 'text-green-700 bg-green-50 border-green-200',  icon: CheckCircle2 },
  REJECTED:  { label: '불합격',   color: 'text-red-600 bg-red-50 border-red-200',         icon: XCircle },
  COMPLETED: { label: '근무완료', color: 'text-blue-700 bg-blue-50 border-blue-200',      icon: Star },
  NO_SHOW:   { label: '노쇼',     color: 'text-gray-500 bg-gray-50 border-gray-200',      icon: XCircle },
}

function ApplicationCard({ app, confirmId, setConfirmId, onCancel }) {
  const s            = STATUS_CONFIG[app.status] ?? STATUS_CONFIG.APPLIED
  const StatusIcon   = s.icon
  const isPending    = app.status === 'APPLIED'
  const isApproved   = app.status === 'APPROVED'
  const isConfirming = confirmId === app.applicationId

  return (
    <div className={`bg-white rounded-2xl border p-5 ${isApproved ? 'border-green-300 shadow-sm shadow-green-50' : 'border-offwhite-200'}`}>
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-3 flex-1 min-w-0">
          <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${isApproved ? 'bg-green-50' : 'bg-offwhite'}`}>
            <ClipboardList size={18} className={isApproved ? 'text-green-600' : 'text-navy'} />
          </div>
          <div className="min-w-0">
            <p className="font-bold text-navy truncate">{app.jobTitle ?? '공고 제목 없음'}</p>
            <p className="text-sm text-gray-500">{app.companyName ?? '—'}</p>
          </div>
        </div>

        <span className={`shrink-0 inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full border ${s.color}`}>
          <StatusIcon size={11} />
          {s.label}
        </span>
      </div>

      {isApproved && (
        <div className="mt-3 flex items-center justify-between bg-green-50 border border-green-200 rounded-xl px-3.5 py-2.5">
          <p className="text-xs font-semibold text-green-700">
            합격을 축하합니다! 계약서에 서명해주세요.
          </p>
          <Link
            to="/individual/contracts"
            className="text-xs font-bold text-white bg-green-600 hover:bg-green-700 px-3 py-1.5 rounded-lg transition-colors flex items-center gap-1 shrink-0 ml-2"
          >
            <FileText size={11} />계약서 보기
          </Link>
        </div>
      )}

      <div className="mt-3 pt-3 border-t border-offwhite-100 flex items-center justify-between">
        <Link
          to={`/individual/jobs/${app.jobPostId}`}
          className="flex items-center gap-1 text-xs text-gray-400 hover:text-navy transition-colors"
        >
          <ChevronRight size={13} />공고 상세 보기
        </Link>

        {isPending && (
          isConfirming ? (
            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-500">취소하시겠습니까?</span>
              <button
                onClick={() => setConfirmId(null)}
                className="text-xs px-2.5 py-1 rounded-lg border border-offwhite-200 text-gray-500 hover:border-gray-400 transition-colors"
              >
                아니요
              </button>
              <button
                onClick={() => onCancel(app.applicationId)}
                className="text-xs px-2.5 py-1 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-colors font-semibold"
              >
                취소하기
              </button>
            </div>
          ) : (
            <button
              onClick={() => setConfirmId(app.applicationId)}
              className="flex items-center gap-1 text-xs text-gray-400 hover:text-red-500 border border-offwhite-200 hover:border-red-300 px-2.5 py-1 rounded-lg transition-colors"
            >
              <X size={11} />지원 취소
            </button>
          )
        )}
      </div>
    </div>
  )
}

export default function IndividualApplicationsPage() {
  const navigate = useNavigate()
  const [applications, setApplications] = useState([])
  const [loading,      setLoading]      = useState(false)
  const [error,        setError]        = useState(null)
  const [confirmId,    setConfirmId]    = useState(null)

  useEffect(() => { fetchApplications() }, [])

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
      setApplications(prev => prev.filter(a => a.applicationId !== applicationId))
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
      <div className="text-center py-16">
        <p className="text-sm text-red-400">{error}</p>
      </div>
    )
  }

  const approved = applications.filter(a => a.status === 'APPROVED')
  const others   = applications.filter(a => a.status !== 'APPROVED')

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
        <div className="space-y-4">
          {approved.length > 0 && (
            <div>
              <div className="flex items-center gap-2 mb-2">
                <CheckCircle2 size={15} className="text-green-500" />
                <p className="text-sm font-bold text-green-700">합격 ({approved.length}건) — 계약서 서명이 필요합니다</p>
              </div>
              <div className="space-y-2">
                {approved.map(app => (
                  <ApplicationCard
                    key={app.applicationId}
                    app={app}
                    confirmId={confirmId}
                    setConfirmId={setConfirmId}
                    onCancel={handleCancel}
                  />
                ))}
              </div>
            </div>
          )}

          {others.length > 0 && (
            <div>
              {approved.length > 0 && (
                <p className="text-sm font-semibold text-gray-400 mb-2">다른 지원 내역</p>
              )}
              <div className="space-y-2">
                {others.map(app => (
                  <ApplicationCard
                    key={app.applicationId}
                    app={app}
                    confirmId={confirmId}
                    setConfirmId={setConfirmId}
                    onCancel={handleCancel}
                  />
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
