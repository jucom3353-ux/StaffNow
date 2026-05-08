import { useState, useCallback } from 'react'
import { useAuth } from '../context/AuthContext'

function getKey(user, type) {
  const email = user?.email?.replace(/[^a-zA-Z0-9]/g, '_') || 'anon'
  return `staffnow_ind_${type}_${email}`
}

function loadJSON(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    if (raw) return JSON.parse(raw)
  } catch {}
  return fallback
}

function saveJSON(key, value) {
  try { localStorage.setItem(key, JSON.stringify(value)) } catch {}
}

const DEFAULT_PROFILE = {
  phone:          '',
  address:        '',
  addressDetail:  '',
  bio:            '',
  skills:         [],
  regions:        [],
  preferredTimes: [],
  account:        null,
  documents:      { health: false, safety: false },
}

export function useIndividualData() {
  const { user } = useAuth()

  const [savedJobIds, setSavedJobIds] = useState(() =>
    loadJSON(getKey(user, 'saved'), [])
  )

  const [applications, setApplications] = useState(() =>
    loadJSON(getKey(user, 'applications'), [])
  )

  const [profile, setProfile] = useState(() => {
    const stored = loadJSON(getKey(user, 'profile'), null)
    return stored ?? {
      ...DEFAULT_PROFILE,
      phone:        user?.phone         || '',
      address:      user?.address       || '',
      addressDetail: user?.addressDetail || '',
    }
  })

  const toggleSave = useCallback((jobId) => {
    setSavedJobIds(prev => {
      const next = prev.includes(jobId)
        ? prev.filter(id => id !== jobId)
        : [...prev, jobId]
      saveJSON(getKey(user, 'saved'), next)
      return next
    })
  }, [user])

  // plain function — savedJobIds가 바뀌면 컴포넌트가 이미 재렌더되므로 useCallback 불필요
  const isSaved = (jobId) => savedJobIds.includes(jobId)

  const applyJob = useCallback(({ jobId, jobTitle, company, wage, location }) => {
    setApplications(prev => {
      if (prev.some(a => a.jobId === jobId)) return prev
      const next = [{
        id:        `app-${Date.now()}`,
        jobId,
        jobTitle,
        company,
        wage,
        location,
        appliedAt: new Date().toISOString().slice(0, 10),
        status:    'pending',
      }, ...prev]
      saveJSON(getKey(user, 'applications'), next)
      return next
    })
  }, [user])

  const isApplied = useCallback((jobId) => applications.some(a => a.jobId === jobId), [applications])

  const cancelApplication = useCallback((appId) => {
    setApplications(prev => {
      const next = prev.filter(a => a.id !== appId)
      saveJSON(getKey(user, 'applications'), next)
      return next
    })
  }, [user])

  const updateProfile = useCallback((patch) => {
    setProfile(prev => {
      const next = { ...prev, ...patch }
      saveJSON(getKey(user, 'profile'), next)
      return next
    })
  }, [user])

  return {
    savedJobIds,
    applications,
    profile,
    toggleSave,
    isSaved,
    applyJob,
    isApplied,
    cancelApplication,
    updateProfile,
  }
}
