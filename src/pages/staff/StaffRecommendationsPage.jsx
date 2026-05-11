import { useState } from 'react'
import { Star, X, Send } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { MOCK_APPLICANTS } from '../../data/mockApplicants'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'

const AVATAR_COLORS = [
  'bg-navy', 'bg-orange', 'bg-emerald-500', 'bg-violet-500',
  'bg-sky-500', 'bg-rose-500', 'bg-amber-500', 'bg-teal-500',
]

const TIER_FILTERS = [
  { key: 'all', label: '전체' },
  { key: 'top', label: '고수 (4.5+)' },
  { key: 'mid', label: '실력자 (3.8–4.4)' },
  { key: 'new', label: '신규' },
]

function getTier(rating) {
  if (rating === null) return 'new'
  if (rating >= 4.5) return 'top'
  if (rating >= 3.8) return 'mid'
  return 'low'
}

function StarRow({ rating }) {
  if (rating === null) return <span className="text-xs text-gray-400">신규</span>
  const full = Math.floor(rating)
  return (
    <span className="flex items-center gap-0.5">
      {Array.from({ length: 5 }).map((_, i) => (
        <Star
          key={i}
          size={11}
          className={i < full ? 'fill-yellow-400 text-yellow-400' : 'text-gray-200 fill-gray-200'}
        />
      ))}
      <span className="text-xs text-yellow-600 font-semibold ml-1">{rating}</span>
    </span>
  )
}

// ── 초대 발송 미니 모달 ────────────────────────────────────
function InviteModal({ person, myShifts, onSend, onClose }) {
  const [shiftId, setShiftId] = useState('')
  const [role, setRole] = useState('')
  const [wage, setWage] = useState('시급 13,000원')

  const selectedShift = myShifts.find(s => s.id === shiftId)

  function handleSend() {
    if (!shiftId || !role.trim()) return
    const d = new Date(selectedShift.date + 'T00:00:00')
    onSend({
      staffId: person.id,
      staffName: person.name,
      role,
      shiftId,
      shiftLabel: `${selectedShift.jobTitle} · ${d.getMonth() + 1}월 ${d.getDate()}일 ${selectedShift.startTime}–${selectedShift.endTime}`,
      jobId: selectedShift.jobId,
      wage,
    })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm mx-4 p-6 space-y-4" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-navy">초대 발송</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600"><X size={16} /></button>
        </div>

        {/* 대상 스태프 */}
        <div className="flex items-center gap-3 p-3 bg-offwhite rounded-xl">
          <div className={`w-9 h-9 rounded-full ${AVATAR_COLORS[MOCK_APPLICANTS.indexOf(person) % AVATAR_COLORS.length]} flex items-center justify-center shrink-0`}>
            <span className="text-white text-sm font-bold">{person.name[0]}</span>
          </div>
          <div>
            <p className="text-sm font-semibold text-navy">{person.name}</p>
            <p className="text-xs text-gray-400">{person.age}세 · {person.region} · ★{person.rating ?? '신규'}</p>
          </div>
        </div>

        {/* Shift 선택 */}
        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Shift 선택 *</label>
          <select
            value={shiftId}
            onChange={e => setShiftId(e.target.value)}
            className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm text-navy outline-none focus:border-navy bg-white"
          >
            <option value="">Shift를 선택하세요</option>
            {myShifts.map(s => {
              const d = new Date(s.date + 'T00:00:00')
              return (
                <option key={s.id} value={s.id}>
                  {s.jobTitle} · {d.getMonth() + 1}월 {d.getDate()}일 {s.startTime}–{s.endTime}
                </option>
              )
            })}
          </select>
        </div>

        {/* 역할 */}
        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">역할 *</label>
          <input
            type="text"
            placeholder="예: 행사 안내 스태프"
            value={role}
            onChange={e => setRole(e.target.value)}
            className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm text-navy outline-none focus:border-navy"
          />
        </div>

        {/* 급여 */}
        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">급여</label>
          <input
            type="text"
            value={wage}
            onChange={e => setWage(e.target.value)}
            className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm text-navy outline-none focus:border-navy"
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
            onClick={handleSend}
            disabled={!shiftId || !role.trim()}
            className="flex-1 py-2.5 rounded-xl bg-orange text-white text-sm font-bold hover:bg-orange-600 transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <Send size={14} />발송
          </button>
        </div>
      </div>
    </div>
  )
}

// ── 메인 페이지 ───────────────────────────────────────────
export default function StaffRecommendationsPage() {
  const { addInvitation, shifts, jobs } = useAppData()
  const { user } = useAuth()
  const [filter, setFilter] = useState('all')
  const [search, setSearch] = useState('')
  const [inviteTarget, setInviteTarget] = useState(null)

  const isAdmin = user?.role === 'ADMIN'
  const myJobIds = isAdmin ? null : new Set(jobs.filter(j => j.createdBy === user?.name).map(j => j.id))
  const myShifts = (isAdmin ? shifts : shifts.filter(s => myJobIds.has(s.jobId)))
    .filter(s => s.status === 'scheduled' || s.status === 'in_progress')

  const visible = MOCK_APPLICANTS.filter(p => {
    const tierMatch =
      filter === 'all' ||
      (filter === 'top' && getTier(p.rating) === 'top') ||
      (filter === 'mid' && getTier(p.rating) === 'mid') ||
      (filter === 'new' && getTier(p.rating) === 'new')
    const searchMatch = !search || p.name.includes(search) || p.region.includes(search)
    return tierMatch && searchMatch
  })

  function handleSend(data) {
    addInvitation(data)
    setInviteTarget(null)
  }

  return (
    <div className="space-y-5">
      {inviteTarget && (
        <InviteModal
          person={inviteTarget}
          myShifts={myShifts}
          onSend={handleSend}
          onClose={() => setInviteTarget(null)}
        />
      )}

      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
        <div>
          <h1 className="text-xl font-bold text-navy">추천 인력</h1>
          <p className="text-sm text-gray-500 mt-0.5">총 {MOCK_APPLICANTS.length}명의 인력 풀</p>
        </div>
        <input
          type="text"
          placeholder="이름, 지역 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border border-offwhite-200 rounded-lg px-3 py-1.5 text-sm text-navy placeholder-gray-400 outline-none focus:border-navy w-full sm:w-44"
        />
      </div>

      {/* 티어 필터 탭 */}
      <div className="flex gap-1 overflow-x-auto scrollbar-hide pb-2">
        {TIER_FILTERS.map(t => {
          const count = t.key === 'all'
            ? MOCK_APPLICANTS.length
            : MOCK_APPLICANTS.filter(p =>
                t.key === 'top' ? getTier(p.rating) === 'top' :
                t.key === 'mid' ? getTier(p.rating) === 'mid' :
                getTier(p.rating) === 'new'
              ).length
          return (
            <button
              key={t.key}
              onClick={() => setFilter(t.key)}
              className={`shrink-0 px-3 py-1.5 rounded-lg text-sm font-medium transition-all flex items-center gap-1.5
                ${filter === t.key ? 'bg-navy text-white' : 'text-gray-500 hover:bg-offwhite-100 hover:text-navy'}`}
            >
              {t.label}
              <span className={`text-xs tabular-nums px-1.5 rounded-md
                ${filter === t.key ? 'bg-white/20 text-white' : 'bg-offwhite-200 text-gray-500'}`}>
                {count}
              </span>
            </button>
          )
        })}
      </div>

      {visible.length === 0 ? (
        <Card>
          <p className="text-center text-sm text-gray-400 py-8">검색 결과가 없습니다</p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {visible.map((person, idx) => (
            <Card key={person.id}>
              <div className="flex items-start gap-3">
                <div className={`w-10 h-10 rounded-full ${AVATAR_COLORS[idx % AVATAR_COLORS.length]} flex items-center justify-center shrink-0`}>
                  <span className="text-white text-sm font-bold">{person.name[0]}</span>
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between gap-1">
                    <p className="font-semibold text-navy text-sm">{person.name}</p>
                    {person.rating !== null && (
                      <span className="text-xs text-yellow-600 font-semibold shrink-0">★{person.rating}</span>
                    )}
                  </div>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {person.age}세 · {person.gender} · {person.region}
                  </p>
                  <div className="flex items-center gap-2 mt-1.5">
                    <StarRow rating={person.rating} />
                    {person.hireCount > 0 && (
                      <span className="text-xs text-orange font-semibold">고용 {person.hireCount}회</span>
                    )}
                  </div>
                </div>
              </div>
              <div className="mt-3 pt-3 border-t border-offwhite-200">
                <Button
                  size="sm"
                  className="w-full justify-center"
                  onClick={() => setInviteTarget(person)}
                >
                  초대 보내기
                </Button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
