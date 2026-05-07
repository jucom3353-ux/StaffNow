import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { MOCK_JOBS } from '../data/mockJobs'
import { MOCK_SHIFTS } from '../data/mockShifts'
import { RECENT_ACTIVITIES } from '../data/mockDashboard'
import { MOCK_INVITATIONS } from '../data/mockInvitations'

const AppDataContext = createContext(null)

// ── localStorage 키 ───────────────────────────────────────
const SHIFTS_KEY = 'staffnow_shifts_v1'
const INVITATIONS_KEY = 'staffnow_invitations_v1'

function loadFromStorage(key) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return null
    return JSON.parse(raw)
  } catch { return null }
}

function buildInitialShifts() {
  const saved = loadFromStorage(SHIFTS_KEY)
  if (!saved?.length) return MOCK_SHIFTS
  const savedMap = new Map(saved.map(s => [s.id, s]))
  const merged = MOCK_SHIFTS.map(s =>
    savedMap.has(s.id) ? { ...s, ...savedMap.get(s.id) } : s
  )
  const mockIds = new Set(MOCK_SHIFTS.map(s => s.id))
  saved.forEach(s => { if (!mockIds.has(s.id)) merged.push(s) })
  return merged
}

function buildInitialInvitations() {
  const saved = loadFromStorage(INVITATIONS_KEY)
  if (!saved?.length) return MOCK_INVITATIONS
  const savedMap = new Map(saved.map(i => [i.id, i]))
  const merged = MOCK_INVITATIONS.map(i =>
    savedMap.has(i.id) ? { ...i, ...savedMap.get(i.id) } : i
  )
  const mockIds = new Set(MOCK_INVITATIONS.map(i => i.id))
  saved.forEach(i => { if (!mockIds.has(i.id)) merged.push(i) })
  return merged
}

export function AppDataProvider({ children }) {
  const [jobs, setJobs] = useState(MOCK_JOBS)
  const [shifts, setShifts] = useState(buildInitialShifts)
  const [activities, setActivities] = useState(RECENT_ACTIVITIES)
  const [invitations, setInvitations] = useState(buildInitialInvitations)
  const [toasts, setToasts] = useState([])

  useEffect(() => {
    try { localStorage.setItem(SHIFTS_KEY, JSON.stringify(shifts)) } catch {}
  }, [shifts])

  useEffect(() => {
    try { localStorage.setItem(INVITATIONS_KEY, JSON.stringify(invitations)) } catch {}
  }, [invitations])

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }, [])

  const addToast = useCallback(({ type = 'success', message }) => {
    const id = Date.now() + Math.random()
    setToasts(prev => [...prev, { id, type, message }])
    setTimeout(() => removeToast(id), 4000)
  }, [removeToast])

  const addJob = useCallback((formData) => {
    const newJob = {
      id: `job-${Date.now()}`,
      title: formData.title,
      status: 'active',
      location: formData.location,
      headcount: Number(formData.headcount),
      filledCount: 0,
      createdAt: new Date().toISOString().slice(0, 10),
      createdBy: '김운영',
      shifts: [],
    }
    setJobs(prev => [newJob, ...prev])
    setActivities(prev => [{
      id: Date.now(),
      type: 'job_created',
      text: `"${formData.title}" 공고가 생성되었습니다`,
      time: '방금',
      actor: '김운영',
    }, ...prev])
    addToast({ type: 'success', message: `"${formData.title}" 공고가 생성되었습니다` })
    return newJob
  }, [addToast])

  const addShift = useCallback((formData) => {
    const job = jobs.find(j => j.id === formData.jobId)
    const newShift = {
      id: `shift-${Date.now()}`,
      jobId: formData.jobId,
      jobTitle: job?.title ?? formData.jobId,
      date: formData.date,
      startTime: formData.startTime,
      endTime: formData.endTime,
      location: job?.location ?? '',
      requiredStaff: Number(formData.requiredStaff) || 1,
      confirmedStaff: 0,
      status: 'scheduled',
    }
    setShifts(prev => [newShift, ...prev])
    addToast({ type: 'success', message: `${formData.date} Shift가 생성되었습니다` })
    return newShift
  }, [jobs, addToast])

  const updateJobStatus = useCallback((jobId, status) => {
    setJobs(prev => prev.map(j => j.id === jobId ? { ...j, status } : j))
    addToast({ type: 'info', message: '공고 상태가 변경되었습니다' })
  }, [addToast])

  const updateJob = useCallback((jobId, formData) => {
    setJobs(prev => prev.map(j => j.id === jobId ? {
      ...j,
      title: formData.title,
      location: formData.location,
      headcount: Number(formData.headcount),
      wage: formData.wage,
      wageType: formData.wageType,
      description: formData.description,
      requirements: formData.requirements,
    } : j))
    addToast({ type: 'success', message: '공고가 수정되었습니다' })
  }, [addToast])

  const deleteJob = useCallback((jobId) => {
    setJobs(prev => {
      const job = prev.find(j => j.id === jobId)
      addToast({ type: 'info', message: `"${job?.title}" 공고가 삭제되었습니다` })
      return prev.filter(j => j.id !== jobId)
    })
  }, [addToast])

  const updateShiftConfirmed = useCallback((shiftId, count) => {
    setShifts(prev => prev.map(s =>
      s.id === shiftId ? { ...s, confirmedStaff: count } : s
    ))
  }, [])

  // 채용 최종 확정: confirmedStaff + status → completed + applicantStates 저장
  const finalizeShift = useCallback((shiftId, { hiredCount, applicantStates }) => {
    setShifts(prev => prev.map(s =>
      s.id === shiftId
        ? {
            ...s,
            confirmedStaff: hiredCount,
            status: 'completed',
            applicantStates,            // 전체 채용/거절/미결정 매핑 보존
          }
        : s
    ))
    addToast({ type: 'success', message: `${hiredCount}명 채용 확정 완료!` })
  }, [addToast])

  const addInvitation = useCallback((data) => {
    const newInv = {
      id: `inv-${Date.now()}`,
      status: 'pending',
      sentAt: new Date().toISOString().slice(0, 10),
      ...data,
    }
    setInvitations(prev => [newInv, ...prev])
    addToast({ type: 'success', message: `${data.staffName}님께 초대를 발송했습니다` })
    return newInv
  }, [addToast])

  const updateInvitationStatus = useCallback((invId, status) => {
    setInvitations(prev => prev.map(i => i.id === invId ? { ...i, status } : i))
  }, [])

  // 데모 초기화: 모든 staffnow_ localStorage 키 삭제 + state 리셋
  const resetDemoData = useCallback(() => {
    Object.keys(localStorage)
      .filter(k => k.startsWith('staffnow_'))
      .forEach(k => localStorage.removeItem(k))
    setShifts(MOCK_SHIFTS)
    setJobs(MOCK_JOBS)
    setActivities(RECENT_ACTIVITIES)
    setInvitations(MOCK_INVITATIONS)
    addToast({ type: 'info', message: '데모 데이터가 초기화되었습니다' })
  }, [addToast])

  return (
    <AppDataContext.Provider value={{
      jobs, shifts, activities, invitations,
      addJob, addShift, updateJob, updateJobStatus, deleteJob,
      updateShiftConfirmed, finalizeShift, resetDemoData,
      addInvitation, updateInvitationStatus,
      toasts, addToast, removeToast,
    }}>
      {children}
    </AppDataContext.Provider>
  )
}

export function useAppData() {
  const ctx = useContext(AppDataContext)
  if (!ctx) throw new Error('useAppData must be used inside AppDataProvider')
  return ctx
}
