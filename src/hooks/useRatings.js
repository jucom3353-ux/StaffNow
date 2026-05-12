import { useState } from 'react'

const RATINGS_KEY = 'staffnow_ratings'

export function loadRatings() {
  try {
    const raw = localStorage.getItem(RATINGS_KEY)
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}

function saveRatings(data) {
  localStorage.setItem(RATINGS_KEY, JSON.stringify(data))
}

export function useRatings() {
  const [ratings, setRatings] = useState(loadRatings)

  function addRating({ recordId, staffName, shiftLabel, stars, comment }) {
    if (ratings.some(r => r.recordId === recordId)) return
    const next = [...ratings, {
      id: Date.now().toString(),
      recordId,
      staffName,
      shiftLabel,
      stars,
      comment: comment ?? '',
      createdAt: new Date().toISOString(),
    }]
    saveRatings(next)
    setRatings(next)
  }

  function hasRated(recordId) {
    return ratings.some(r => r.recordId === recordId)
  }

  function getAverageRating(staffName) {
    const mine = ratings.filter(r => r.staffName === staffName)
    if (!mine.length) return null
    return Math.round((mine.reduce((s, r) => s + r.stars, 0) / mine.length) * 10) / 10
  }

  function getStaffRatings(staffName) {
    return ratings.filter(r => r.staffName === staffName)
  }

  return { ratings, addRating, hasRated, getAverageRating, getStaffRatings }
}
