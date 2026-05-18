import { useState, useEffect } from 'react'
import { ArrowLeft, MapPin, Bookmark, Send, Building2, Banknote, Calendar, CheckCircle2, AlertTriangle, X } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { jobSearchApi, applicationApi } from '../../services/api'
import { useIndividualData } from '../../hooks/useIndividualData'

const WAGE_LABEL = { HOURLY: '시급', DAILY: '일급', MONTHLY: '월급', FIXED: '고정급' }
const CATEGORY_TO_TAG = { SHORT_TERM: '단기', LONG_TERM: '장기', WEEKEND: '주말', EVENT: '행사/이벤트' }

function transformJob(data) {
  return {
    id: data.id,
    title: data.title,
    company: data.companyName,
    location: data.workLocation,
    wage: `${WAGE_LABEL[data.wageType] || '시급'} ${(data.wageAmount || 0).toLocaleString()}원`,
    deadline: data.deadline ? data.deadline.substring(0, 10) : '',
    tags: [CATEGORY_TO_TAG[data.category]].filter(Boolean),
    isNew: data.createdAt && (Date.now() - new Date(data.createdAt).getTime() < 48 * 60 * 60 * 1000),
  }
}

function ApplyConfirmModal({ job, applying, onConfirm, onClose }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4" onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 space-y-4" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-navy">지원 확인</h2>
          <button onClick={onClose} disabled={applying} className="text-gray-400 hover:text-gray-600 disabled:opacity-50">
            <X size={16} />
          </button>
        </div>

        <div className="bg-offwhite rounded-xl p-3">
          <p className="text-sm font-bold text-navy line-clamp-1">{job.title}</p>
          <p className="text-xs text-gray-500 mt-0.5">{job.company} · {job.wage}</p>
        </div>

        <p className="text-sm text-gray-600 leading-relaxed">
          위 공고에 지원하시겠습니까?<br />
          지원 후 합격 시 근무 일정이 확정됩니다.
        </p>

        <div className="flex gap-2.5 bg-red-50 border border-red-100 rounded-xl p-3">
          <AlertTriangle size={15} className="text-red-400 shrink-0 mt-0.5" />
          <p className="text-xs text-red-600 leading-relaxed">
            <span className="font-bold">노쇼(No-Show) 주의</span><br />
            확정 후 무단 불참 시 발생하는 손해에 대한 책임은
            지원자 본인에게 있으며, 플랫폼은 이에 대한 책임을
            지지 않습니다. 신중히 지원해주세요.
          </p>
        </div>

        <div className="flex gap-2 pt-1">
          <button
            onClick={onClose}
            disabled={applying}
            className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:bg-offwhite transition-colors disabled:opacity-50"
          >
            취소
          </button>
          <button
            onClick={onConfirm}
            disabled={applying}
            className="flex-1 py-2.5 rounded-xl bg-orange text-white text-sm font-bold hover:bg-orange-600 transition-colors flex items-center justify-center gap-1.5 disabled:opacity-60"
          >
            {applying ? (
              <span>지원 중...</span>
            ) : (
              <><Send size={13} />지원하기</>
            )}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function IndividualJobDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isSaved, toggleSave } = useIndividualData()
  const [job,         setJob]         = useState(null)
  const [loading,     setLoading]     = useState(true)
  const [error,       setError]       = useState(null)
  const [applied,     setApplied]     = useState(false)
  const [applying,    setApplying]    = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)

  useEffect(() => {
    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [jobRes, myRes] = await Promise.all([
          jobSearchApi.get(id),
          applicationApi.myList(),
        ])
        if (!jobRes.ok) throw new Error('공고를 불러오지 못했습니다.')
        const jobData = await jobRes.json()
        setJob(transformJob(jobData))

        if (myRes.ok) {
          const myApps = await myRes.json()
          const apps = Array.isArray(myApps) ? myApps : []
          setApplied(apps.some(a => String(a.jobPost?.id) === String(id)))
        }
      } catch (e) {
        setError(e.message)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [id])

  async function handleConfirm() {
    setApplying(true)
    try {
      const res = await applicationApi.apply(id)
      if (res.status === 409) {
        setApplied(true)
        setShowConfirm(false)
        return
      }
      if (!res.ok) throw new Error('지원에 실패했습니다.')
      setApplied(true)
      setShowConfirm(false)
    } catch (e) {
      alert(e.message)
    } finally {
      setApplying(false)
    }
  }

  if (loading) {
    return (
      <div className="text-center py-16 text-gray-400">
        <p className="text-sm">공고를 불러오는 중...</p>
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

  const saved = isSaved(String(job.id))

  return (
    <div className="max-w-2xl mx-auto space-y-5">
      {showConfirm && (
        <ApplyConfirmModal
          job={job}
          applying={applying}
          onConfirm={handleConfirm}
          onClose={() => { if (!applying) setShowConfirm(false) }}
        />
      )}

      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-navy transition-colors"
      >
        <ArrowLeft size={16} />
        목록으로
      </button>

      <div className="bg-white rounded-2xl border border-offwhite-200 p-6">
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              {job.isNew && (
                <span className="text-[10px] font-bold bg-orange text-white px-1.5 py-0.5 rounded-full">NEW</span>
              )}
              <div className="flex gap-1.5">
                {job.tags.map(t => (
                  <span key={t} className="text-xs bg-offwhite px-2.5 py-0.5 rounded-full text-gray-500">{t}</span>
                ))}
              </div>
            </div>
            <h1 className="text-xl font-bold text-navy mb-1">{job.title}</h1>
            <p className="text-gray-500 text-sm">{job.company}</p>
          </div>
          <button
            onClick={() => toggleSave(String(job.id))}
            className={`p-2 rounded-xl border-2 transition-colors ${saved ? 'border-orange text-orange bg-orange-50' : 'border-offwhite-200 text-gray-400 hover:border-orange hover:text-orange'}`}
          >
            <Bookmark size={20} fill={saved ? 'currentColor' : 'none'} />
          </button>
        </div>

        <div className="mt-5 grid grid-cols-2 gap-4">
          {[
            { icon: Banknote,  label: '급여',   value: job.wage },
            { icon: MapPin,    label: '위치',   value: job.location },
            { icon: Calendar,  label: '마감일', value: job.deadline },
            { icon: Building2, label: '회사',   value: job.company },
          ].map(item => (
            <div key={item.label} className="flex items-center gap-3 p-3 bg-offwhite rounded-xl">
              <item.icon size={16} className="text-orange shrink-0" />
              <div>
                <p className="text-[10px] text-gray-400">{item.label}</p>
                <p className="text-sm font-semibold text-navy">{item.value}</p>
              </div>
            </div>
          ))}
        </div>

        <div className="mt-5">
          {applied ? (
            <div className="w-full py-3 rounded-xl bg-green-50 border border-green-200 text-green-700 font-semibold text-sm text-center flex items-center justify-center gap-2">
              <CheckCircle2 size={16} />
              지원 완료! 결과를 기다려주세요.
            </div>
          ) : (
            <button
              onClick={() => setShowConfirm(true)}
              className="w-full py-3 rounded-xl bg-orange text-white font-bold text-sm hover:bg-orange-600 transition-colors flex items-center justify-center gap-2"
            >
              <Send size={15} />
              지원하기
            </button>
          )}
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-offwhite-200 p-6">
        <h2 className="font-bold text-navy mb-4">공고 내용</h2>
        <div className="space-y-4 text-sm text-gray-600 leading-relaxed">
          <p>안녕하세요! {job.company}입니다. {job.title} 포지션으로 열정 있는 스태프를 모집합니다.</p>
          <div>
            <p className="font-semibold text-navy mb-2">업무 내용</p>
            <ul className="list-disc list-inside space-y-1 text-gray-500">
              <li>행사 진행 보조 및 안내</li>
              <li>현장 스태프 운영 지원</li>
              <li>기타 현장 관련 업무</li>
            </ul>
          </div>
          <div>
            <p className="font-semibold text-navy mb-2">지원 자격</p>
            <ul className="list-disc list-inside space-y-1 text-gray-500">
              <li>성실하고 책임감 있는 분</li>
              <li>팀워크를 중시하는 분</li>
              <li>경력 무관, 누구나 지원 가능</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}
