import { useState, useEffect } from 'react'
import {
  Clock, CheckCircle2, MapPin, ChevronRight, LogIn, LogOut,
  CalendarDays, Pencil, Trash2, Send, X, AlertTriangle,
} from 'lucide-react'
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
  submitted:   { label: '제출됨',  color: 'bg-navy/10 text-navy' },
}

// ── 출퇴근 기록 수정 모달 ────────────────────────────────────
function EditModal({ record, onSave, onClose }) {
  const [checkIn,  setCheckIn]  = useState(record?.checkIn  ?? '')
  const [checkOut, setCheckOut] = useState(record?.checkOut ?? '')

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4" onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 space-y-4" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-navy">출퇴근 시간 수정</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600"><X size={16} /></button>
        </div>

        <div className="space-y-3">
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">출근 시간</label>
            <input
              type="time" value={checkIn} onChange={e => setCheckIn(e.target.value)}
              className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy outline-none focus:border-navy"
            />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">퇴근 시간</label>
            <input
              type="time" value={checkOut} onChange={e => setCheckOut(e.target.value)}
              className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy outline-none focus:border-navy"
            />
          </div>
        </div>

        <div className="flex gap-2 pt-1">
          <button onClick={onClose}
            className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:bg-offwhite transition-colors">
            취소
          </button>
          <button
            onClick={() => { onSave({ checkIn, checkOut }); onClose() }}
            disabled={!checkIn}
            className="flex-1 py-2.5 rounded-xl bg-navy text-white text-sm font-bold hover:bg-navy/80 transition-colors disabled:opacity-40"
          >
            저장
          </button>
        </div>
      </div>
    </div>
  )
}

// ── 제출 확인 모달 ───────────────────────────────────────────
function SubmitModal({ shift, record, onConfirm, onClose }) {
  const worked = calcDuration(record?.checkIn, record?.checkOut)
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4" onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 space-y-4" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-navy">출퇴근 기록 제출</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600"><X size={16} /></button>
        </div>

        <div className="bg-offwhite rounded-xl p-3 space-y-2">
          <p className="text-sm font-bold text-navy">{shift.jobTitle}</p>
          <p className="text-xs text-gray-500">{shift.company}</p>
          <div className="flex gap-4 pt-1 text-sm">
            <div>
              <p className="text-[10px] text-gray-400">출근</p>
              <p className="font-bold text-navy tabular-nums">{record?.checkIn ?? '—'}</p>
            </div>
            <div>
              <p className="text-[10px] text-gray-400">퇴근</p>
              <p className="font-bold text-navy tabular-nums">{record?.checkOut ?? '—'}</p>
            </div>
            {worked && (
              <div>
                <p className="text-[10px] text-gray-400">근무 시간</p>
                <p className="font-bold text-green-600">{worked}</p>
              </div>
            )}
          </div>
        </div>

        <div className="flex gap-2 items-start bg-amber-50 border border-amber-100 rounded-xl p-3">
          <AlertTriangle size={14} className="text-amber-500 shrink-0 mt-0.5" />
          <p className="text-xs text-amber-700 leading-relaxed">
            제출 후에는 <span className="font-bold">수정 및 삭제가 불가능</span>합니다.
            기업 및 운영팀에 기록이 전달됩니다.
          </p>
        </div>

        <div className="flex gap-2">
          <button onClick={onClose}
            className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:bg-offwhite transition-colors">
            취소
          </button>
          <button onClick={onConfirm}
            className="flex-1 py-2.5 rounded-xl bg-navy text-white text-sm font-bold hover:bg-navy/80 transition-colors flex items-center justify-center gap-1.5">
            <Send size={13} />제출하기
          </button>
        </div>
      </div>
    </div>
  )
}

// ── 메인 페이지 ─────────────────────────────────────────────
export default function IndividualAttendancePage() {
  const { user } = useAuth()
  const { checkIn, checkOut, editRecord, deleteRecord, submitRecord, getRecord } = useAttendance()
  const shifts = getAssignedShifts(user?.name)
  const today = todayStr()

  const [confirmAction, setConfirmAction] = useState(null)
  const [editTarget,    setEditTarget]    = useState(null) // { shiftId, record }
  const [submitTarget,  setSubmitTarget]  = useState(null) // { shift, record }
  const [deleteTarget,  setDeleteTarget]  = useState(null) // shiftId
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

  const todayShifts    = shifts.filter(s => s.shiftDate === today)
  const upcomingShifts = shifts.filter(s => s.shiftDate > today).sort((a, b) => a.shiftDate.localeCompare(b.shiftDate))
  const pastShifts     = shifts.filter(s => s.shiftDate < today).sort((a, b) => b.shiftDate.localeCompare(a.shiftDate))

  return (
    <div className="space-y-6">

      {/* ── 모달들 ── */}
      {editTarget && (
        <EditModal
          record={editTarget.record}
          onSave={(times) => editRecord(editTarget.shiftId, times)}
          onClose={() => setEditTarget(null)}
        />
      )}
      {submitTarget && (
        <SubmitModal
          shift={submitTarget.shift}
          record={submitTarget.record}
          onConfirm={() => { submitRecord(submitTarget.shift.shiftId); setSubmitTarget(null) }}
          onClose={() => setSubmitTarget(null)}
        />
      )}
      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4"
          onClick={() => setDeleteTarget(null)}>
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 space-y-4" onClick={e => e.stopPropagation()}>
            <div className="text-center space-y-2">
              <div className="w-12 h-12 rounded-full bg-red-50 flex items-center justify-center mx-auto">
                <Trash2 size={20} className="text-red-500" />
              </div>
              <p className="font-bold text-navy">기록을 삭제할까요?</p>
              <p className="text-sm text-gray-500">출퇴근 기록이 초기화되며<br />다시 출근 기록이 가능합니다.</p>
            </div>
            <div className="flex gap-2">
              <button onClick={() => setDeleteTarget(null)}
                className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:bg-offwhite transition-colors">
                취소
              </button>
              <button onClick={() => { deleteRecord(deleteTarget); setDeleteTarget(null) }}
                className="flex-1 py-2.5 rounded-xl bg-red-500 text-white text-sm font-bold hover:bg-red-600 transition-colors">
                삭제
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── 헤더 + 현재 시각 ── */}
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

      {/* ── 오늘의 근무 ── */}
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
            const canEdit     = status === 'completed'
            const isSubmitted = status === 'submitted'
            const worked = calcDuration(rec?.checkIn, rec?.checkOut)

            return (
              <div key={shift.shiftId} className="bg-white rounded-2xl border border-offwhite-200 p-5 space-y-4">
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

                <div className="flex flex-wrap gap-4 text-sm text-gray-500">
                  <span className="flex items-center gap-1.5"><Clock size={13} />{shift.scheduledStart} – {shift.scheduledEnd}</span>
                  <span className="flex items-center gap-1.5"><MapPin size={13} />{shift.location}</span>
                </div>

                {/* 출퇴근 기록 */}
                {(rec?.checkIn || rec?.checkOut) && (
                  <div className="pt-3 border-t border-offwhite-200 space-y-3">
                    <div className="flex gap-4 text-sm">
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

                    {/* 수정/삭제 버튼 (completed 상태) */}
                    {canEdit && (
                      <div className="flex gap-2">
                        <button
                          onClick={() => setEditTarget({ shiftId: shift.shiftId, record: rec })}
                          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-offwhite-200 text-xs font-semibold text-gray-500 hover:bg-offwhite hover:text-navy transition-colors"
                        >
                          <Pencil size={12} />수정
                        </button>
                        <button
                          onClick={() => setDeleteTarget(shift.shiftId)}
                          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-offwhite-200 text-xs font-semibold text-gray-500 hover:bg-red-50 hover:text-red-500 hover:border-red-200 transition-colors"
                        >
                          <Trash2 size={12} />삭제
                        </button>
                      </div>
                    )}
                  </div>
                )}

                {/* 출근하기 / 퇴근하기 버튼 */}
                {(canCheckIn || canCheckOut) && (
                  <button
                    onClick={() => setConfirmAction({ shiftId: shift.shiftId, type: canCheckIn ? 'in' : 'out' })}
                    className={`w-full py-3 rounded-xl font-bold text-sm flex items-center justify-center gap-2 transition-colors ${
                      canCheckIn ? 'bg-orange text-white hover:bg-orange-600' : 'bg-green-500 text-white hover:bg-green-600'
                    }`}
                  >
                    {canCheckIn ? <><LogIn size={16} />출근하기</> : <><LogOut size={16} />퇴근하기</>}
                  </button>
                )}

                {/* 제출하기 버튼 (completed 상태, 퇴근까지 완료된 경우) */}
                {canEdit && rec?.checkOut && (
                  <button
                    onClick={() => setSubmitTarget({ shift, record: rec })}
                    className="w-full py-3 rounded-xl bg-navy text-white font-bold text-sm flex items-center justify-center gap-2 hover:bg-navy/80 transition-colors"
                  >
                    <Send size={15} />출퇴근 기록 제출하기
                  </button>
                )}

                {/* 제출 완료 상태 */}
                {isSubmitted && (
                  <div className="flex items-center justify-center gap-2 py-2.5 bg-navy/5 rounded-xl text-navy text-sm font-semibold">
                    <CheckCircle2 size={16} className="text-navy" />
                    제출 완료 · 기업에 전달되었습니다
                  </div>
                )}
              </div>
            )
          })
        )}
      </section>

      {/* ── 예정된 근무 ── */}
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

      {/* ── 지난 근무 ── */}
      {pastShifts.length > 0 && (
        <section className="space-y-3">
          <h2 className="text-sm font-bold text-navy flex items-center gap-2">
            <CheckCircle2 size={15} className="text-orange" />
            지난 근무
          </h2>
          <div className="space-y-2">
            {pastShifts.map(shift => {
              const rec = getRecord(shift.shiftId)
              const isSubmitted = rec?.status === 'submitted'
              const isCompleted = rec?.status === 'completed' || isSubmitted
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
                      <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${
                        isSubmitted ? 'bg-navy/10 text-navy' :
                        isCompleted ? 'bg-green-100 text-green-600' :
                        'bg-gray-100 text-gray-400'
                      }`}>
                        {isSubmitted ? '제출됨' : isCompleted ? '완료' : '미기록'}
                      </span>
                    </div>
                  </div>

                  {rec && (
                    <div className="mt-3 pt-3 border-t border-offwhite-100 space-y-2">
                      <div className="flex gap-6 text-sm">
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

                      {/* 지난 근무 수정/삭제/제출 (completed 상태) */}
                      {rec.status === 'completed' && (
                        <div className="flex gap-2 flex-wrap">
                          <button
                            onClick={() => setEditTarget({ shiftId: shift.shiftId, record: rec })}
                            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-offwhite-200 text-xs font-semibold text-gray-500 hover:bg-offwhite hover:text-navy transition-colors"
                          >
                            <Pencil size={12} />수정
                          </button>
                          <button
                            onClick={() => setDeleteTarget(shift.shiftId)}
                            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-offwhite-200 text-xs font-semibold text-gray-500 hover:bg-red-50 hover:text-red-500 hover:border-red-200 transition-colors"
                          >
                            <Trash2 size={12} />삭제
                          </button>
                          {rec.checkOut && (
                            <button
                              onClick={() => setSubmitTarget({ shift, record: rec })}
                              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-navy text-white text-xs font-bold hover:bg-navy/80 transition-colors"
                            >
                              <Send size={12} />제출하기
                            </button>
                          )}
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

      {/* ── 출/퇴근 확인 모달 ── */}
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
              <p className="text-xs text-gray-400 mt-1">제출 전까지 수정·삭제가 가능합니다.</p>
            </div>
            <div className="flex gap-2">
              <button onClick={() => setConfirmAction(null)}
                className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:border-gray-400 transition-colors">
                취소
              </button>
              <button onClick={handleConfirm}
                className={`flex-1 py-2.5 rounded-xl text-sm font-bold text-white transition-colors ${confirmAction.type === 'in' ? 'bg-orange hover:bg-orange-600' : 'bg-green-500 hover:bg-green-600'}`}>
                확인
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
