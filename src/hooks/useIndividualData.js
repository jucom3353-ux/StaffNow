import { useState, useCallback, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { bookmarkApi } from '../services/api'

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
  mbti:           '',
}

export function useIndividualData() {
  const { user } = useAuth()

  const [savedJobIds, setSavedJobIds] = useState([])

  const [applications, setApplications] = useState(() =>
    loadJSON(getKey(user, 'applications'), [])
  )

  const [profile, setProfile] = useState(() => {
    const stored = loadJSON(getKey(user, 'profile'), null)
    return stored ?? {
      ...DEFAULT_PROFILE,
      phone:         user?.phone          || '',
      address:       user?.address        || '',
      addressDetail: user?.addressDetail  || '',
      mbti:          user?.mbti           || '',
    }
  })

  useEffect(() => {
    if (!user) return
    bookmarkApi.myList()
      .then(r => r.ok ? r.json() : [])
      .then(data => {
        const ids = Array.isArray(data) ? data.map(job => String(job.id)) : []
        setSavedJobIds(ids)
      })
      .catch(() => {})
  }, [user])

  const toggleSave = useCallback((jobId) => {
    const id = String(jobId)
    setSavedJobIds(prev => {
      const isCurrentlySaved = prev.includes(id)
      const next = isCurrentlySaved ? prev.filter(x => x !== id) : [...prev, id]

      const apiCall = isCurrentlySaved ? bookmarkApi.remove(id) : bookmarkApi.add(id)
      apiCall.catch(() => setSavedJobIds(prev2 => {
        const stillToggled = !prev2.includes(id)
        return stillToggled
          ? isCurrentlySaved ? [...prev2, id] : prev2.filter(x => x !== id)
          : prev2
      }))

      return next
    })
  }, [])

  const isSaved = (jobId) => savedJobIds.includes(String(jobId))

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
    updateProfile,
  }
}
