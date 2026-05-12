/*
 * ── 카카오맵 코드 (보관) ──────────────────────────────────────────────────────
 * 카카오맵(react-kakao-maps-sdk)으로 구현. 카카오맵 API 심사 승인 후 아래
 * Leaflet 코드를 주석처리하고 카카오맵 코드를 활성화할 것.
 * 카카오 앱키: 245d64d85a4f1e7dc5de2436590ebd32
 * ─────────────────────────────────────────────────────────────────────────── */
/*
import { useState } from 'react'
import { Map, MapMarker, CustomOverlayMap, useKakaoLoader } from 'react-kakao-maps-sdk'
import { X, MapPin, Navigation, Footprints, Car, ExternalLink, List, ChevronDown } from 'lucide-react'

const KAKAO_APP_KEY = '245d64d85a4f1e7dc5de2436590ebd32'
const SEOUL_CENTER  = { lat: 37.5172, lng: 127.0473 }

export default function MapView({ jobs, onJobClick }) {
  const [loading, error] = useKakaoLoader({ appkey: KAKAO_APP_KEY, libraries: ['services'] })
  const [selectedJob, setSelectedJob] = useState(null)
  const [userPos,     setUserPos]     = useState(null)
  const [locError,    setLocError]    = useState(false)
  const [locLoading,  setLocLoading]  = useState(false)
  const [listOpen,    setListOpen]    = useState(false)
  const [center,      setCenter]      = useState(SEOUL_CENTER)
  const [level,       setLevel]       = useState(5)

  // ... 전체 카카오맵 구현 코드 (MapView.jsx git 히스토리 참조)
}
*/

import { useState, useEffect } from 'react'
import { MapContainer, TileLayer, Marker, useMap } from 'react-leaflet'
import L from 'leaflet'
import { X, MapPin, Navigation, Footprints, Car, ExternalLink, List, ChevronDown } from 'lucide-react'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
})

const userIcon = L.divIcon({
  className: '',
  html: `<div style="
    width:18px;height:18px;
    background:#3B82F6;border:3px solid white;
    border-radius:50%;box-shadow:0 2px 6px rgba(59,130,246,0.6);
  "></div>`,
  iconSize: [18, 18],
  iconAnchor: [9, 9],
})

const SEOUL_CENTER = [37.5172, 127.0473]

function haversineKm(lat1, lng1, lat2, lng2) {
  const R = 6371
  const dLat = ((lat2 - lat1) * Math.PI) / 180
  const dLng = ((lng2 - lng1) * Math.PI) / 180
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLng / 2) ** 2
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

function estimateTimes(distKm) {
  return {
    walk:  Math.max(1, Math.round((distKm / 4) * 60)),
    drive: Math.max(1, Math.round((distKm * 1.3 / 25) * 60)),
  }
}

function MapController({ flyTarget, onFlown }) {
  const map = useMap()
  useEffect(() => {
    if (flyTarget) {
      map.flyTo([flyTarget.lat, flyTarget.lng], 16, { duration: 1 })
      onFlown()
    }
  }, [flyTarget])
  return null
}

function FlyToUser({ pos }) {
  const map = useMap()
  useEffect(() => {
    if (pos) map.flyTo([pos.lat, pos.lng], 15, { duration: 1.2 })
  }, [pos])
  return null
}

export default function MapView({ jobs, onJobClick }) {
  const [selectedJob,  setSelectedJob]  = useState(null)
  const [userPos,      setUserPos]      = useState(null)
  const [locError,     setLocError]     = useState(false)
  const [locLoading,   setLocLoading]   = useState(false)
  const [listOpen,     setListOpen]     = useState(false)
  const [flyTarget,    setFlyTarget]    = useState(null)

  const validJobs = jobs.filter(j => j.lat && j.lng)

  function handleLocate() {
    if (!navigator.geolocation) { setLocError(true); return }
    setLocLoading(true); setLocError(false)
    navigator.geolocation.getCurrentPosition(
      pos => { setUserPos({ lat: pos.coords.latitude, lng: pos.coords.longitude }); setLocLoading(false) },
      ()  => { setLocError(true); setLocLoading(false) },
      { timeout: 8000 }
    )
  }

  function handleJobListClick(job) {
    setListOpen(false)
    setSelectedJob(job)
    setFlyTarget({ lat: job.lat, lng: job.lng, _ts: Date.now() })
  }

  const distInfo = userPos && selectedJob
    ? (() => {
        const d = haversineKm(userPos.lat, userPos.lng, selectedJob.lat, selectedJob.lng)
        return { dist: d, ...estimateTimes(d) }
      })()
    : null

  return (
    <div className="relative w-full rounded-2xl overflow-hidden border border-offwhite-200" style={{ height: '65vh', minHeight: 400 }}>
      <MapContainer center={SEOUL_CENTER} zoom={13} style={{ width: '100%', height: '100%' }}>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        {validJobs.map(job => (
          <Marker
            key={job.id}
            position={[job.lat, job.lng]}
            eventHandlers={{ click: () => setSelectedJob(prev => prev?.id === job.id ? null : job) }}
          />
        ))}
        {userPos && <Marker position={[userPos.lat, userPos.lng]} icon={userIcon} />}
        {userPos && <FlyToUser pos={userPos} />}
        {flyTarget && (
          <MapController
            flyTarget={flyTarget}
            onFlown={() => setFlyTarget(null)}
          />
        )}
      </MapContainer>

      {/* ── 공고 목록 토글 버튼 + 드롭다운 (하단 중앙) ── */}
      <div className="absolute bottom-8 left-1/2 -translate-x-1/2 z-[1000] flex flex-col items-center">
        {listOpen && (
          <div className="mb-2 w-64 bg-white rounded-2xl shadow-xl border border-offwhite-200 overflow-hidden">
            <p className="px-3 py-2 text-[11px] font-semibold text-gray-400 border-b border-offwhite-200">
              공고를 눌러 위치로 이동
            </p>
            <div className="max-h-56 overflow-y-auto">
              {validJobs.map(job => (
                <button
                  key={job.id}
                  onClick={() => handleJobListClick(job)}
                  className={`w-full text-left px-3 py-2.5 border-b border-offwhite-100 hover:bg-offwhite transition-colors last:border-0 ${
                    selectedJob?.id === job.id ? 'bg-orange/5' : ''
                  }`}
                >
                  <p className={`text-xs font-bold leading-tight line-clamp-1 ${selectedJob?.id === job.id ? 'text-orange' : 'text-navy'}`}>
                    {job.title}
                  </p>
                  <p className="text-[11px] text-gray-400 mt-0.5 flex items-center gap-1">
                    <MapPin size={10} />
                    {job.location}
                  </p>
                  <p className="text-[11px] font-semibold text-orange mt-0.5">{job.wage}</p>
                </button>
              ))}
            </div>
          </div>
        )}
        <button
          onClick={() => setListOpen(v => !v)}
          className={`flex items-center gap-1.5 px-4 py-2 rounded-full text-xs font-semibold shadow-lg border transition-colors ${
            listOpen
              ? 'bg-navy text-white border-navy'
              : 'bg-white/95 text-navy border-offwhite-200 hover:border-navy'
          } backdrop-blur-sm`}
        >
          <List size={12} />
          {validJobs.length}개 공고
          <ChevronDown size={12} className={`transition-transform ${listOpen ? 'rotate-180' : ''}`} />
        </button>
      </div>

      {/* ── 내 위치 버튼 ── */}
      <div className="absolute top-3 right-3 z-[1000] flex flex-col items-end gap-1.5">
        <button
          onClick={handleLocate}
          disabled={locLoading}
          className={`flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold shadow border transition-colors ${
            userPos ? 'bg-blue-500 text-white border-blue-500' : 'bg-white text-navy border-offwhite-200 hover:border-navy'
          } disabled:opacity-60`}
        >
          <Navigation size={13} className={locLoading ? 'animate-spin' : ''} />
          {locLoading ? '위치 확인 중...' : userPos ? '내 위치 설정됨' : '내 위치'}
        </button>
        {locError && (
          <p className="text-[11px] text-red-500 bg-white/90 px-2 py-1 rounded-lg shadow">
            위치 권한을 허용해주세요
          </p>
        )}
      </div>

      {/* ── 공고 팝업 ── */}
      {selectedJob && (
        <div className="absolute bottom-14 left-1/2 -translate-x-1/2 z-[1000] bg-white rounded-2xl shadow-xl border border-offwhite-200 w-72 overflow-hidden">
          <div className="flex items-start justify-between px-4 pt-3 pb-2">
            <div className="flex-1 min-w-0 pr-2">
              <p className="text-sm font-bold text-navy leading-tight line-clamp-2">{selectedJob.title}</p>
              <p className="text-xs text-gray-500 mt-0.5">{selectedJob.company}</p>
            </div>
            <button onClick={() => setSelectedJob(null)} className="text-gray-300 hover:text-gray-500 shrink-0 mt-0.5">
              <X size={14} />
            </button>
          </div>
          <div className="px-4 pb-2 space-y-1">
            <div className="flex items-center gap-1 text-xs text-gray-400">
              <MapPin size={11} className="shrink-0" />
              <span className="truncate">{selectedJob.location}</span>
            </div>
            <p className="text-base font-extrabold text-orange">{selectedJob.wage}</p>
          </div>

          {distInfo && (
            <div className="mx-4 mb-3 p-2.5 bg-blue-50 rounded-xl border border-blue-100">
              <p className="text-[11px] font-semibold text-blue-600 mb-1.5 flex items-center gap-1">
                <Navigation size={11} />
                직선 {distInfo.dist < 1
                  ? `${Math.round(distInfo.dist * 1000)}m`
                  : `${distInfo.dist.toFixed(1)}km`}
              </p>
              <div className="flex items-center gap-2 text-[11px] text-gray-600">
                <span className="flex items-center gap-0.5"><Footprints size={11} className="text-gray-400" />약 {distInfo.walk}분</span>
                <span className="text-gray-300">|</span>
                <span className="flex items-center gap-0.5"><Car size={11} className="text-gray-400" />약 {distInfo.drive}분</span>
                <a
                  href={`https://map.kakao.com/link/to/${encodeURIComponent(selectedJob.company)},${selectedJob.lat},${selectedJob.lng}`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="ml-auto flex items-center gap-0.5 text-blue-500 hover:text-blue-700 font-semibold"
                  onClick={e => e.stopPropagation()}
                >
                  길찾기 <ExternalLink size={10} />
                </a>
              </div>
            </div>
          )}

          <div className="px-4 pb-3 flex flex-wrap gap-1">
            {selectedJob.tags.map(t => (
              <span key={t} className="text-[10px] bg-offwhite px-2 py-0.5 rounded-full text-gray-500">{t}</span>
            ))}
          </div>
          <button
            onClick={() => onJobClick(selectedJob.id)}
            className="w-full py-2.5 bg-navy text-white text-sm font-semibold hover:bg-navy/80 transition-colors"
          >
            자세히 보기 →
          </button>
        </div>
      )}
    </div>
  )
}
