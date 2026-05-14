import { useMemo } from 'react'
import { Banknote, Clock, CheckCircle2, TrendingUp, Calendar } from 'lucide-react'
import { useAttendance, getAssignedShifts } from '../../hooks/useAttendance'
import { calcBillableHours, hoursLabel } from '../../utils/payrollUtils'

function parseWageNum(wageStr) {
  if (!wageStr) return 0
  return parseInt(wageStr.replace(/[^0-9]/g, ''), 10) || 0
}

function calcScheduledHours(start, end) {
  if (!start || !end) return 0
  const [sh, sm] = start.split(':').map(Number)
  const [eh, em] = end.split(':').map(Number)
  let mins = (eh * 60 + em) - (sh * 60 + sm)
  if (mins < 0) mins += 24 * 60
  return mins / 60
}

function todayStr() {
  return new Date().toISOString().slice(0, 10)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr + 'T00:00:00')
  return `${d.getMonth() + 1}/${d.getDate()}(${['일','월','화','수','목','금','토'][d.getDay()]})`
}

const STATUS_CONFIG = {
  upcoming:    { label: '예정',     color: 'bg-blue-50 text-blue-600' },
  scheduled:   { label: '예정',     color: 'bg-blue-50 text-blue-600' },
  in_progress: { label: '근무중',   color: 'bg-orange-50 text-orange-600' },
  completed:   { label: '완료',     color: 'bg-green-50 text-green-600' },
  submitted:   { label: '정산대기', color: 'bg-purple-50 text-purple-600' },
  absent:      { label: '결근',     color: 'bg-red-50 text-red-500' },
}

function StatusBadge({ status }) {
  const cfg = STATUS_CONFIG[status] ?? { label: status, color: 'bg-gray-100 text-gray-500' }
  return (
    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${cfg.color}`}>
      {cfg.label}
    </span>
  )
}

function SummaryCard({ icon: Icon, label, value, sub, accent }) {
  return (
    <div className="bg-white rounded-2xl p-4 shadow-sm border border-offwhite-200 flex items-start gap-3">
      <div className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${accent}`}>
        <Icon size={18} />
      </div>
      <div className="min-w-0">
        <p className="text-xs text-gray-400 mb-0.5">{label}</p>
        <p className="text-lg font-bold text-navy leading-tight">{value}</p>
        {sub && <p className="text-xs text-gray-400 mt-0.5">{sub}</p>}
      </div>
    </div>
  )
}

export default function IndividualPayrollPage() {
  const { getRecord } = useAttendance()

  const shifts = useMemo(() => getAssignedShifts(), [])

  const today = todayStr()

  const rows = useMemo(() => shifts.map(s => {
    const rec = getRecord(s.shiftId)
    const wage = parseWageNum(s.wage)
    const scheduledHours = calcScheduledHours(s.scheduledStart, s.scheduledEnd)

    let status = 'scheduled'
    let earnedWage = null
    let actualHours = null

    if (s.shiftDate > today) {
      status = 'upcoming'
    } else if (rec) {
      if (rec.status === 'submitted') status = 'submitted'
      else if (rec.status === 'completed') status = 'completed'
      else if (rec.status === 'in_progress') status = 'in_progress'
      else status = 'scheduled'
    } else if (s.shiftDate < today) {
      status = 'absent'
    }

    if (rec?.checkIn && rec?.checkOut) {
      const billed = calcBillableHours(rec.checkIn, rec.checkOut, s.scheduledEnd)
      actualHours = billed.hours
      const ratio = scheduledHours > 0 ? Math.min(billed.hours / scheduledHours, 1) : 0
      earnedWage = Math.round(wage * ratio)
    } else if (status === 'upcoming' || status === 'scheduled') {
      earnedWage = wage
    }

    return { ...s, status, earnedWage, actualHours, scheduledHours, wageNum: wage }
  }), [shifts, getRecord, today])

  const completedRows  = rows.filter(r => r.status === 'completed' || r.status === 'submitted')
  const settledRows    = rows.filter(r => r.status === 'submitted')
  const pendingRows    = rows.filter(r => r.status === 'completed')

  const totalThisMonth = completedRows.reduce((s, r) => s + (r.earnedWage ?? 0), 0)
  const totalSettled   = settledRows.reduce((s, r) => s + (r.earnedWage ?? 0), 0)
  const totalPending   = pendingRows.reduce((s, r) => s + (r.earnedWage ?? 0), 0)

  function fmt(n) {
    return n.toLocaleString('ko-KR') + '원'
  }

  return (
    <div className="min-h-screen bg-offwhite pb-24">
      <div className="max-w-2xl mx-auto px-4 pt-6 space-y-5">

        {/* 헤더 */}
        <div>
          <h1 className="text-xl font-bold text-navy">급여 명세</h1>
          <p className="text-sm text-gray-400 mt-0.5">근무별 정산 내역을 확인하세요</p>
        </div>

        {/* 요약 카드 3종 */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <SummaryCard
            icon={TrendingUp}
            label="이번달 수입 (완료 기준)"
            value={fmt(totalThisMonth)}
            accent="bg-green-50 text-green-600"
          />
          <SummaryCard
            icon={CheckCircle2}
            label="누적 정산완료"
            value={fmt(totalSettled)}
            sub={`${settledRows.length}건`}
            accent="bg-blue-50 text-blue-600"
          />
          <SummaryCard
            icon={Clock}
            label="정산 대기"
            value={fmt(totalPending)}
            sub={`${pendingRows.length}건 미정산`}
            accent="bg-orange-50 text-orange-600"
          />
        </div>

        {/* 근무 목록 */}
        <div className="space-y-3">
          <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide">전체 근무 내역</h2>

          {rows.length === 0 && (
            <div className="bg-white rounded-2xl border border-offwhite-200 p-10 text-center">
              <Banknote size={36} className="mx-auto text-gray-200 mb-3" />
              <p className="text-sm text-gray-400">배정된 근무가 없습니다.</p>
            </div>
          )}

          {rows.map(r => (
            <div key={r.shiftId} className="bg-white rounded-2xl border border-offwhite-200 shadow-sm p-4">
              <div className="flex items-start justify-between gap-2 mb-2">
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-navy truncate">{r.jobTitle}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{r.company}</p>
                </div>
                <StatusBadge status={r.status} />
              </div>

              <div className="flex items-center gap-4 text-xs text-gray-500 mb-3">
                <span className="flex items-center gap-1">
                  <Calendar size={12} />
                  {formatDate(r.shiftDate)}
                </span>
                <span className="flex items-center gap-1">
                  <Clock size={12} />
                  {r.scheduledStart}–{r.scheduledEnd}
                </span>
              </div>

              <div className="flex items-center justify-between pt-3 border-t border-offwhite-200">
                <div className="text-xs text-gray-400 space-y-0.5">
                  {r.actualHours != null ? (
                    <p>실근무 <span className="text-navy font-medium">{hoursLabel(r.actualHours)}</span>
                      {' / '}예정 {hoursLabel(r.scheduledHours)}
                    </p>
                  ) : (
                    <p>예정 {hoursLabel(r.scheduledHours)}</p>
                  )}
                </div>
                <div className="text-right">
                  {r.status === 'absent' ? (
                    <p className="text-sm font-bold text-red-500">결근</p>
                  ) : r.earnedWage != null ? (
                    <p className="text-sm font-bold text-navy">{fmt(r.earnedWage)}</p>
                  ) : (
                    <p className="text-sm text-gray-300">—</p>
                  )}
                  {(r.status === 'completed' || r.status === 'submitted') && r.earnedWage != null && (
                    <p className="text-xs text-gray-400">
                      {r.status === 'submitted' ? '정산완료' : '정산대기'}
                    </p>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* 완료 근무 합계 */}
        {completedRows.length > 0 && (
          <div className="bg-navy rounded-2xl p-4 flex items-center justify-between">
            <div className="flex items-center gap-2 text-navy-200">
              <Banknote size={16} />
              <span className="text-sm font-medium">완료 근무 총합</span>
            </div>
            <span className="text-lg font-bold text-white">{fmt(totalThisMonth)}</span>
          </div>
        )}

      </div>
    </div>
  )
}
