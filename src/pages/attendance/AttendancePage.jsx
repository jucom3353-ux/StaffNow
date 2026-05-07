import { useState } from 'react'
import { Clock, CheckCircle2, AlertCircle, Users } from 'lucide-react'
import Card from '../../components/ui/Card'
import StatusBadge from '../../components/ui/StatusBadge'

const MOCK_ATTENDANCE = [
  { id: 1, staff: '이영희', role: '부스 운영 보조',    shift: '주말 행사 스태프 · 5월 10일', checkIn: '09:02', checkOut: '18:05', status: 'completed',   workHours: '9h 3m' },
  { id: 2, staff: '홍길동', role: '행사 안내 스태프',  shift: '주말 행사 스태프 · 5월 10일', checkIn: '09:15', checkOut: null,    status: 'in_progress',  workHours: null },
  { id: 3, staff: '박지수', role: '안내 데스크',        shift: '주말 행사 스태프 · 5월 10일', checkIn: null,    checkOut: null,    status: 'absent',       workHours: null },
  { id: 4, staff: '김철수', role: '행사 진행 보조',    shift: '주말 행사 스태프 · 5월 10일', checkIn: '08:58', checkOut: '18:10', status: 'completed',   workHours: '9h 12m' },
  { id: 5, staff: '최민준', role: '행사 안내 스태프',  shift: '6월 박람회 안내 · 6월 1일',   checkIn: null,    checkOut: null,    status: 'scheduled',    workHours: null },
]

const TABS = [
  { key: 'all',         label: '전체' },
  { key: 'in_progress', label: '출근 중' },
  { key: 'completed',   label: '완료' },
  { key: 'absent',      label: '결근' },
  { key: 'scheduled',   label: '예정' },
]

const STATUS_META = {
  completed:   { label: '완료',   color: 'text-green-600 bg-green-50 border-green-200' },
  in_progress: { label: '출근 중', color: 'text-blue-600 bg-blue-50 border-blue-200' },
  absent:      { label: '결근',   color: 'text-red-500 bg-red-50 border-red-200' },
  scheduled:   { label: '예정',   color: 'text-gray-500 bg-gray-50 border-gray-200' },
}

function StatusPill({ status }) {
  const meta = STATUS_META[status] || { label: status, color: 'text-gray-500 bg-gray-50 border-gray-200' }
  return (
    <span className={`inline-flex text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
      {meta.label}
    </span>
  )
}

export default function AttendancePage() {
  const [tab, setTab] = useState('all')

  const filtered = MOCK_ATTENDANCE.filter(a => tab === 'all' || a.status === tab)

  const counts = {
    in_progress: MOCK_ATTENDANCE.filter(a => a.status === 'in_progress').length,
    completed:   MOCK_ATTENDANCE.filter(a => a.status === 'completed').length,
    absent:      MOCK_ATTENDANCE.filter(a => a.status === 'absent').length,
    scheduled:   MOCK_ATTENDANCE.filter(a => a.status === 'scheduled').length,
  }

  return (
    <div className="space-y-5">
      {/* 헤더 */}
      <div>
        <h1 className="text-xl font-bold text-navy">근태 관리</h1>
        <p className="text-sm text-gray-500 mt-0.5">스태프 출퇴근 기록 및 현황</p>
      </div>

      {/* 요약 */}
      <div className="grid grid-cols-4 gap-3">
        {[
          { label: '출근 중',  value: counts.in_progress, icon: Clock,         color: 'text-blue-500',  bg: 'bg-blue-50' },
          { label: '완료',     value: counts.completed,   icon: CheckCircle2,  color: 'text-green-600', bg: 'bg-green-50' },
          { label: '결근',     value: counts.absent,      icon: AlertCircle,   color: 'text-red-500',   bg: 'bg-red-50' },
          { label: '예정',     value: counts.scheduled,   icon: Users,         color: 'text-gray-500',  bg: 'bg-gray-50' },
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

      {/* 탭 */}
      <div className="flex gap-1 border-b border-offwhite-200">
        {TABS.map(t => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`px-4 py-2.5 text-sm font-semibold transition-colors border-b-2 -mb-px ${
              tab === t.key
                ? 'border-orange text-orange'
                : 'border-transparent text-gray-500 hover:text-navy'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* 테이블 */}
      <Card padding={false}>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-offwhite-200">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">스태프</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider hidden md:table-cell">Shift</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">체크인</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">체크아웃</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider hidden md:table-cell">근무 시간</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">상태</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(a => (
              <tr key={a.id} className="border-b border-offwhite-100 hover:bg-offwhite-100 transition-colors">
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
    </div>
  )
}
