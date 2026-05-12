import { useState, useEffect } from 'react'
import { Clock, CheckCircle2, MapPin, ChevronRight, LogIn, LogOut, CalendarDays } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { useAttendance, getAssignedShifts } from '../../hooks/useAttendance'

function todayStr() {
  return new Date().toISOString().slice(0, 10)
}

function formatDate(dateStr) {
  const d = new Date(dateStr + 'T00:00:00')
  const days = ['일', '월', '화', '수', '목', '금', '토']
  return `${d.getMonth() + 1}월 ${d.getDate()}일 (${days[d.getDay()]})`
}

function calcDuration(start, end) {
  if (!start || !end) return null
  const [h1, m1] = start.split(':').map(Number)
  const [h2, m2] = end.split(':').map(Number)
  let total = (h2 * 60 + m2) - (h1 * 60 + m1)
  if (total < 0) total += 24 * 60
  const h = Math.floor(total / 60)
  const m = total % 60
  return m > 0 ? `${h}시간 ${m}분` : `${h}시간`
}

const STATUS = {
  scheduled:   { label: '예정',    color: 'bg-gray-100 text-gray-500' },
  in_progress: { label: '근무 중', color: 'bg-blue-100 text-blue-600' },
  completed:   { label: '완료',    color: 'bg-green-100 text-green-600' },
}

export default function IndividualAttendancePage() {
  const { user } = useAuth()
  const { checkIn, checkOut, getRecord } = useAttendance()
  const shifts = getAssignedShifts(user?.name)
  const today = todayStr()

  const [confirmAction, setConfirmAction] = useState(null)
  const [now, setNow] = useState(new Date())

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(timer)
  }, [])

  function handleConfirm() {
    if (!confirmAction) return
    confirmAction.type === 'in' ? checkIn(confirmAction.shiftId) : checkOut(confirmAction.shiftId)
    setConfirmAction(null)
  }

  const todayShifts  = shifts.filter(s => s.shiftDate === today)
  const upcomingShifts = shifts.filter(s => s.shiftDate > today).sort((a, b) => a.shiftDate.localeCompare(b.shiftDate))
  const pastShifts   = shifts.filter(s => s.shiftDate < today).sort((a, b) => b.shiftDate.localeCompare(a.shiftDate))

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-navy">출퇴근 관리</h1>
          <p className="text-sm text-gray-500 mt-1">배정된 근무를 확인하고 출퇴근을 기록하세요.</p>
        </div>
        <div className="flex flex-col items-end shrink-0 bg-white border border-offwhite-200 rounded-2xl px-4 py-2.5">
          <span className="text-[11px] text-gray-400 font-medium">현재 시각</span>
          <span className="text-xl font-bold text-navy tabular-nums tracking-tight">
            {now.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })}
          </span>
          <span className="text-[11px] text-gray-400">
            {now.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric', weekday: 'short' })}
          </span>
        </div>
      </div>

      {/* 오늘의 근무 */}
      <section className="space-y-3">
        <h2 className="text-sm font-bold text-navy flex items-center gap-2">
          <CalendarDays size={15} className="text-orange" />
          오늘의 근무
        </h2>

        {todayShifts.length === 0 ? (
          <div className="bg-white rounded-2xl border border-offwhite-200 px-5 py-8 text-center">
            <Clock size={28} className="mx-auto text-gray-300 mb-2" />
            <p className="text-sm font-semibold text-gray-400">오늘 예정된 근무가 없습니다</p>
          </div>
        ) : (
          todayShifts.map(shift => {
            const rec = getRecord(shift.shiftId)
            const status = rec?.status ?? 'scheduled'
            const meta = STATUS[status] ?? STATUS.scheduled
            const canCheckIn  = status === 'scheduled'
            const canCheckOut = status === 'in_progress'
            const worked = calcDuration(rec?.checkIn, rec?.checkOut)

            return (
              <div key={shift.shiftId} className="bg-white rounded-2xl border border-offwhite-200 p-5 space-y-4">
                {/* 헤더 */}
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${meta.color}`}>{meta.label}</span>
                    </div>
                    <p className="font-bold text-navy text-lg">{shift.jobTitle}</p>
                    <p className="text-sm text-gray-500">{shift.company}</p>
                  </div>
                  <p className="text-base font-bold text-orange shrink-0">{shift.wage}</p>
                </div>

                {/* 시간·장소 */}
                <div className="flex flex-wrap gap-4 text-sm text-gray-500">
                  <span className="flex items-center gap-1.5">
                    <Clock size={13} />
                    {shift.scheduledStart} – {shift.scheduledEnd}
                  </span>
                  <span className="flex items-center gap-1.5">
                    <MapPin size={13} />
                    {shift.location}
                  </span>
                </div>

                {/* 실제 출퇴근 기록 */}
                {(rec?.checkIn || rec?.checkOut) && (
                  <div className="flex gap-4 pt-3 border-t border-offwhite-200 text-sm">
                    <div>
                      <p className="text-xs text-gray-400 mb-0.5">출근</p>
                      <p className="font-bold text-navy tabular-nums">{rec?.checkIn ?? '—'}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-400 mb-0.5">퇴근</p>
                      <p className="font-bold text-navy tabular-nums">{rec?.checkOut ?? '—'}</p>
                    </div>
                    {worked && (
                      <div>
                        <p className="text-xs text-gray-400 mb-0.5">근무 시간</p>
                        <p className="font-bold text-green-600">{worked}</p>
                      </div>
                    )}
                  </div>
                )}

                {/* 출퇴근 버튼 */}
                {(canCheckIn || canCheckOut) && (
                  <button
                    onClick={() => setConfirmAction({ shiftId: shift.shiftId, type: canCheckIn ? 'in' : 'out' })}
                    className={`w-full py-3 rounded-xl font-bold text-sm flex items-center justify-center gap-2 transition-colors ${
                      canCheckIn
                        ? 'bg-orange text-white hover:bg-orange-600'
                        : 'bg-green-500 text-white hover:bg-green-600'
                    }`}
                  >
                    {canCheckIn ? <><LogIn size={16} />출근하기</> : <><LogOut size={16} />퇴근하기</>}
                  </button>
                )}

                {status === 'completed' && (
                  <div className="flex items-center justify-center gap-2 py-2 text-green-600 text-sm font-semibold">
                    <CheckCircle2 size={16} />
                    근무 완료
                  </div>
                )}
              </div>
            )
          })
        )}
      </section>

      {/* 예정된 근무 */}
      {upcomingShifts.length > 0 && (
        <section className="space-y-3">
          <h2 className="text-sm font-bold text-navy flex items-center gap-2">
            <ChevronRight size={15} className="text-orange" />
            예정된 근무
          </h2>
          <div className="space-y-2">
            {upcomingShifts.map(shift => (
              <div key={shift.shiftId} className="bg-white rounded-2xl border border-offwhite-200 px-5 py-4 flex items-center justify-between gap-3">
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-navy truncate">{shift.jobTitle}</p>
                  <p className="text-xs text-gray-500 mt-0.5">{shift.company}</p>
                  <div className="flex items-center gap-3 mt-1.5 text-xs text-gray-400">
                    <span>{formatDate(shift.shiftDate)}</span>
                    <span>{shift.scheduledStart} – {shift.scheduledEnd}</span>
                  </div>
                </div>
                <div className="text-right shrink-0">
                  <p className="text-sm font-bold text-orange">{shift.wage}</p>
                  <span className="text-xs text-gray-400 bg-gray-50 px-2 py-0.5 rounded-full">예정</span>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* 지난 근무 */}
      {pastShifts.length > 0 && (
        <section className="space-y-3">
          <h2 className="text-sm font-bold text-navy flex items-center gap-2">
            <CheckCircle2 size={15} className="text-orange" />
            지난 근무
          </h2>
          <div className="space-y-2">
            {pastShifts.map(shift => {
              const rec = getRecord(shift.shiftId)
              const isCompleted = rec?.status === 'completed'
              const worked = calcDuration(rec?.checkIn, rec?.checkOut)
              return (
                <div key={shift.shiftId} className="bg-white rounded-2xl border border-offwhite-200 px-5 py-4">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-navy truncate">{shift.jobTitle}</p>
                      <p className="text-xs text-gray-500 mt-0.5">{shift.company}</p>
                      <p className="text-xs text-gray-400 mt-1">{formatDate(shift.shiftDate)} · {shift.scheduledStart} – {shift.scheduledEnd}</p>
                    </div>
                    <div className="text-right shrink-0">
                      <p className="text-sm font-bold text-orange">{shift.wage}</p>
                      <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${isCompleted ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-400'}`}>
                        {isCompleted ? '완료' : '미기록'}
                      </span>
                    </div>
                  </div>

                  {rec && (
                    <div className="flex gap-6 mt-3 pt-3 border-t border-offwhite-100 text-sm">
                      <div>
                        <p className="text-xs text-gray-400 mb-0.5">출근</p>
                        <p className="font-semibold text-navy tabular-nums">{rec.checkIn ?? '—'}</p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-400 mb-0.5">퇴근</p>
                        <p className="font-semibold text-navy tabular-nums">{rec.checkOut ?? '—'}</p>
                      </div>
                      {worked && (
                        <div>
                          <p className="text-xs text-gray-400 mb-0.5">근무 시간</p>
                          <p className="font-semibold text-green-600">{worked}</p>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        </section>
      )}

      {/* 확인 모달 */}
      {confirmAction && (
        <div className="fixed inset-0 bg-black/40 flex items-end sm:items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl p-6 w-full max-w-sm space-y-4 shadow-xl">
            <div className="text-center">
              <div className={`w-14 h-14 rounded-full flex items-center justify-center mx-auto mb-3 ${confirmAction.type === 'in' ? 'bg-orange/10' : 'bg-green-50'}`}>
                {confirmAction.type === 'in'
                  ? <LogIn size={24} className="text-orange" />
                  : <LogOut size={24} className="text-green-500" />
                }
              </div>
              <p className="font-bold text-navy text-lg">
                {confirmAction.type === 'in' ? '출근을 기록할까요?' : '퇴근을 기록할까요?'}
              </p>
              <p className="text-sm text-gray-500 mt-1">
                현재 시각으로 {confirmAction.type === 'in' ? '출근' : '퇴근'} 시간이 저장됩니다.
              </p>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => setConfirmAction(null)}
                className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:border-gray-400 transition-colors"
              >
                취소
              </button>
              <button
                onClick={handleConfirm}
                className={`flex-1 py-2.5 rounded-xl text-sm font-bold text-white transition-colors ${confirmAction.type === 'in' ? 'bg-orange hover:bg-orange-600' : 'bg-green-500 hover:bg-green-600'}`}
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
