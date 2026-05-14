import { createContext, useContext, useState, useCallback, useEffect } from 'react'

const AppDataContext = createContext(null)

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

  const addJob = useCallback((formData) => {
    const newJob = {
      id: `job-${Date.now()}`,
      title: formData.title,
      status: 'active',
      location: formData.location,
      headcount: Number(formData.headcount),
      filledCount: 0,
      createdAt: new Date().toISOString().slice(0, 10),
      createdBy: formData.createdBy || '알 수 없음',
      shifts: [],
    }
    setJobs(prev => [newJob, ...prev])
    setActivities(prev => [{
      id: Date.now(),
      type: 'job_created',
      text: `"${formData.title}" 공고가 생성되었습니다`,
      time: '방금',
      actor: formData.createdBy || '알 수 없음',
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
