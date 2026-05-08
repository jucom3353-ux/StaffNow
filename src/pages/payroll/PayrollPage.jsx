import { useState, useMemo, useCallback } from 'react'
import { DollarSign, CheckCircle2, AlertCircle, Check, Clock, ShieldAlert } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import EmptyState from '../../components/ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'
import { MOCK_APPLICANTS } from '../../data/mockApplicants'
import { calcBillableHours, hoursLabel } from '../../utils/payrollUtils'

const HOURLY_RATE = 13000

const applicantMap = Object.fromEntries(MOCK_APPLICANTS.map(a => [a.id, a]))

const STATUS_META = {
  unpaid:          { label: '미정산',    color: 'text-amber-600 bg-amber-50 border-amber-200' },
  paid:            { label: '정산 완료', color: 'text-green-600 bg-green-50 border-green-200' },
  pending_confirm: { label: '확인 중',   color: 'text-gray-500 bg-gray-50 border-gray-200' },
}

// overtimeStatus 뱃지
const OT_META = {
  approved: { label: '연장 승인',  color: 'text-blue-600 bg-blue-50 border-blue-200' },
  capped:   { label: `초과 미인정`, color: 'text-red-500 bg-red-50 border-red-200' },
  grace:    { label: '유예 처리',  color: 'text-gray-400 bg-gray-50 border-gray-200' },
}

const TABS = [
  { key: 'all',             label: '전체' },
  { key: 'unpaid',          label: '미정산' },
  { key: 'paid',            label: '완료' },
  { key: 'pending_confirm', label: '확인 중' },
  { key: 'capped',          label: '초과 미인정' },
]

function fmt(n) { return n.toLocaleString('ko-KR') }

function StatusPill({ status }) {
  const meta = STATUS_META[status] || { label: status, color: 'text-gray-500 bg-gray-50 border-gray-200' }
  return (
    <span className={`inline-flex text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
      {meta.label}
    </span>
  )
}

function OvertimePill({ overtimeStatus, cappedMins }) {
  if (!overtimeStatus || overtimeStatus === 'normal' || overtimeStatus === 'absent') return null
  const meta = OT_META[overtimeStatus]
  if (!meta) return null
  return (
    <span className={`inline-flex items-center gap-1 text-[11px] font-semibold px-2 py-0.5 rounded-full border ${meta.color}`}>
      {overtimeStatus === 'capped' && <ShieldAlert size={10} />}
      {meta.label}
      {cappedMins > 0 && overtimeStatus !== 'approved' && (
        <span className="opacity-70">({cappedMins}분)</span>
      )}
    </span>
  )
}

export default function PayrollPage() {
  const { shifts, jobs, markAsPaid } = useAppData()
  const { user } = useAuth()

  const [paidIds, setPaidIds] = useState(new Set())
  const [approvedOtIds, setApprovedOtIds] = useState(new Set())
  const [tab, setTab] = useState('all')

  const isAdmin = user?.role === 'ADMIN'
  const myJobIds = isAdmin ? null : new Set(jobs.filter(j => j.createdBy === user?.name).map(j => j.id))
  const myShifts = isAdmin ? shifts : shifts.filter(s => myJobIds.has(s.jobId))

  const payroll = useMemo(() => {
    const rows = []
    myShifts
      .filter(s => s.status === 'completed')
      .forEach(s => {
        const d = new Date(s.date + 'T00:00:00')
        const shiftLabel = `${s.jobTitle} · ${d.getMonth() + 1}월 ${d.getDate()}일`
        const scheduledEnd = s.endTime ?? '18:00'

        const attendees = s.attendance?.length
          ? s.attendance
          : (s.applicantStates?.filter(a => a.status === 'hired') ?? []).map(a => ({
              id: a.id, role: '스태프',
              checkIn: s.startTime, checkOut: s.endTime,
              attendanceStatus: 'completed',
              overtimeApproved: false,
            }))

        attendees.forEach(a => {
          const rowId = `${s.id}-${a.id}`
          const isOtApproved = a.overtimeApproved || approvedOtIds.has(rowId)

          if (a.attendanceStatus === 'absent') {
            rows.push({
              id: rowId,
              staff: applicantMap[a.id]?.name ?? a.id,
              role: a.role,
              shift: shiftLabel,
              hours: 0,
              hoursLabel: '결근',
              actualCheckout: null,
              billableCheckout: null,
              cappedMins: 0,
              overtimeStatus: 'absent',
              amount: 0,
              status: 'pending_confirm',
            })
            return
          }

          const result = calcBillableHours(a.checkIn, a.checkOut, scheduledEnd, isOtApproved)

          rows.push({
            id: rowId,
            shiftId: s.id,
            staff: applicantMap[a.id]?.name ?? a.id,
            role: a.role,
            shift: shiftLabel,
            hours: result.hours,
            hoursLabel: hoursLabel(result.hours),
            actualCheckout: a.checkOut,
            billableCheckout: result.billableCheckout,
            cappedMins: result.cappedMins,
            overtimeStatus: result.overtimeStatus,
            amount: Math.round(result.hours * HOURLY_RATE),
            status: s.isPaid ? 'paid' : 'unpaid',
          })
        })
      })
    return rows
  }, [myShifts, approvedOtIds])

  const resolvedPayroll = payroll.map(p => ({
    ...p,
    status: paidIds.has(p.id) ? 'paid' : p.status,
  }))

  const unpaid   = resolvedPayroll.filter(p => p.status === 'unpaid')
  const capped   = resolvedPayroll.filter(p => p.overtimeStatus === 'capped')
  const filtered = resolvedPayroll.filter(p => {
    if (tab === 'capped') return p.overtimeStatus === 'capped'
    return tab === 'all' || p.status === tab
  })

  const approveOt = useCallback((id) => setApprovedOtIds(prev => new Set([...prev, id])), [])

  const approveOne = useCallback((id) => {
    setPaidIds(prev => {
      const next = new Set([...prev, id])
      // 해당 row의 shift에 속한 모든 row가 paid되면 shift를 markAsPaid
      const row = payroll.find(p => p.id === id)
      if (row) {
        const siblingIds = payroll.filter(p => p.shiftId === row.shiftId).map(p => p.id)
        const allPaid = siblingIds.every(sid => next.has(sid) || sid === id)
        if (allPaid) markAsPaid(row.shiftId)
      }
      return next
    })
  }, [payroll, markAsPaid])

  const approveAll = useCallback(() => {
    const newIds = unpaid.map(p => p.id)
    setPaidIds(prev => new Set([...prev, ...newIds]))
    // 미정산 row가 있는 모든 shift를 markAsPaid
    const shiftIds = [...new Set(unpaid.map(p => p.shiftId))]
    shiftIds.forEach(sid => markAsPaid(sid))
  }, [unpaid, markAsPaid])

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
            {capped.length > 0 && (
              <span className="ml-2 text-red-500 font-medium">
                · 초과 미인정 {capped.length}건
              </span>
            )}
          </p>
        </div>
        {unpaid.length > 0 && (
          <Button icon={CheckCircle2} onClick={approveAll}>일괄 정산 승인</Button>
        )}
      </div>

      {/* 요약 카드 */}
      <div className="grid grid-cols-3 gap-3">
        {[
          { label: '미정산',    value: `₩${fmt(totalUnpaid)}`,         sub: `${unpaid.length}건`,                                                            icon: AlertCircle,  color: 'text-amber-500',  bg: 'bg-amber-50' },
          { label: '정산 완료', value: `₩${fmt(totalPaid)}`,           sub: `${resolvedPayroll.filter(p => p.status === 'paid').length}건`,                  icon: CheckCircle2, color: 'text-green-600', bg: 'bg-green-50' },
          { label: '이번 달 총', value: `₩${fmt(totalUnpaid + totalPaid)}`, sub: `${resolvedPayroll.filter(p => p.status !== 'pending_confirm').length}건`, icon: DollarSign,   color: 'text-navy',       bg: 'bg-navy/10' },
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

      {/* 초과 미인정 안내 배너 */}
      {capped.length > 0 && tab !== 'capped' && (
        <button
          onClick={() => setTab('capped')}
          className="w-full flex items-center gap-3 px-4 py-3 bg-red-50 border border-red-200 rounded-xl text-left hover:bg-red-100 transition-colors"
        >
          <ShieldAlert size={16} className="text-red-500 shrink-0" />
          <div className="flex-1">
            <span className="text-sm font-semibold text-red-600">
              퇴근 시간 초과 미인정 {capped.length}건
            </span>
            <span className="text-xs text-red-400 ml-2">— 연장 승인 처리가 필요합니다</span>
          </div>
          <span className="text-xs text-red-400">확인하기 →</span>
        </button>
      )}

      {/* 탭 */}
      <div className="flex gap-1 border-b border-offwhite-200">
        {TABS.map(t => {
          const cnt = t.key === 'all'
            ? resolvedPayroll.length
            : t.key === 'capped'
              ? capped.length
              : resolvedPayroll.filter(p => p.status === t.key).length
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
                <span className={`text-xs tabular-nums px-1.5 rounded-md ${
                  tab === t.key
                    ? t.key === 'capped' ? 'bg-red-100 text-red-500' : 'bg-orange/10 text-orange'
                    : t.key === 'capped' ? 'bg-red-50 text-red-400' : 'bg-offwhite-200 text-gray-500'
                }`}>
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
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider hidden lg:table-cell">실제 퇴근</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">금액</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">상태</th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody>
              {filtered.map(p => (
                <tr
                  key={p.id}
                  className={`border-b border-offwhite-100 last:border-0 transition-colors ${
                    p.overtimeStatus === 'capped' ? 'bg-red-50/40 hover:bg-red-50' : 'hover:bg-offwhite-100'
                  }`}
                >
                  <td className="px-5 py-3.5">
                    <p className="font-semibold text-navy">{p.staff}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{p.role}</p>
                  </td>
                  <td className="px-5 py-3.5 text-gray-600 hidden md:table-cell">{p.shift}</td>
                  <td className="px-5 py-3.5">
                    <p className="text-gray-700 tabular-nums font-medium">{p.hoursLabel}</p>
                    <OvertimePill overtimeStatus={p.overtimeStatus} cappedMins={p.cappedMins} />
                  </td>
                  <td className="px-5 py-3.5 hidden lg:table-cell">
                    {p.actualCheckout ? (
                      <div className="text-xs tabular-nums">
                        <span className={p.overtimeStatus === 'capped' ? 'text-red-500 font-semibold' : 'text-gray-500'}>
                          {p.actualCheckout}
                        </span>
                        {p.billableCheckout && p.billableCheckout !== p.actualCheckout && (
                          <span className="text-gray-300 mx-1">→</span>
                        )}
                        {p.billableCheckout && p.billableCheckout !== p.actualCheckout && (
                          <span className="text-navy font-semibold">{p.billableCheckout}</span>
                        )}
                      </div>
                    ) : '—'}
                  </td>
                  <td className="px-5 py-3.5 font-semibold text-navy tabular-nums">
                    {p.amount > 0 ? `₩${fmt(p.amount)}` : '—'}
                  </td>
                  <td className="px-5 py-3.5"><StatusPill status={p.status} /></td>
                  <td className="px-5 py-3.5">
                    <div className="flex flex-col gap-1.5 items-start">
                      {/* 연장 승인 버튼 (capped 상태일 때만) */}
                      {p.overtimeStatus === 'capped' && p.status !== 'paid' && (
                        <button
                          onClick={() => approveOt(p.id)}
                          className="flex items-center gap-1 text-xs font-semibold text-blue-600 bg-blue-50 hover:bg-blue-100 border border-blue-200 px-2.5 py-1.5 rounded-lg transition-colors whitespace-nowrap"
                        >
                          <Clock size={11} />연장 승인
                        </button>
                      )}
                      {/* 정산 승인 버튼 */}
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
                    </div>
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
