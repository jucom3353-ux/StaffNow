import { useState, useCallback } from 'react'
import { useAuth } from '../context/AuthContext'

// 개인-기업 공유 localStorage 키 (같은 브라우저에서 양쪽이 읽음)
export const SHARED_ATTENDANCE_KEY = 'staffnow_live_attendance'
export const DISPUTES_KEY          = 'staffnow_disputes'

// 이의신청 유틸
export function loadDisputes() {
  try { const r = localStorage.getItem(DISPUTES_KEY); return r ? JSON.parse(r) : [] } catch { return [] }
}
export function saveDisputes(list) {
  try { localStorage.setItem(DISPUTES_KEY, JSON.stringify(list)) } catch { /* no-op */ }
}

// ── 지각 시간수정 요청 (인력 → 기업 공유) ──────────────────
export const LATE_REQUESTS_KEY = 'staffnow_late_requests'

export function loadLateRequests() {
  try { const r = localStorage.getItem(LATE_REQUESTS_KEY); return r ? JSON.parse(r) : [] } catch { return [] }
}
export function saveLateRequests(list) {
  try { localStorage.setItem(LATE_REQUESTS_KEY, JSON.stringify(list)) } catch { /* no-op */ }
}

function getPersonalKey(user) {
  const email = user?.email?.replace(/[^a-zA-Z0-9]/g, '_') || 'anon'
  return `staffnow_attendance_${email}`
}

function loadJSON(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    if (raw) return JSON.parse(raw)
  } catch { /* no-op */ }
  return fallback
}

function saveJSON(key, value) {
  try { localStorage.setItem(key, JSON.stringify(value)) } catch { /* no-op */ }
}

function nowTime() {
  const n = new Date()
  return `${String(n.getHours()).padStart(2, '0')}:${String(n.getMinutes()).padStart(2, '0')}`
}

export function getAssignedShifts() {
  return []
}

export function useAttendance() {
  const { user } = useAuth()
  const personalKey = getPersonalKey(user)

  // { [shiftId]: { checkIn, checkOut, status } }
  const [records, setRecords] = useState(() => loadJSON(personalKey, {}))

  const _persist = useCallback((next) => {
    saveJSON(personalKey, next)

    // 공유 풀 업데이트 (기업 근태 관리 페이지가 읽음)
    const shifts = getAssignedShifts(user?.name)
    const shared = shifts
      .filter(s => next[s.shiftId])
      .map(s => ({
        shiftId:        s.shiftId,
        userName:       user?.name ?? '—',
        userEmail:      user?.email ?? '',
        jobTitle:       s.jobTitle,
        company:        s.company,
        shiftDate:      s.shiftDate,
        scheduledStart: s.scheduledStart,
        scheduledEnd:   s.scheduledEnd,
        location:       s.location,
        checkIn:        next[s.shiftId]?.checkIn  ?? null,
        checkOut:       next[s.shiftId]?.checkOut ?? null,
        status:         next[s.shiftId]?.status   ?? 'scheduled',
      }))
    saveJSON(SHARED_ATTENDANCE_KEY, shared)
  }, [personalKey, user])

  const checkIn = useCallback((shiftId, location = null) => {
    setRecords(prev => {
      const next = {
        ...prev,
        [shiftId]: {
          ...prev[shiftId],
          checkIn: nowTime(),
          checkInAt: new Date().toISOString(),
          checkInLocation: location,
          status: 'in_progress',
        },
      }
      _persist(next)
      return next
    })
  }, [_persist])

  const checkOut = useCallback((shiftId, location = null) => {
    setRecords(prev => {
      const next = {
        ...prev,
        [shiftId]: {
          ...prev[shiftId],
          checkOut: nowTime(),
          checkOutAt: new Date().toISOString(),
          checkOutLocation: location,
          status: 'completed',
        },
      }
      _persist(next)
      return next
    })
  }, [_persist])

  // 시간 수정 — 수정 이력 누적 저장
  const editRecord = useCallback((shiftId, { checkIn, checkOut }) => {
    setRecords(prev => {
      const cur = prev[shiftId] ?? {}
      const historyEntry = {
        from: { checkIn: cur.checkIn ?? null, checkOut: cur.checkOut ?? null },
        to:   { checkIn: checkIn ?? cur.checkIn, checkOut: checkOut ?? cur.checkOut },
        editedAt: new Date().toISOString(),
      }
      const next = {
        ...prev,
        [shiftId]: {
          ...cur,
          checkIn:     checkIn  ?? cur.checkIn,
          checkOut:    checkOut ?? cur.checkOut,
          checkOutAt:  cur.checkOutAt, // 원본 퇴근 기록 시각 유지
          status:      'completed',
          editHistory: [...(cur.editHistory ?? []), historyEntry],
        },
      }
      _persist(next)
      return next
    })
  }, [_persist])

  // 기록 삭제 → 예정 상태로 초기화
  const deleteRecord = useCallback((shiftId) => {
    setRecords(prev => {
      const next = { ...prev }
      delete next[shiftId]
      _persist(next)
      return next
    })
  }, [_persist])

  // 제출 확정 → 수정 불가 상태로 잠금
  const submitRecord = useCallback((shiftId) => {
    setRecords(prev => {
      const next = {
        ...prev,
        [shiftId]: { ...prev[shiftId], status: 'submitted', submittedAt: new Date().toISOString() },
      }
      _persist(next)
      return next
    })
  }, [_persist])

  const getRecord = useCallback((shiftId) => records[shiftId] ?? null, [records])

  // 이의신청 제출
  const submitDispute = useCallback((shift, { note }) => {
    const existing = loadDisputes()
    const entry = {
      shiftId:    shift.shiftId,
      userName:   user?.name ?? '—',
      userEmail:  user?.email ?? '',
      jobTitle:   shift.jobTitle,
      company:    shift.company,
      shiftDate:  shift.shiftDate,
      note,
      status:     'pending',
      submittedAt: new Date().toISOString(),
    }
    const next = [...existing.filter(d => d.shiftId !== shift.shiftId), entry]
    saveDisputes(next)
  }, [user])

  return { records, checkIn, checkOut, editRecord, deleteRecord, submitRecord, submitDispute, getRecord }
}
