import { useState, useMemo, useCallback } from 'react'
import { DollarSign, CheckCircle2, Clock, AlertCircle, Check } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import EmptyState from '../../components/ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'
import { MOCK_APPLICANTS } from '../../data/mockApplicants'

const HOURLY_RATE = 13000  // 데모 기준 시급

const applicantMap = Object.fromEntries(MOCK_APPLICANTS.map(a => [a.id, a]))

const STATUS_META = {
  unpaid:          { label: '미정산',    color: 'text-amber-600 bg-amber-50 border-amber-200' },
  paid:            { label: '정산 완료', color: 'text-green-600 bg-green-50 border-green-200' },
  pending_confirm: { label: '확인 중',   color: 'text-gray-500 bg-gray-50 border-gray-200' },
}

const TABS = [
  { key: 'all',             label: '전체' },
  { key: 'unpaid',          label: '미정산' },
  { key: 'paid',            label: '완료' },
  { key: 'pending_confirm', label: '확인 중' },
]

function fmt(n) { return n.toLocaleString('ko-KR') }

function calcHours(checkIn, checkOut) {
  if (!checkIn || !checkOut) return 0
  const [h1, m1] = checkIn.split(':').map(Number)
  const [h2, m2] = checkOut.split(':').map(Number)
  let mins = (h2 * 60 + m2) - (h1 * 60 + m1)
  if (mins < 0) mins += 24 * 60  // 자정 넘는 야간 근무
  return Math.max(0, mins / 60)
}

function hoursLabel(h) {
  if (h === 0) return '0h'
  const hh = Math.floor(h)
  const mm = Math.round((h - hh) * 60)
  return mm > 0 ? `${hh}h ${mm}m` : `${hh}h`
}

function StatusPill({ status }) {
  const meta = STATUS_META[status] || { label: status, color: 'text-gray-500 bg-gray-50 border-gray-200' }
  return (
    <span className={`inline-flex text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
      {meta.label}
    </span>
  )
}

export default function PayrollPage() {
  const { shifts, jobs } = useAppData()
  const { user } = useAuth()

  // 정산 지급 상태를 로컬로 관리 (실제 서비스에서는 백엔드 필드)
  const [paidIds, setPaidIds] = useState(new Set())

  const isAdmin = user?.role === 'ADMIN'
  const myJobIds = isAdmin ? null : new Set(jobs.filter(j => j.createdBy === user?.name).map(j => j.id))
  const myShifts = isAdmin ? shifts : shifts.filter(s => myJobIds.has(s.jobId))

  // 완료된 Shift의 고용 확정 인원 → 정산 행 생성
  const payroll = useMemo(() => {
    const rows = []
    myShifts
      .filter(s => s.status === 'completed')
      .forEach(s => {
        const d = new Date(s.date + 'T00:00:00')
        const shiftLabel = `${s.jobTitle} · ${d.getMonth() + 1}월 ${d.getDate()}일`

        // attendance 있으면 우선 사용, 없으면 applicantStates hired 목록
        const attendees = s.attendance?.length
          ? s.attendance
          : (s.applicantStates?.filter(a => a.status === 'hired') ?? []).map(a => ({
              id: a.id, role: '스태프',
              checkIn: s.startTime, checkOut: s.endTime,
              attendanceStatus: 'completed',
            }))

        attendees.forEach(a => {
          if (a.attendanceStatus === 'absent') {
            rows.push({
              id: `${s.id}-${a.id}`,
              staff: applicantMap[a.id]?.name ?? a.id,
              role: a.role,
              shift: shiftLabel,
              hours: 0,
              hoursLabel: '결근',
              amount: 0,
              status: 'pending_confirm',
            })
            return
          }
          const h = calcHours(a.checkIn, a.checkOut)
          rows.push({
            id: `${s.id}-${a.id}`,
            staff: applicantMap[a.id]?.name ?? a.id,
            role: a.role,
            shift: shiftLabel,
            hours: h,
            hoursLabel: hoursLabel(h),
            amount: Math.round(h * HOURLY_RATE),
            status: 'unpaid',
          })
        })
      })
    return rows
  }, [myShifts])

  // 지급 완료 처리 (로컬)
  const [tab, setTab] = useState('all')

  const resolvedPayroll = payroll.map(p => ({
    ...p,
    status: paidIds.has(p.id) ? 'paid' : p.status,
  }))

  const unpaid   = resolvedPayroll.filter(p => p.status === 'unpaid')
  const filtered = resolvedPayroll.filter(p => tab === 'all' || p.status === tab)

  const approveOne = useCallback((id) => setPaidIds(prev => new Set([...prev, id])), [])
  const approveAll = useCallback(() => {
    setPaidIds(prev => new Set([...prev, ...unpaid.map(p => p.id)]))
  }, [unpaid])

  const totalUnpaid = unpaid.reduce((s, p) => s + p.amount, 0)
  const totalPaid   = resolvedPayroll.filter(p => p.status === 'paid').reduce((s, p) => s + p.amount, 0)

  return (
    <div className="space-y-5">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-navy">정산 관리</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            미정산 <strong className="text-amber-600">{unpaid.length}건</strong>
            {totalUnpaid > 0 && <span className="ml-1 text-amber-600">· ₩{fmt(totalUnpaid)}</span>}
          </p>
        </div>
        {unpaid.length > 0 && (
          <Button icon={CheckCircle2} onClick={approveAll}>일괄 정산 승인</Button>
        )}
      </div>

      {/* 요약 카드 */}
      <div className="grid grid-cols-3 gap-3">
        {[
          { label: '미정산',    value: `₩${fmt(totalUnpaid)}`,         sub: `${unpaid.length}건`,                                             icon: AlertCircle,  color: 'text-amber-500',  bg: 'bg-amber-50' },
          { label: '정산 완료', value: `₩${fmt(totalPaid)}`,           sub: `${resolvedPayroll.filter(p => p.status === 'paid').length}건`,    icon: CheckCircle2, color: 'text-green-600', bg: 'bg-green-50' },
          { label: '이번 달 총', value: `₩${fmt(totalUnpaid + totalPaid)}`, sub: `${resolvedPayroll.filter(p => p.status !== 'pending_confirm').length}건`, icon: DollarSign, color: 'text-navy', bg: 'bg-navy/10' },
        ].map(({ label, value, sub, icon: Icon, color, bg }) => (
          <Card key={label}>
            <div className="flex items-center gap-3">
              <div className={`w-9 h-9 rounded-lg ${bg} flex items-center justify-center shrink-0`}>
                <Icon size={16} className={color} />
              </div>
              <div>
                <p className="text-lg font-bold text-navy tabular-nums">{value}</p>
                <p className="text-xs text-gray-500">{label} · {sub}</p>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* 탭 */}
      <div className="flex gap-1 border-b border-offwhite-200">
        {TABS.map(t => {
          const cnt = t.key === 'all' ? resolvedPayroll.length : resolvedPayroll.filter(p => p.status === t.key).length
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
            icon={DollarSign}
            title="정산 내역이 없습니다"
            description="Shift를 완료·확정하면 정산 내역이 자동으로 표시됩니다"
          />
        </Card>
      ) : (
        <Card padding={false}>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-offwhite-200 bg-offwhite-100">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">스태프</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider hidden md:table-cell">Shift</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">근무 시간</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">금액</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">상태</th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody>
              {filtered.map(p => (
                <tr key={p.id} className="border-b border-offwhite-100 last:border-0 hover:bg-offwhite-100 transition-colors">
                  <td className="px-5 py-3.5">
                    <p className="font-semibold text-navy">{p.staff}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{p.role}</p>
                  </td>
                  <td className="px-5 py-3.5 text-gray-600 hidden md:table-cell">{p.shift}</td>
                  <td className="px-5 py-3.5 text-gray-700 tabular-nums">{p.hoursLabel}</td>
                  <td className="px-5 py-3.5 font-semibold text-navy tabular-nums">
                    {p.amount > 0 ? `₩${fmt(p.amount)}` : '—'}
                  </td>
                  <td className="px-5 py-3.5"><StatusPill status={p.status} /></td>
                  <td className="px-5 py-3.5">
                    {p.status === 'unpaid' && (
                      <button
                        onClick={() => approveOne(p.id)}
                        className="flex items-center gap-1 text-xs font-semibold text-green-600 bg-green-50 hover:bg-green-100 border border-green-200 px-2.5 py-1.5 rounded-lg transition-colors"
                      >
                        <Check size={12} />승인
                      </button>
                    )}
                    {p.status === 'paid' && (
                      <span className="text-xs text-green-600 flex items-center gap-1 font-medium">
                        <CheckCircle2 size={11} />완료
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  )
}
