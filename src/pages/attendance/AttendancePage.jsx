import { useState } from 'react'
import { Clock, CheckCircle2, AlertCircle, Users } from 'lucide-react'
import Card from '../../components/ui/Card'
import EmptyState from '../../components/ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'
import { MOCK_APPLICANTS } from '../../data/mockApplicants'

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

export default function AttendancePage() {
  const { shifts, jobs } = useAppData()
  const { user } = useAuth()
  const [tab, setTab] = useState('all')

  const isAdmin = user?.role === 'ADMIN'
  const myJobIds = isAdmin ? null : new Set(jobs.filter(j => j.createdBy === user?.name).map(j => j.id))
  const myShifts = isAdmin ? shifts : shifts.filter(s => myJobIds.has(s.jobId))

  // 완료된 Shift → attendance 배열 파생
  const records = myShifts
    .filter(s => s.status === 'completed' && s.attendance?.length)
    .flatMap(s => {
      const d = new Date(s.date + 'T00:00:00')
      const shiftLabel = `${s.jobTitle} · ${d.getMonth() + 1}월 ${d.getDate()}일`
      return s.attendance.map(a => ({
        id: `${s.id}-${a.id}`,
        staff: applicantMap[a.id]?.name ?? a.id,
        role: a.role,
        shift: shiftLabel,
        checkIn: a.checkIn,
        checkOut: a.checkOut,
        workHours: calcWorkHours(a.checkIn, a.checkOut),
        status: a.attendanceStatus,
      }))
    })

  const filtered = records.filter(a => tab === 'all' || a.status === tab)

  const counts = {
    in_progress: records.filter(a => a.status === 'in_progress').length,
    completed:   records.filter(a => a.status === 'completed').length,
    absent:      records.filter(a => a.status === 'absent').length,
    scheduled:   records.filter(a => a.status === 'scheduled').length,
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-bold text-navy">근태 관리</h1>
        <p className="text-sm text-gray-500 mt-0.5">스태프 출퇴근 기록 및 현황</p>
      </div>

      {/* 요약 카드 */}
      <div className="grid grid-cols-4 gap-3">
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

      {/* 탭 */}
      <div className="flex gap-1 border-b border-offwhite-200">
        {TABS.map(t => {
          const cnt = t.key === 'all' ? records.length : records.filter(a => a.status === t.key).length
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
            title="근태 기록이 없습니다"
            description="Shift를 완료·확정하면 근태 기록이 표시됩니다"
          />
        </Card>
      ) : (
        <Card padding={false}>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-offwhite-200 bg-offwhite-100">
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
