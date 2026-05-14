import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { jobApi } from '../services/api'

const AppDataContext = createContext(null)

const STATUS_FROM_API = { ACTIVE: 'active', DRAFT: 'draft', CLOSED: 'closed', COMPLETED: 'completed', CANCELLED: 'cancelled' }
const WAGE_TYPE_FROM_API = { HOURLY: 'hourly', DAILY: 'daily', MONTHLY: 'fixed', FIXED: 'fixed' }
const STATUS_TO_API = { active: 'ACTIVE', draft: 'DRAFT', closed: 'CLOSED', completed: 'COMPLETED', cancelled: 'CANCELLED' }
const WAGE_TYPE_TO_API = { hourly: 'HOURLY', daily: 'DAILY', fixed: 'MONTHLY' }

function normalizeJob(j) {
  return {
    id: String(j.id),
    title: j.title ?? '',
    status: STATUS_FROM_API[j.postStatus] ?? (j.postStatus?.toLowerCase() ?? 'draft'),
    location: j.workLocation ?? '',
    headcount: j.recruitCount ?? 0,
    filledCount: j.currentCount ?? 0,
    createdAt: j.createdAt ? String(j.createdAt).slice(0, 10) : '',
    createdBy: j.companyName ?? '',
    wageType: WAGE_TYPE_FROM_API[j.wageType] ?? (j.wageType?.toLowerCase() ?? 'hourly'),
    wage: j.wageAmount ? j.wageAmount.toLocaleString('ko-KR') : '',
    description: j.content ?? j.description ?? '',
    requirements: [j.requiredCondition, j.preferredExperience, j.preferredEtc].filter(Boolean).join('\n'),
    category: j.category ?? '',
    startTime: j.startTime ?? '',
    endTime: j.endTime ?? '',
    shifts: [],
  }
}

function denormalizeJob(formData, status = 'ACTIVE') {
  const wageAmount = parseInt(String(formData.wage ?? '').replace(/[^0-9]/g, ''), 10) || 0
  return {
    title: formData.title ?? '',
    content: formData.description ?? '',
    workLocation: formData.location ?? '',
    startTime: formData.startTime ?? null,
    endTime: formData.endTime ?? null,
    breakTime: formData.breakMin ? Number(formData.breakMin) : null,
    wageType: WAGE_TYPE_TO_API[formData.wageType] ?? 'HOURLY',
    wageAmount,
    includeHolidayPay: false,
    workType: null,
    description: formData.description ?? '',
    requiredGender: null,
    requiredAgeMin: null,
    requiredAgeMax: null,
    requiredPersonality: Array.isArray(formData.requiredChips) ? formData.requiredChips.join(', ') : null,
    requiredCondition: formData.requirements ?? null,
    preferredExperience: Array.isArray(formData.preferredChips) ? formData.preferredChips.join(', ') : null,
    preferredLanguage: null,
    preferredEtc: null,
    recruitCount: Number(formData.headcount) || 1,
    postStatus: STATUS_TO_API[formData.status] ?? status,
    category: Array.isArray(formData.categories) ? formData.categories.join(', ') : (formData.category ?? null),
    deadline: formData.deadline ?? null,
  }
}

const SHIFTS_KEY = 'staffnow_shifts_v1'
const INVITATIONS_KEY = 'staffnow_invitations_v1'

function loadFromStorage(key) {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return null
    return JSON.parse(raw)
  } catch { return null }
}

function getCurrentUserEmail() {
  try {
    const u = JSON.parse(localStorage.getItem('staffnow_auth_user') || 'null')
    return u?.email?.replace(/[^a-zA-Z0-9]/g, '_') || 'anon'
  } catch { return 'anon' }
}

function getMessagesKey() {
  return `staffnow_messages_${getCurrentUserEmail()}`
}

export function AppDataProvider({ children }) {
  const [jobs, setJobs] = useState([])

  useEffect(() => {
    jobApi.myList()
      .then(res => res.ok ? res.json() : [])
      .then(data => { if (Array.isArray(data)) setJobs(data.map(normalizeJob)) })
      .catch(() => {})
  }, [])
  const [shifts, setShifts] = useState(() => loadFromStorage(SHIFTS_KEY) ?? [])
  const [activities, setActivities] = useState([])
  const [invitations, setInvitations] = useState(() => loadFromStorage(INVITATIONS_KEY) ?? [])
  const [conversations, setConversations] = useState(() => loadFromStorage(getMessagesKey()) ?? [])
  const [toasts, setToasts] = useState([])

  useEffect(() => {
    try { localStorage.setItem(SHIFTS_KEY, JSON.stringify(shifts)) } catch {}
  }, [shifts])

  useEffect(() => {
    try { localStorage.setItem(INVITATIONS_KEY, JSON.stringify(invitations)) } catch {}
  }, [invitations])

  useEffect(() => {
    try { localStorage.setItem(getMessagesKey(), JSON.stringify(conversations)) } catch {}
  }, [conversations])

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }, [])

  const addToast = useCallback(({ type = 'success', message }) => {
    const id = Date.now() + Math.random()
    setToasts(prev => [...prev, { id, type, message }])
    setTimeout(() => removeToast(id), 4000)
  }, [removeToast])

  const refreshJobs = useCallback(async () => {
    try {
      const res = await jobApi.myList()
      if (res.ok) {
        const data = await res.json()
        if (Array.isArray(data)) setJobs(data.map(normalizeJob))
        return data.map(normalizeJob)
      }
    } catch {}
    return []
  }, [])

  const addJob = useCallback(async (formData) => {
    try {
      const payload = denormalizeJob(formData, 'ACTIVE')
      const res = await jobApi.create(payload)
      if (!res.ok) {
        addToast({ type: 'error', message: '공고 생성에 실패했습니다' })
        return null
      }
      const updated = await refreshJobs()
      const newJob = updated[0] ?? null
      if (newJob) {
        addToast({ type: 'success', message: `"${formData.title}" 공고가 생성되었습니다` })
        setActivities(prev => [{
          id: Date.now(), type: 'job_created',
          text: `"${formData.title}" 공고가 생성되었습니다`,
          time: '방금', actor: formData.createdBy || '알 수 없음',
        }, ...prev])
      }
      return newJob
    } catch {
      addToast({ type: 'error', message: '공고 생성 중 오류가 발생했습니다' })
      return null
    }
  }, [addToast, refreshJobs])

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
    jobApi.updateStatus(jobId, STATUS_TO_API[status] ?? status.toUpperCase())
      .catch(() => {})
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
    const payload = denormalizeJob(formData)
    jobApi.update(jobId, payload).catch(() => {})
    addToast({ type: 'success', message: '공고가 수정되었습니다' })
  }, [addToast])

  const deleteJob = useCallback((jobId) => {
    setJobs(prev => {
      const job = prev.find(j => j.id === jobId)
      addToast({ type: 'info', message: `"${job?.title}" 공고가 삭제되었습니다` })
      return prev.filter(j => j.id !== jobId)
    })
    jobApi.delete(jobId).catch(() => {})
  }, [addToast])

  const updateShiftConfirmed = useCallback((shiftId, count) => {
    setShifts(prev => prev.map(s =>
      s.id === shiftId ? { ...s, confirmedStaff: count } : s
    ))
  }, [])

  const finalizeShift = useCallback((shiftId, { hiredCount, applicantStates }) => {
    setShifts(prev => prev.map(s =>
      s.id === shiftId
        ? { ...s, confirmedStaff: hiredCount, status: 'completed', applicantStates }
        : s
    ))
    addToast({ type: 'success', message: `${hiredCount}명 채용 확정 완료!` })
  }, [addToast])

  const addInvitation = useCallback((data) => {
    const invId = `inv-${Date.now()}`
    const newInv = {
      id: invId,
      status: 'pending',
      sentAt: new Date().toISOString().slice(0, 10),
      ...data,
    }
    setInvitations(prev => [newInv, ...prev])

    try {
      const companyName = data.companyName || '기업'
      const inviteMsg = {
        id: `m-${invId}`,
        from: 'company',
        text: '',
        type: 'invite',
        invite: {
          inviteId: invId,
          role: data.role,
          shiftLabel: data.shiftLabel,
          wage: data.wage,
          companyName,
          status: 'pending',
        },
        time: new Date().toISOString(),
        read: false,
        deleted: false,
        file: null,
      }
      const indKey = 'staffnow_ind_messages_user_staffnow_kr'
      const raw = localStorage.getItem(indKey)
      const convs = raw ? JSON.parse(raw) : []
      const convIdx = convs.findIndex(c => c.companyName === companyName)
      if (convIdx >= 0) {
        convs[convIdx] = { ...convs[convIdx], messages: [...convs[convIdx].messages, inviteMsg] }
      } else {
        convs.unshift({
          id: `ic-${invId}`,
          companyName,
          online: true,
          blocked: false,
          left: false,
          messages: [inviteMsg],
        })
      }
      localStorage.setItem(indKey, JSON.stringify(convs))
    } catch {}

    addToast({ type: 'success', message: `${data.staffName}님께 초대를 발송했습니다` })
    return newInv
  }, [addToast])

  const updateInvitationStatus = useCallback((invId, status) => {
    setInvitations(prev => prev.map(i => i.id === invId ? { ...i, status } : i))
  }, [])

  const sendMessage = useCallback((convId, { text, file = null }) => {
    const newMsg = {
      id: `m-${Date.now()}`,
      from: 'biz',
      text: text ?? '',
      time: new Date().toISOString(),
      read: true,
      deleted: false,
      file,
    }
    setConversations(prev =>
      prev.map(c => c.id === convId ? { ...c, messages: [...c.messages, newMsg] } : c)
    )
  }, [])

  const editMessage = useCallback((convId, msgId, newText) => {
    setConversations(prev =>
      prev.map(c => c.id === convId
        ? { ...c, messages: c.messages.map(m => m.id === msgId ? { ...m, text: newText, edited: true } : m) }
        : c
      )
    )
  }, [])

  const deleteMessage = useCallback((convId, msgId) => {
    setConversations(prev =>
      prev.map(c => c.id === convId
        ? { ...c, messages: c.messages.map(m => m.id === msgId ? { ...m, deleted: true } : m) }
        : c
      )
    )
  }, [])

  const markAsRead = useCallback((convId) => {
    setConversations(prev =>
      prev.map(c => c.id === convId
        ? { ...c, messages: c.messages.map(m => ({ ...m, read: true })) }
        : c
      )
    )
  }, [])

  const blockConversation = useCallback((convId) => {
    setConversations(prev =>
      prev.map(c => c.id === convId ? { ...c, blocked: true } : c)
    )
    addToast({ type: 'info', message: '차단 처리되었습니다' })
  }, [addToast])

  const leaveConversation = useCallback((convId) => {
    setConversations(prev =>
      prev.map(c => c.id === convId ? { ...c, left: true } : c)
    )
    addToast({ type: 'info', message: '대화방을 나갔습니다' })
  }, [addToast])

  const markAsPaid = useCallback((shiftId) => {
    setShifts(prev => prev.map(s =>
      s.id === shiftId ? { ...s, isPaid: true } : s
    ))
    addToast({ type: 'success', message: '정산 완료 처리되었습니다' })
  }, [addToast])

  const reinitializeConversations = useCallback(() => {
    setConversations(loadFromStorage(getMessagesKey()) ?? [])
  }, [])

  return (
    <AppDataContext.Provider value={{
      jobs, shifts, activities, invitations, conversations,
      addJob, addShift, updateJob, updateJobStatus, deleteJob,
      updateShiftConfirmed, finalizeShift,
      addInvitation, updateInvitationStatus,
      sendMessage, editMessage, deleteMessage, markAsRead,
      blockConversation, leaveConversation, markAsPaid,
      reinitializeConversations,
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
