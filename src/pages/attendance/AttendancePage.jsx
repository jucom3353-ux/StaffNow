import { useState, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { Clock, CheckCircle2, AlertCircle, Users, ChevronLeft, ChevronRight, ChevronUp, ChevronDown, DollarSign } from 'lucide-react'
import Card from '../../components/ui/Card'
import EmptyState from '../../components/ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'
import { MOCK_APPLICANTS } from '../../data/mockApplicants'
import { SHARED_ATTENDANCE_KEY } from '../../hooks/useAttendance'

function loadLiveAttendance() {
  try {
    const raw = localStorage.getItem(SHARED_ATTENDANCE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}

const applicantMap = Object.fromEntries(MOCK_APPLICANTS.map(a => [a.id, a]))

const TABS = [
  { key: 'all',         label: '전체' },
  { key: 'in_progress', label: '출근 중' },
  { key: 'completed',   label: '완료' },
  { key: 'absent',      label: '결근' },
  { key: 'scheduled',   label: '예정' },
]

const STATUS_META = {
  completed:   { label: '완료',    color: 'text-green-600 bg-green-50 border-green-200' },
  in_progress: { label: '출근 중', color: 'text-blue-600 bg-blue-50 border-blue-200' },
  absent:      { label: '결근',    color: 'text-red-500 bg-red-50 border-red-200' },
  scheduled:   { label: '예정',    color: 'text-gray-500 bg-gray-50 border-gray-200' },
}

function StatusPill({ status }) {
  const meta = STATUS_META[status] ?? { label: status, color: 'text-gray-500 bg-gray-50 border-gray-200' }
  return (
    <span className={`inline-flex text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
      {meta.label}
    </span>
  )
}

function SortableHeader({ label, sortKey, sort, onSort, className = '' }) {
  const active = sort.key === sortKey
  return (
    <th
      className={`text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider cursor-pointer select-none hover:text-navy transition-colors ${className}`}
      onClick={() => onSort(sortKey)}
    >
      <span className="inline-flex items-center gap-1">
        {label}
        <span className="inline-flex flex-col -space-y-0.5">
          <ChevronUp  size={10} className={active && sort.dir === 'asc'  ? 'text-orange' : 'text-gray-300'} />
          <ChevronDown size={10} className={active && sort.dir === 'desc' ? 'text-orange' : 'text-gray-300'} />
        </span>
      </span>
    </th>
  )
}

function calcWorkHours(checkIn, checkOut) {
  if (!checkIn || !checkOut) return null
  const [h1, m1] = checkIn.split(':').map(Number)
  const [h2, m2] = checkOut.split(':').map(Number)
  const total = (h2 * 60 + m2) - (h1 * 60 + m1)
  if (total <= 0) return null
  const h = Math.floor(total / 60)
  const m = total % 60
  return m > 0 ? `${h}h ${m}m` : `${h}h`
}

// ── 달력 컴포넌트 ─────────────────────────────────────────
const KO_DAYS = ['일', '월', '화', '수', '목', '금', '토']

function AttendanceCalendar({ recordsByDate, selectedDate, onSelect }) {
  const initial = selectedDate
    ? new Date(selectedDate + 'T00:00:00')
    : new Date()
  const [viewYear, setViewYear] = useState(initial.getFullYear())
  const [viewMonth, setViewMonth] = useState(initial.getMonth()) // 0-indexed

  const datesWithData = new Set(Object.keys(recordsByDate))

  function prevMonth() {
    if (viewMonth === 0) { setViewYear(y => y - 1); setViewMonth(11) }
    else setViewMonth(m => m - 1)
  }
  function nextMonth() {
    if (viewMonth === 11) { setViewYear(y => y + 1); setViewMonth(0) }
    else setViewMonth(m => m + 1)
  }

  // 이번 달 날짜 그리드 계산
  const firstDay = new Date(viewYear, viewMonth, 1).getDay()   // 0=일
  const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate()
  const cells = []
  for (let i = 0; i < firstDay; i++) cells.push(null)
  for (let d = 1; d <= daysInMonth; d++) cells.push(d)
  // 6행 맞추기
  while (cells.length % 7 !== 0) cells.push(null)

  return (
    <Card>
      {/* 월 헤더 */}
      <div className="flex items-center justify-between mb-4">
        <button onClick={prevMonth} className="w-8 h-8 rounded-lg hover:bg-offwhite-100 flex items-center justify-center text-gray-500 hover:text-navy transition-colors">
          <ChevronLeft size={16} />
        </button>
        <span className="text-sm font-bold text-navy">
          {viewYear}년 {viewMonth + 1}월
        </span>
        <button onClick={nextMonth} className="w-8 h-8 rounded-lg hover:bg-offwhite-100 flex items-center justify-center text-gray-500 hover:text-navy transition-colors">
          <ChevronRight size={16} />
        </button>
      </div>

      {/* 요일 헤더 */}
      <div className="grid grid-cols-7 mb-1">
        {KO_DAYS.map((d, i) => (
          <div key={d} className={`text-center text-xs font-semibold py-1
            ${i === 0 ? 'text-red-400' : i === 6 ? 'text-blue-400' : 'text-gray-400'}`}>
            {d}
          </div>
        ))}
      </div>

      {/* 날짜 셀 */}
      <div className="grid grid-cols-7 gap-y-1">
        {cells.map((day, idx) => {
          if (!day) return <div key={`e-${idx}`} />
          const dateStr = `${viewYear}-${String(viewMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`
          const hasData = datesWithData.has(dateStr)
          const isSelected = selectedDate === dateStr
          const dayOfWeek = (firstDay + day - 1) % 7

          return (
            <button
              key={dateStr}
              onClick={() => hasData && onSelect(isSelected ? null : dateStr)}
              disabled={!hasData}
              className={`relative flex flex-col items-center justify-center h-9 rounded-lg text-sm font-medium transition-all
                ${isSelected
                  ? 'bg-navy text-white'
                  : hasData
                    ? 'hover:bg-navy-50 text-navy cursor-pointer'
                    : 'text-gray-300 cursor-default'}
                ${dayOfWeek === 0 && !isSelected ? 'text-red-400' : ''}
                ${dayOfWeek === 6 && !isSelected ? 'text-blue-400' : ''}
              `}
            >
              {day}
              {hasData && !isSelected && (
                <span className="absolute bottom-1 w-1 h-1 rounded-full bg-orange" />
              )}
            </button>
          )
        })}
      </div>

      {/* 범례 */}
      <div className="flex items-center gap-4 mt-3 pt-3 border-t border-offwhite-100">
        <span className="flex items-center gap-1.5 text-xs text-gray-400">
          <span className="w-2 h-2 rounded-full bg-orange inline-block" />
          근태 기록 있음
        </span>
        <span className="flex items-center gap-1.5 text-xs text-gray-400">
          <span className="w-6 h-5 rounded-md bg-navy inline-block" />
          선택된 날짜
        </span>
      </div>
    </Card>
  )
}

// ── 메인 페이지 ───────────────────────────────────────────
export default function AttendancePage() {
  const { shifts, jobs } = useAppData()
  const { user } = useAuth()
  const [tab, setTab] = useState('all')
  const [sort, setSort] = useState({ key: null, dir: 'asc' })

  function handleSort(key) {
    setSort(prev =>
      prev.key === key
        ? { key, dir: prev.dir === 'asc' ? 'desc' : 'asc' }
        : { key, dir: 'asc' }
    )
  }

  const isAdmin = user?.role === 'ADMIN'
  const myJobIds = isAdmin ? null : new Set(jobs.filter(j => j.createdBy === user?.name).map(j => j.id))
  const myShifts = isAdmin ? shifts : shifts.filter(s => myJobIds.has(s.jobId))

  // 완료된 Shift → attendance 파생
  const allRecords = useMemo(() => {
    const mockRecords = myShifts
      .filter(s => s.status === 'completed' && s.attendance?.length)
      .flatMap(s => {
        const d = new Date(s.date + 'T00:00:00')
        const shiftLabel = `${s.jobTitle} · ${d.getMonth() + 1}월 ${d.getDate()}일`
        return s.attendance.map(a => ({
          id: `${s.id}-${a.id}`,
          staff: applicantMap[a.id]?.name ?? a.id,
          role: a.role,
          shift: shiftLabel,
          shiftDate: s.date,
          checkIn: a.checkIn,
          checkOut: a.checkOut,
          workHours: calcWorkHours(a.checkIn, a.checkOut),
          status: a.attendanceStatus,
        }))
      })

    // 인력 앱에서 실시간 출퇴근 기록 병합
    const live = loadLiveAttendance().map(r => ({
      id:        `live-${r.shiftId}`,
      staff:     r.userName,
      role:      r.jobTitle,
      shift:     `${r.jobTitle} · ${r.company}`,
      shiftDate: r.shiftDate,
      checkIn:   r.checkIn,
      checkOut:  r.checkOut,
      workHours: calcWorkHours(r.checkIn, r.checkOut),
      status:    r.status,
    }))

    // 중복 방지: live에 있으면 mock은 제외
    const liveIds = new Set(live.map(r => r.staff + r.shiftDate))
    const filtered = mockRecords.filter(r => !liveIds.has(r.staff + r.shiftDate))
    return [...live, ...filtered]
  }, [myShifts])

  // 날짜별 그룹
  const recordsByDate = useMemo(() => {
    const map = {}
    allRecords.forEach(r => {
      if (!map[r.shiftDate]) map[r.shiftDate] = []
      map[r.shiftDate].push(r)
    })
    return map
  }, [allRecords])

  // 기본 선택 날짜: 데이터 있는 날 중 가장 최근
  const latestDate = useMemo(() =>
    Object.keys(recordsByDate).sort().at(-1) ?? null
  , [recordsByDate])

  const [selectedDate, setSelectedDate] = useState(latestDate)

  const dayRecords = selectedDate ? (recordsByDate[selectedDate] ?? []) : allRecords

  const filtered = useMemo(() => {
    const base = dayRecords.filter(a => tab === 'all' || a.status === tab)
    if (!sort.key) return base

    return [...base].sort((a, b) => {
      let av = a[sort.key] ?? ''
      let bv = b[sort.key] ?? ''

      // 시간 문자열 → 분으로 변환해 숫자 비교
      if (sort.key === 'checkIn' || sort.key === 'checkOut') {
        const toMin = t => { if (!t) return sort.dir === 'asc' ? Infinity : -Infinity; const [h,m] = t.split(':').map(Number); return h*60+m }
        av = toMin(av); bv = toMin(bv)
      }
      // 근무시간: workHours 대신 실제 분 계산
      if (sort.key === 'workHours') {
        const toMin = t => { if (!t) return sort.dir === 'asc' ? Infinity : -Infinity; const m = t.match(/(\d+)h(?:\s*(\d+)m)?/); return m ? parseInt(m[1])*60+(parseInt(m[2])||0) : 0 }
        av = toMin(av); bv = toMin(bv)
      }

      if (av < bv) return sort.dir === 'asc' ? -1 : 1
      if (av > bv) return sort.dir === 'asc' ?  1 : -1
      return 0
    })
  }, [dayRecords, tab, sort])

  const counts = {
    in_progress: dayRecords.filter(a => a.status === 'in_progress').length,
    completed:   dayRecords.filter(a => a.status === 'completed').length,
    absent:      dayRecords.filter(a => a.status === 'absent').length,
    scheduled:   dayRecords.filter(a => a.status === 'scheduled').length,
  }

  const selectedLabel = selectedDate
    ? (() => { const d = new Date(selectedDate + 'T00:00:00'); return `${d.getMonth() + 1}월 ${d.getDate()}일` })()
    : '전체'

  return (
    <div className="space-y-5">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-navy">근태 관리</h1>
          <p className="text-sm text-gray-500 mt-0.5">스태프 출퇴근 기록 및 현황</p>
        </div>
        <Link
          to="/payroll"
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-orange text-white text-sm font-semibold hover:bg-orange-600 transition-colors"
        >
          <DollarSign size={14} />정산 관리
        </Link>
      </div>

      {/* 달력 + 요약 카드 2단 레이아웃 */}
      <div className="grid grid-cols-[320px_1fr] gap-5 items-start">
        <AttendanceCalendar
          recordsByDate={recordsByDate}
          selectedDate={selectedDate}
          onSelect={setSelectedDate}
        />

        <div className="space-y-3">
          <p className="text-sm font-semibold text-navy">
            {selectedDate ? `${selectedLabel} 근태 현황` : '전체 근태 현황'}
            <span className="ml-2 text-gray-400 font-normal text-xs">
              {dayRecords.length}명
            </span>
          </p>
          <div className="grid grid-cols-2 gap-3">
            {[
              { label: '출근 중', value: counts.in_progress, icon: Clock,        color: 'text-blue-500',  bg: 'bg-blue-50' },
              { label: '완료',    value: counts.completed,   icon: CheckCircle2, color: 'text-green-600', bg: 'bg-green-50' },
              { label: '결근',    value: counts.absent,      icon: AlertCircle,  color: 'text-red-500',   bg: 'bg-red-50' },
              { label: '예정',    value: counts.scheduled,   icon: Users,        color: 'text-gray-500',  bg: 'bg-gray-50' },
            ].map(({ label, value, icon: Icon, color, bg }) => (
              <Card key={label}>
                <div className="flex items-center gap-3">
                  <div className={`w-9 h-9 rounded-lg ${bg} flex items-center justify-center shrink-0`}>
                    <Icon size={16} className={color} />
                  </div>
                  <div>
                    <p className="text-xl font-bold text-navy tabular-nums">{value}</p>
                    <p className="text-xs text-gray-500">{label}</p>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </div>
      </div>

      {/* 탭 */}
      <div className="flex gap-1 border-b border-offwhite-200">
        {TABS.map(t => {
          const cnt = t.key === 'all' ? dayRecords.length : (counts[t.key] ?? 0)
          return (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`px-4 py-2.5 text-sm font-semibold transition-colors border-b-2 -mb-px flex items-center gap-1.5 ${
                tab === t.key ? 'border-orange text-orange' : 'border-transparent text-gray-500 hover:text-navy'
              }`}
            >
              {t.label}
              {cnt > 0 && (
                <span className={`text-xs tabular-nums px-1.5 rounded-md ${tab === t.key ? 'bg-orange/10 text-orange' : 'bg-offwhite-200 text-gray-500'}`}>
                  {cnt}
                </span>
              )}
            </button>
          )
        })}
      </div>

      {/* 테이블 */}
      {filtered.length === 0 ? (
        <Card>
          <EmptyState
            icon={Clock}
            title={selectedDate ? `${selectedLabel}에 근태 기록이 없습니다` : '근태 기록이 없습니다'}
            description="Shift를 완료·확정하면 근태 기록이 표시됩니다"
          />
        </Card>
      ) : (
        <Card padding={false}>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-offwhite-200 bg-offwhite-100">
                <SortableHeader label="스태프"   sortKey="staff"     sort={sort} onSort={handleSort} />
                <SortableHeader label="Shift"    sortKey="shift"     sort={sort} onSort={handleSort} className="hidden md:table-cell" />
                <SortableHeader label="체크인"   sortKey="checkIn"   sort={sort} onSort={handleSort} />
                <SortableHeader label="체크아웃" sortKey="checkOut"  sort={sort} onSort={handleSort} />
                <SortableHeader label="근무 시간" sortKey="workHours" sort={sort} onSort={handleSort} className="hidden md:table-cell" />
                <SortableHeader label="상태"     sortKey="status"    sort={sort} onSort={handleSort} />
              </tr>
            </thead>
            <tbody>
              {filtered.map(a => (
                <tr key={a.id} className="border-b border-offwhite-100 last:border-0 hover:bg-offwhite-100 transition-colors">
                  <td className="px-5 py-3.5">
                    <p className="font-semibold text-navy">{a.staff}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{a.role}</p>
                  </td>
                  <td className="px-5 py-3.5 text-gray-600 hidden md:table-cell">{a.shift}</td>
                  <td className="px-5 py-3.5 text-gray-700 font-medium tabular-nums">{a.checkIn ?? '—'}</td>
                  <td className="px-5 py-3.5 text-gray-700 font-medium tabular-nums">{a.checkOut ?? '—'}</td>
                  <td className="px-5 py-3.5 text-gray-500 hidden md:table-cell">
                    {a.workHours ? <span className="font-medium text-navy">{a.workHours}</span> : '—'}
                  </td>
                  <td className="px-5 py-3.5"><StatusPill status={a.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  )
}
