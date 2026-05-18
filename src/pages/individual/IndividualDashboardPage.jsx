import { useState, useEffect } from 'react'
import { Heart, ClipboardList, Bell, Search, ChevronRight, MapPin, Clock, Bookmark } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useIndividualData } from '../../hooks/useIndividualData'
import { getAssignedShifts } from '../../hooks/useAttendance'
import { applicationApi, jobSearchApi } from '../../services/api'

const STATUS_CONFIG = {
  APPLIED:   { label: '검토 중', color: 'bg-yellow-100 text-yellow-700' },
  APPROVED:  { label: '합격',    color: 'bg-green-100 text-green-700' },
  REJECTED:  { label: '불합격',  color: 'bg-red-100 text-red-700' },
  COMPLETED: { label: '완료',    color: 'bg-blue-100 text-blue-700' },
  NO_SHOW:   { label: '노쇼',    color: 'bg-gray-100 text-gray-500' },
}

const WAGE_LABEL = { HOURLY: '시급', DAILY: '일급', MONTHLY: '월급', FIXED: '고정급' }
const CATEGORY_TO_TAG = { SHORT_TERM: '단기', LONG_TERM: '장기', WEEKEND: '주말', EVENT: '행사/이벤트' }

function transformJob(job) {
  return {
    id:       job.id,
    title:    job.title,
    company:  job.companyName,
    location: job.workLocation,
    wage:     `${WAGE_LABEL[job.wageType] || '시급'} ${(job.wageAmount || 0).toLocaleString()}원`,
    deadline: job.deadline ? job.deadline.substring(0, 10) : '',
    tags:     [CATEGORY_TO_TAG[job.category]].filter(Boolean),
    isNew:    job.createdAt && (Date.now() - new Date(job.createdAt).getTime() < 48 * 60 * 60 * 1000),
  }
}

function getUnreadMsgCount(email) {
  try {
    const safe = email?.replace(/[^a-zA-Z0-9]/g, '_') || 'anon'
    const convs = JSON.parse(localStorage.getItem(`staffnow_ind_messages_${safe}`) || '[]')
    return convs.reduce((sum, c) =>
      sum + c.messages.filter(m => !m.read && m.from === 'company').length, 0)
  } catch { return 0 }
}

function todayStr() { return new Date().toISOString().slice(0, 10) }

export default function IndividualDashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const { savedJobIds, isSaved, toggleSave } = useIndividualData()
  const unreadCount = getUnreadMsgCount(user?.email)
  const todayShift  = getAssignedShifts(user?.name).find(s => s.shiftDate === todayStr()) ?? null

  const [applications,    setApplications]    = useState([])
  const [recommendedJobs, setRecommendedJobs] = useState([])

  useEffect(() => {
    async function load() {
      const [appsRes, jobsRes] = await Promise.all([
        applicationApi.myList().catch(() => null),
        jobSearchApi.search({ sort: 'latest', size: 4 }).catch(() => null),
      ])

      if (appsRes?.ok) {
        const data = await appsRes.json()
        setApplications(Array.isArray(data) ? data : [])
      }

      if (jobsRes?.ok) {
        const data = await jobsRes.json()
        const list = Array.isArray(data) ? data : (data.content ?? [])
        setRecommendedJobs(list.map(transformJob))
      }
    }
    load()
  }, [])

  const approvedCount      = applications.filter(a => a.status === 'APPROVED').length
  const recentApplications = applications.slice(0, 4)

  const stats = [
    { label: '지원한 공고', value: applications.length, icon: ClipboardList, color: 'text-blue-500',  bg: 'bg-blue-50',   to: '/individual/applications' },
    { label: '합격',        value: approvedCount,        icon: ClipboardList, color: 'text-green-500', bg: 'bg-green-50',  to: '/individual/applications' },
    { label: '관심 공고',   value: savedJobIds.length,   icon: Heart,         color: 'text-pink-500',  bg: 'bg-pink-50',   to: '/individual/saved' },
    { label: '미확인 알림', value: unreadCount,           icon: Bell,          color: 'text-orange',    bg: 'bg-orange-50', to: '/individual/messages' },
  ]

  return (
    <div className="space-y-6">
      <div className="space-y-3">
        <div>
          <h1 className="text-2xl font-bold text-navy">안녕하세요, {user?.name}님 👋</h1>
          <p className="text-gray-500 text-sm mt-1">오늘도 좋은 공고를 찾아보세요.</p>
        </div>
        <button
          onClick={() => navigate('/individual/jobs')}
          className="w-full flex items-center gap-3 bg-white border border-offwhite-200 rounded-2xl px-4 py-3 text-sm text-gray-400 hover:border-orange transition-colors shadow-sm"
        >
          <Search size={16} className="text-gray-300 shrink-0" />
          공고 검색 (직종, 지역, 키워드...)
        </button>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map(s => (
          <div
            key={s.label}
            onClick={() => s.to && navigate(s.to)}
            className="bg-white rounded-2xl p-4 border border-offwhite-200 transition-all cursor-pointer hover:border-navy hover:shadow-sm"
          >
            <div className={`w-9 h-9 rounded-xl ${s.bg} flex items-center justify-center mb-3`}>
              <s.icon size={18} className={s.color} />
            </div>
            <p className="text-2xl font-extrabold text-navy tabular-nums">{s.value}</p>
            <p className="text-xs text-gray-500 mt-0.5">{s.label}</p>
          </div>
        ))}
      </div>

      {/* 오늘의 근무 카드 */}
      {todayShift && (
        <div className="bg-white rounded-2xl border border-offwhite-200 p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-bold text-navy flex items-center gap-2">
              <Clock size={15} className="text-orange" />
              오늘의 근무
            </h2>
            <button
              onClick={() => navigate('/individual/attendance')}
              className="flex items-center gap-1 text-xs text-orange font-semibold hover:underline"
            >
              전체 보기 <ChevronRight size={13} />
            </button>
          </div>

          <div className="flex items-start justify-between gap-3 mb-4">
            <div>
              <p className="font-bold text-navy">{todayShift.jobTitle}</p>
              <p className="text-sm text-gray-500">{todayShift.company}</p>
              <div className="flex items-center gap-3 mt-1.5 text-xs text-gray-400">
                <span className="flex items-center gap-1"><Clock size={11} />{todayShift.scheduledStart} – {todayShift.scheduledEnd}</span>
                <span className="flex items-center gap-1"><MapPin size={11} />{todayShift.location}</span>
              </div>
            </div>
            <p className="text-sm font-bold text-orange shrink-0">{todayShift.wage}</p>
          </div>

          <button
            onClick={() => navigate('/individual/attendance')}
            className="w-full py-2.5 bg-orange text-white rounded-xl text-sm font-bold flex items-center justify-center gap-2 hover:bg-orange-600 transition-colors"
          >
            <Clock size={15} />출퇴근 기록하기
          </button>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 추천 공고 */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
            <h2 className="font-bold text-navy">추천 공고</h2>
            <button
              onClick={() => navigate('/individual/jobs')}
              className="flex items-center gap-1 text-xs text-orange font-semibold hover:underline"
            >
              전체 보기 <ChevronRight size={13} />
            </button>
          </div>
          {recommendedJobs.length === 0 ? (
            <div className="px-5 py-8 text-center text-sm text-gray-400">공고를 불러오는 중...</div>
          ) : (
            <div className="divide-y divide-offwhite-200">
              {recommendedJobs.map(job => (
                <div
                  key={job.id}
                  onClick={() => navigate(`/individual/jobs/${job.id}`)}
                  className="px-5 py-4 hover:bg-offwhite cursor-pointer transition-colors"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        {job.isNew && (
                          <span className="text-[10px] font-bold bg-orange text-white px-1.5 py-0.5 rounded-full">NEW</span>
                        )}
                        <span className="text-sm font-semibold text-navy truncate">{job.title}</span>
                      </div>
                      <p className="text-xs text-gray-500 mb-2">{job.company}</p>
                      <div className="flex items-center gap-3 text-xs text-gray-400">
                        <span className="flex items-center gap-1"><MapPin size={11} />{job.location}</span>
                        <span className="flex items-center gap-1"><Clock size={11} />~{job.deadline}</span>
                      </div>
                      <div className="flex flex-wrap gap-1 mt-2">
                        {job.tags.map(t => (
                          <span key={t} className="text-[10px] bg-offwhite px-2 py-0.5 rounded-full text-gray-500">{t}</span>
                        ))}
                      </div>
                    </div>
                    <div className="text-right shrink-0">
                      <p className="text-sm font-bold text-orange">{job.wage}</p>
                      <button
                        onClick={e => { e.stopPropagation(); toggleSave(String(job.id)) }}
                        className={`mt-2 p-1 rounded-lg transition-colors ${isSaved(String(job.id)) ? 'text-orange' : 'text-gray-300 hover:text-orange'}`}
                      >
                        <Bookmark size={16} fill={isSaved(String(job.id)) ? 'currentColor' : 'none'} />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* 지원 현황 */}
        <div className="bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
            <h2 className="font-bold text-navy">지원 현황</h2>
            <button
              onClick={() => navigate('/individual/applications')}
              className="flex items-center gap-1 text-xs text-orange font-semibold hover:underline"
            >
              전체 보기 <ChevronRight size={13} />
            </button>
          </div>
          <div className="divide-y divide-offwhite-200">
            {recentApplications.length === 0 ? (
              <div className="px-5 py-8 text-center">
                <ClipboardList size={28} className="text-gray-300 mx-auto mb-2" />
                <p className="text-sm font-semibold text-gray-400">아직 지원한 공고가 없습니다</p>
                <p className="text-xs text-gray-400 mt-1 mb-3">근처의 공고를 확인하고 지원해보세요!</p>
                <button
                  onClick={() => navigate('/individual/jobs')}
                  className="text-xs font-semibold text-orange hover:underline"
                >
                  공고 보러 가기 →
                </button>
              </div>
            ) : recentApplications.map(app => {
              const s = STATUS_CONFIG[app.status] ?? STATUS_CONFIG.APPLIED
              return (
                <div key={app.id} className="px-5 py-4">
                  <p className="text-sm font-semibold text-navy truncate">{app.jobPost?.title}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{app.jobPost?.companyName}</p>
                  <div className="flex items-center justify-between mt-2">
                    <span className="text-xs text-gray-400">{app.createdAt ? app.createdAt.substring(0, 10) : ''}</span>
                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${s.color}`}>{s.label}</span>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      </div>
    </div>
  )
}
