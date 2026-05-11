import { useState, useMemo, useEffect, useRef } from 'react'
import { Search, MoreVertical, ChevronUp, ChevronDown, X, User, Mail, Calendar, ShieldOff, ShieldCheck, Copy, Check, Briefcase, Star, Clock, Building2, FileText, AlertTriangle, ChevronRight } from 'lucide-react'
import { RECENT_USERS } from '../../data/mockAdmin'

// ── 유저별 활동 기록 데모 데이터 ──────────────────────────────────────────────
const USER_ACTIVITY = {
  'u-101': {
    applications: [
      { id: 1, job: '주말 행사 스태프 모집',   company: '이벤트플러스',    date: '2026-05-06', status: '합격' },
      { id: 2, job: '6월 박람회 안내 스태프',  company: '코엑스',          date: '2026-04-28', status: '지원 중' },
      { id: 3, job: '강남 매장 오픈 지원',     company: '강남스토어',      date: '2026-04-10', status: '완료' },
      { id: 4, job: '물류센터 야간 지원',      company: '한빛물류',        date: '2026-03-22', status: '불합격' },
    ],
    ratings: [
      { company: '이벤트플러스', score: 4.9, comment: '적극적이고 성실한 태도가 인상적이었습니다.',    date: '2026-05-02' },
      { company: '강남스토어',   score: 4.7, comment: '고객 응대가 능숙하고 지시를 잘 따랐습니다.',   date: '2026-04-12' },
    ],
    attendance: [
      { date: '2026-05-02', job: '강남 매장 오픈 지원',    status: '정상 출근' },
      { date: '2026-04-10', job: '물류센터 야간 지원',     status: '정상 출근' },
      { date: '2026-03-22', job: '주말 행사 스태프 모집',  status: '정상 출근' },
    ],
  },
  'u-103': {
    applications: [
      { id: 1, job: '주말 행사 스태프 모집',   company: '이벤트플러스',    date: '2026-05-05', status: '지원 중' },
      { id: 2, job: '강남 매장 오픈 지원',     company: '강남스토어',      date: '2026-04-20', status: '완료' },
    ],
    ratings: [
      { company: '강남스토어', score: 4.8, comment: '밝은 미소와 빠른 업무 처리가 좋았습니다.', date: '2026-04-22' },
    ],
    attendance: [
      { date: '2026-04-20', job: '강남 매장 오픈 지원', status: '정상 출근' },
    ],
  },
  'u-105': {
    applications: [
      { id: 1, job: '6월 박람회 안내 스태프',  company: '코엑스',     date: '2026-05-04', status: '합격' },
      { id: 2, job: '물류센터 야간 지원',      company: '한빛물류',   date: '2026-04-15', status: '완료' },
      { id: 3, job: '강남 매장 오픈 지원',     company: '강남스토어', date: '2026-03-30', status: '완료' },
    ],
    ratings: [
      { company: '한빛물류',   score: 5.0, comment: '물류 작업 속도와 정확성이 매우 뛰어납니다.',         date: '2026-04-17' },
      { company: '강남스토어', score: 4.6, comment: '성실하게 임했습니다. 재고용 의사 있음.',              date: '2026-04-01' },
    ],
    attendance: [
      { date: '2026-04-15', job: '물류센터 야간 지원', status: '정상 출근' },
      { date: '2026-03-30', job: '강남 매장 오픈 지원', status: '정상 출근' },
    ],
  },
  'u-102': {
    postedJobs: [
      { id: 1, title: '창고 야간 파트', applicants: 12, hired: 5, status: '완료', date: '2026-04-01' },
      { id: 2, title: '물류 상하차 긴급 모집', applicants: 8, hired: 3, status: '진행 중', date: '2026-05-01' },
      { id: 3, title: '배송 드라이버 모집', applicants: 4, hired: 0, status: '마감', date: '2026-03-15' },
    ],
    hiringActivity: [
      { date: '2026-05-06', action: '이민준 채용 확정', job: '물류 상하차 긴급 모집' },
      { date: '2026-05-05', action: '박서연 서류 검토', job: '물류 상하차 긴급 모집' },
      { date: '2026-04-28', action: '최지훈 채용 확정', job: '창고 야간 파트' },
    ],
  },
  'u-104': {
    postedJobs: [
      { id: 1, title: '행사 진행 스태프', applicants: 20, hired: 10, status: '완료', date: '2026-03-10' },
      { id: 2, title: '팝업스토어 운영 보조', applicants: 5, hired: 2, status: '완료', date: '2026-02-20' },
    ],
    hiringActivity: [
      { date: '2026-03-25', action: '행사 완료 처리', job: '행사 진행 스태프' },
      { date: '2026-03-10', action: '공고 등록', job: '행사 진행 스태프' },
    ],
  },
}

const APP_STATUS_COLOR = {
  '합격':   'bg-green-100 text-green-700',
  '지원 중': 'bg-blue-100 text-blue-700',
  '완료':   'bg-gray-100 text-gray-600',
  '불합격': 'bg-red-100 text-red-500',
}

const JOB_STATUS_COLOR = {
  '진행 중': 'bg-blue-100 text-blue-700',
  '완료':    'bg-gray-100 text-gray-600',
  '마감':    'bg-red-100 text-red-500',
}

const ROLE_LABEL = { INDIVIDUAL: '개인', BUSINESS: '기업', ADMIN: '관리자' }
const ROLE_COLOR = {
  INDIVIDUAL: 'bg-blue-100 text-blue-700',
  BUSINESS:   'bg-purple-100 text-purple-700',
  ADMIN:      'bg-orange-100 text-orange-700',
}

const STATUS_OVERRIDE_KEY = 'staffnow_admin_user_status'

function loadRegisteredUsers() {
  try {
    const arr = JSON.parse(localStorage.getItem('staffnow_users') || '[]')
    return arr.map(u => ({
      id:       u.id,
      name:     u.name,
      role:     u.role,
      email:    u.email,
      joinedAt: u.id?.startsWith('u-reg-')
        ? new Date(Number(u.id.replace('u-reg-', ''))).toISOString().slice(0, 10)
        : '-',
      isActive: true,
    }))
  } catch { return [] }
}

function loadStatusOverrides() {
  try { return JSON.parse(localStorage.getItem(STATUS_OVERRIDE_KEY) || '{}') } catch { return {} }
}

function saveStatusOverrides(obj) {
  try { localStorage.setItem(STATUS_OVERRIDE_KEY, JSON.stringify(obj)) } catch {}
}

// ── 정렬 헤더 셀 ────────────────────────────────────────────
function SortTh({ label, sortKey, current, dir, onSort, className = '' }) {
  const active = current === sortKey
  return (
    <th
      className={`text-left px-5 py-3 text-xs font-semibold text-gray-500 cursor-pointer select-none hover:text-navy transition-colors ${className}`}
      onClick={() => onSort(sortKey)}
    >
      <span className="inline-flex items-center gap-1">
        {label}
        <span className="flex flex-col">
          <ChevronUp  size={10} className={active && dir === 'asc'  ? 'text-orange' : 'text-gray-300'} />
          <ChevronDown size={10} className={active && dir === 'desc' ? 'text-orange' : 'text-gray-300'} />
        </span>
      </span>
    </th>
  )
}

export default function AdminUsersPage() {
  const [query, setQuery]             = useState('')
  const [sortKey, setSortKey]         = useState('joinedAt')
  const [sortDir, setSortDir]         = useState('desc')
  const [menuOpenId, setMenuOpenId]   = useState(null)
  const [viewUser, setViewUser]       = useState(null)
  const [statusMap, setStatusMap]     = useState(loadStatusOverrides)
  const [copied, setCopied]           = useState(false)
  const [activityUser, setActivityUser] = useState(null)
  const [activityTab, setActivityTab]   = useState('applications')

  const menuRef = useRef(null)

  // 메뉴 외부 클릭 닫기
  useEffect(() => {
    function handler(e) {
      if (menuRef.current && !menuRef.current.contains(e.target)) setMenuOpenId(null)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  // 유저 목록 구성
  const allUsers = useMemo(() => {
    const registered = loadRegisteredUsers()
    const registeredEmails = new Set(registered.map(u => u.email))
    const mockOnly = RECENT_USERS.filter(u => !registeredEmails.has(u.email))
    return [...registered, ...mockOnly].map(u => ({
      ...u,
      isActive: statusMap[u.id] !== undefined ? statusMap[u.id] : u.isActive,
    }))
  }, [statusMap])

  // 검색 (대소문자 무시)
  const searched = useMemo(() => {
    const q = query.toLowerCase()
    if (!q) return allUsers
    return allUsers.filter(u =>
      u.name?.toLowerCase().includes(q) ||
      u.email?.toLowerCase().includes(q)
    )
  }, [allUsers, query])

  // 정렬
  const filtered = useMemo(() => {
    return [...searched].sort((a, b) => {
      let va = a[sortKey] ?? ''
      let vb = b[sortKey] ?? ''
      if (sortKey === 'isActive') { va = a.isActive ? 1 : 0; vb = b.isActive ? 1 : 0 }
      const cmp = String(va).localeCompare(String(vb), 'ko')
      return sortDir === 'asc' ? cmp : -cmp
    })
  }, [searched, sortKey, sortDir])

  function handleSort(key) {
    if (sortKey === key) setSortDir(d => d === 'asc' ? 'desc' : 'asc')
    else { setSortKey(key); setSortDir('asc') }
  }

  function toggleStatus(userId) {
    setStatusMap(prev => {
      const user = allUsers.find(u => u.id === userId)
      const current = prev[userId] !== undefined ? prev[userId] : (user?.isActive ?? true)
      const next = { ...prev, [userId]: !current }
      saveStatusOverrides(next)
      return next
    })
    setMenuOpenId(null)
  }

  function copyEmail(email) {
    navigator.clipboard.writeText(email).catch(() => {})
    setCopied(true)
    setTimeout(() => setCopied(false), 1500)
    setMenuOpenId(null)
  }

  const sortProps = { current: sortKey, dir: sortDir, onSort: handleSort }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">유저 관리</h1>
        <p className="text-sm text-gray-500 mt-1">전체 회원 {allUsers.length}명</p>
      </div>

      <div className="flex items-center gap-2 bg-white border border-offwhite-200 rounded-xl px-4 py-2.5 max-w-sm">
        <Search size={15} className="text-gray-400" />
        <input
          type="text"
          placeholder="이름, 이메일 검색..."
          value={query}
          onChange={e => setQuery(e.target.value)}
          className="bg-transparent text-sm outline-none w-full placeholder-gray-400 text-navy"
        />
      </div>

      <div className="bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-offwhite-200 bg-offwhite">
              <SortTh label="이름"  sortKey="name"     {...sortProps} />
              <SortTh label="유형"  sortKey="role"     {...sortProps} />
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 hidden md:table-cell">이메일</th>
              <SortTh label="가입일" sortKey="joinedAt" {...sortProps} className="hidden lg:table-cell" />
              <SortTh label="상태"  sortKey="isActive" {...sortProps} />
              <th className="px-5 py-3" />
            </tr>
          </thead>
          <tbody className="divide-y divide-offwhite-200">
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-5 py-10 text-center text-sm text-gray-400">
                  검색 결과가 없습니다.
                </td>
              </tr>
            ) : filtered.map(u => (
              <tr key={u.id} className="hover:bg-offwhite transition-colors">
                <td className="px-5 py-3.5">
                  <button
                    onClick={() => { setActivityUser(u); setActivityTab(u.role === 'BUSINESS' ? 'jobs' : 'applications') }}
                    className="font-semibold text-navy hover:text-orange hover:underline underline-offset-2 transition-colors text-left"
                  >
                    {u.name}
                  </button>
                </td>
                <td className="px-5 py-3.5">
                  <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${ROLE_COLOR[u.role] ?? 'bg-gray-100 text-gray-600'}`}>
                    {ROLE_LABEL[u.role] ?? u.role}
                  </span>
                </td>
                <td className="px-5 py-3.5 text-gray-500 hidden md:table-cell">{u.email}</td>
                <td className="px-5 py-3.5 text-gray-500 hidden lg:table-cell">{u.joinedAt}</td>
                <td className="px-5 py-3.5">
                  <span className={`flex items-center gap-1.5 text-xs font-medium ${u.isActive ? 'text-green-600' : 'text-gray-400'}`}>
                    <span className={`w-1.5 h-1.5 rounded-full ${u.isActive ? 'bg-green-500' : 'bg-gray-300'}`} />
                    {u.isActive ? '활성' : '비활성'}
                  </span>
                </td>
                <td className="px-5 py-3.5 relative" ref={menuOpenId === u.id ? menuRef : null}>
                  <button
                    onClick={() => setMenuOpenId(prev => prev === u.id ? null : u.id)}
                    className="p-1 rounded hover:bg-offwhite text-gray-400 hover:text-navy transition-colors"
                  >
                    <MoreVertical size={15} />
                  </button>

                  {menuOpenId === u.id && (
                    <div className="absolute right-4 top-10 w-40 bg-white rounded-xl shadow-lg border border-offwhite-200 z-50 py-1 animate-slide-up">
                      <button
                        onClick={() => { setViewUser(u); setMenuOpenId(null) }}
                        className="w-full flex items-center gap-2.5 px-4 py-2.5 text-sm text-gray-600 hover:bg-offwhite-100 transition-colors"
                      >
                        <User size={13} className="text-gray-400" />상세 보기
                      </button>
                      <button
                        onClick={() => copyEmail(u.email)}
                        className="w-full flex items-center gap-2.5 px-4 py-2.5 text-sm text-gray-600 hover:bg-offwhite-100 transition-colors"
                      >
                        <Mail size={13} className="text-gray-400" />이메일 복사
                      </button>
                      <div className="border-t border-offwhite-200 my-1" />
                      <button
                        onClick={() => toggleStatus(u.id)}
                        className={`w-full flex items-center gap-2.5 px-4 py-2.5 text-sm transition-colors ${
                          u.isActive
                            ? 'text-red-500 hover:bg-red-50'
                            : 'text-green-600 hover:bg-green-50'
                        }`}
                      >
                        {u.isActive
                          ? <><ShieldOff size={13} />계정 정지</>
                          : <><ShieldCheck size={13} />정지 해제</>
                        }
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 상세 보기 모달 */}
      {viewUser && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setViewUser(null)}
        >
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm mx-4 overflow-hidden">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
              <h3 className="font-bold text-navy">회원 상세</h3>
              <button onClick={() => setViewUser(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="p-5 space-y-4">
              {/* 아바타 */}
              <div className="flex items-center gap-4">
                <div className="w-14 h-14 rounded-full bg-navy/10 text-navy font-bold text-xl flex items-center justify-center shrink-0">
                  {viewUser.name?.[0] ?? '?'}
                </div>
                <div>
                  <p className="font-bold text-navy text-base">{viewUser.name}</p>
                  <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${ROLE_COLOR[viewUser.role] ?? 'bg-gray-100 text-gray-600'}`}>
                    {ROLE_LABEL[viewUser.role] ?? viewUser.role}
                  </span>
                </div>
              </div>

              <div className="space-y-3 pt-1">
                {[
                  { icon: Mail,     label: '이메일', value: viewUser.email },
                  { icon: Calendar, label: '가입일', value: viewUser.joinedAt },
                ].map(({ icon: Icon, label, value }) => (
                  <div key={label} className="flex items-center gap-3">
                    <Icon size={14} className="text-gray-400 shrink-0" />
                    <span className="text-xs text-gray-400 w-12 shrink-0">{label}</span>
                    <span className="text-sm text-navy">{value}</span>
                  </div>
                ))}
                <div className="flex items-center gap-3">
                  <span className={`w-2 h-2 rounded-full ${viewUser.isActive ? 'bg-green-500' : 'bg-gray-300'}`} />
                  <span className="text-xs text-gray-400 w-12 shrink-0">상태</span>
                  <span className={`text-sm font-medium ${viewUser.isActive ? 'text-green-600' : 'text-gray-400'}`}>
                    {viewUser.isActive ? '활성' : '비활성'}
                  </span>
                </div>
              </div>
            </div>
            <div className="px-5 pb-5 flex gap-2">
              <button
                onClick={() => { toggleStatus(viewUser.id); setViewUser(null) }}
                className={`flex-1 py-2.5 text-sm font-semibold rounded-xl transition-colors ${
                  viewUser.isActive
                    ? 'text-red-500 border border-red-200 hover:bg-red-50'
                    : 'text-green-600 border border-green-200 hover:bg-green-50'
                }`}
              >
                {viewUser.isActive ? '계정 정지' : '정지 해제'}
              </button>
              <button
                onClick={() => setViewUser(null)}
                className="flex-1 py-2.5 text-sm font-semibold text-gray-500 border border-offwhite-200 rounded-xl hover:bg-offwhite-100 transition-colors"
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 이메일 복사 토스트 */}
      {copied && (
        <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2 bg-green-600 text-white text-sm font-semibold px-5 py-3 rounded-xl shadow-lg">
          <Check size={15} />이메일이 복사되었습니다
        </div>
      )}

      {/* ── 활동 기록 모달 ── */}
      {activityUser && (() => {
        const isIndividual = activityUser.role === 'INDIVIDUAL'
        const activity = USER_ACTIVITY[activityUser.id] ?? {}
        const indTabs = [
          { key: 'applications', label: '지원 이력', icon: FileText },
          { key: 'ratings',      label: '별점',      icon: Star },
          { key: 'attendance',   label: '근태 기록', icon: Clock },
        ]
        const bizTabs = [
          { key: 'jobs',    label: '등록 공고',  icon: Briefcase },
          { key: 'hiring',  label: '채용 활동',  icon: Building2 },
        ]
        const tabs = isIndividual ? indTabs : bizTabs

        return (
          <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
            onClick={e => e.target === e.currentTarget && setActivityUser(null)}
          >
            <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 overflow-hidden flex flex-col max-h-[85vh]">
              {/* 헤더 */}
              <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200 shrink-0">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-navy/10 text-navy font-bold flex items-center justify-center shrink-0">
                    {activityUser.name?.[0] ?? '?'}
                  </div>
                  <div>
                    <p className="font-bold text-navy">{activityUser.name}</p>
                    <span className={`text-[10px] font-semibold px-1.5 py-0.5 rounded-full ${ROLE_COLOR[activityUser.role] ?? 'bg-gray-100 text-gray-600'}`}>
                      {ROLE_LABEL[activityUser.role] ?? activityUser.role}
                    </span>
                  </div>
                </div>
                <button onClick={() => setActivityUser(null)} className="text-gray-400 hover:text-navy transition-colors">
                  <X size={18} />
                </button>
              </div>

              {/* 탭 */}
              <div className="flex gap-1 px-4 pt-3 pb-0 border-b border-offwhite-200 shrink-0">
                {tabs.map(t => (
                  <button
                    key={t.key}
                    onClick={() => setActivityTab(t.key)}
                    className={`flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-t-lg border-b-2 transition-colors ${
                      activityTab === t.key
                        ? 'border-navy text-navy bg-offwhite'
                        : 'border-transparent text-gray-400 hover:text-navy'
                    }`}
                  >
                    <t.icon size={12} />{t.label}
                  </button>
                ))}
              </div>

              {/* 컨텐츠 */}
              <div className="overflow-y-auto flex-1">
                {/* 개인: 지원 이력 */}
                {activityTab === 'applications' && (
                  <div className="divide-y divide-offwhite-200">
                    {(activity.applications ?? []).length === 0 ? (
                      <EmptyActivity label="지원 이력이 없습니다" />
                    ) : (activity.applications ?? []).map(a => (
                      <div key={a.id} className="px-5 py-3.5 flex items-start justify-between gap-4 hover:bg-offwhite-100 transition-colors">
                        <div className="min-w-0">
                          <p className="text-sm font-semibold text-navy truncate">{a.job}</p>
                          <p className="text-xs text-gray-400 mt-0.5">{a.company} · {a.date}</p>
                        </div>
                        <span className={`text-[11px] font-bold px-2.5 py-1 rounded-full shrink-0 ${APP_STATUS_COLOR[a.status] ?? 'bg-gray-100 text-gray-500'}`}>
                          {a.status}
                        </span>
                      </div>
                    ))}
                  </div>
                )}

                {/* 개인: 별점 */}
                {activityTab === 'ratings' && (
                  <div className="divide-y divide-offwhite-200">
                    {(activity.ratings ?? []).length === 0 ? (
                      <EmptyActivity label="별점 기록이 없습니다" />
                    ) : (activity.ratings ?? []).map((r, i) => (
                      <div key={i} className="px-5 py-4 hover:bg-offwhite-100 transition-colors">
                        <div className="flex items-center justify-between gap-3 mb-1.5">
                          <p className="text-sm font-bold text-navy">{r.company}</p>
                          <div className="flex items-center gap-1 shrink-0">
                            <Star size={13} className="text-yellow-400 fill-yellow-400" />
                            <span className="text-sm font-extrabold text-navy tabular-nums">{r.score.toFixed(1)}</span>
                          </div>
                        </div>
                        <p className="text-xs text-gray-500 leading-relaxed">{r.comment}</p>
                        <p className="text-[10px] text-gray-400 mt-1.5">{r.date}</p>
                      </div>
                    ))}
                    {(activity.ratings ?? []).length > 0 && (
                      <div className="px-5 py-3 bg-offwhite-100 flex items-center justify-between">
                        <span className="text-xs text-gray-500">평균 별점</span>
                        <div className="flex items-center gap-1">
                          <Star size={12} className="text-yellow-400 fill-yellow-400" />
                          <span className="text-sm font-extrabold text-navy">
                            {((activity.ratings ?? []).reduce((s, r) => s + r.score, 0) / (activity.ratings ?? []).length).toFixed(1)}
                          </span>
                          <span className="text-xs text-gray-400">/ 5.0</span>
                        </div>
                      </div>
                    )}
                  </div>
                )}

                {/* 개인: 근태 기록 */}
                {activityTab === 'attendance' && (
                  <div className="divide-y divide-offwhite-200">
                    {(activity.attendance ?? []).length === 0 ? (
                      <EmptyActivity label="근태 기록이 없습니다" />
                    ) : (activity.attendance ?? []).map((a, i) => (
                      <div key={i} className="px-5 py-3.5 flex items-center justify-between hover:bg-offwhite-100 transition-colors">
                        <div>
                          <p className="text-sm font-medium text-navy">{a.job}</p>
                          <p className="text-xs text-gray-400 mt-0.5">{a.date}</p>
                        </div>
                        <span className="text-[11px] font-bold px-2.5 py-1 rounded-full bg-green-100 text-green-700">
                          {a.status}
                        </span>
                      </div>
                    ))}
                    {(activity.attendance ?? []).length > 0 && (
                      <div className="px-5 py-3 bg-offwhite-100 flex items-center justify-between">
                        <span className="text-xs text-gray-500">노쇼 횟수</span>
                        <span className="text-sm font-extrabold text-green-600">0회</span>
                      </div>
                    )}
                  </div>
                )}

                {/* 기업: 등록 공고 */}
                {activityTab === 'jobs' && (
                  <div className="divide-y divide-offwhite-200">
                    {(activity.postedJobs ?? []).length === 0 ? (
                      <EmptyActivity label="등록된 공고가 없습니다" />
                    ) : (activity.postedJobs ?? []).map(j => (
                      <div key={j.id} className="px-5 py-3.5 hover:bg-offwhite-100 transition-colors">
                        <div className="flex items-start justify-between gap-3">
                          <div className="min-w-0">
                            <p className="text-sm font-semibold text-navy truncate">{j.title}</p>
                            <p className="text-xs text-gray-400 mt-0.5">{j.date} · 지원 {j.applicants}명 · 채용 {j.hired}명</p>
                          </div>
                          <span className={`text-[11px] font-bold px-2.5 py-1 rounded-full shrink-0 ${JOB_STATUS_COLOR[j.status] ?? 'bg-gray-100 text-gray-500'}`}>
                            {j.status}
                          </span>
                        </div>
                      </div>
                    ))}
                    {(activity.postedJobs ?? []).length > 0 && (
                      <div className="px-5 py-3 bg-offwhite-100 flex items-center justify-between">
                        <span className="text-xs text-gray-500">총 채용 인원</span>
                        <span className="text-sm font-extrabold text-navy">
                          {(activity.postedJobs ?? []).reduce((s, j) => s + j.hired, 0)}명
                        </span>
                      </div>
                    )}
                  </div>
                )}

                {/* 기업: 채용 활동 */}
                {activityTab === 'hiring' && (
                  <div className="divide-y divide-offwhite-200">
                    {(activity.hiringActivity ?? []).length === 0 ? (
                      <EmptyActivity label="채용 활동 기록이 없습니다" />
                    ) : (activity.hiringActivity ?? []).map((h, i) => (
                      <div key={i} className="px-5 py-3.5 flex items-start gap-3 hover:bg-offwhite-100 transition-colors">
                        <div className="w-1.5 h-1.5 rounded-full bg-navy mt-2 shrink-0" />
                        <div>
                          <p className="text-sm font-medium text-navy">{h.action}</p>
                          <p className="text-xs text-gray-400 mt-0.5">{h.job} · {h.date}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* 푸터 */}
              <div className="px-5 py-3 border-t border-offwhite-200 shrink-0">
                <button
                  onClick={() => setActivityUser(null)}
                  className="w-full py-2 text-sm font-semibold text-gray-500 border border-offwhite-200 rounded-xl hover:bg-offwhite-100 transition-colors"
                >
                  닫기
                </button>
              </div>
            </div>
          </div>
        )
      })()}
    </div>
  )
}

function EmptyActivity({ label }) {
  return (
    <div className="py-12 text-center text-sm text-gray-400">
      <AlertTriangle size={28} className="mx-auto mb-2 text-gray-200" />
      {label}
    </div>
  )
}
