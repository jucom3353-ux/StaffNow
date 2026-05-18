import { useState, useEffect } from 'react'
import CameraCapture from '../../components/camera/CameraCapture'
import { MapContainer, TileLayer, Marker, useMap } from 'react-leaflet'
import L from 'leaflet'
import {
  Clock, CheckCircle2, MapPin, ChevronRight, LogIn, LogOut,
  CalendarDays, Pencil, Trash2, Send, X, AlertTriangle, History,
  Camera, Wifi, ImageIcon, Navigation,
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { useAttendance, loadDisputes, loadLateRequests, saveLateRequests } from '../../hooks/useAttendance'
import { applicationApi, workAttendanceApi } from '../../services/api'

const WAGE_LABEL = { HOURLY: '시급', DAILY: '일급', MONTHLY: '월급', FIXED: '고정급' }

// ── Leaflet 마커 아이콘 ────────────────────────────────────
const userLocationIcon = L.divIcon({
  className: '',
  html: `<div style="width:16px;height:16px;background:#3B82F6;border:3px solid white;border-radius:50%;box-shadow:0 2px 6px rgba(59,130,246,0.6);"></div>`,
  iconSize: [16, 16],
  iconAnchor: [8, 8],
})

const worksiteIcon = L.divIcon({
  className: '',
  html: `<div style="width:22px;height:22px;background:#f97316;border:3px solid white;border-radius:6px;box-shadow:0 2px 6px rgba(249,115,22,0.5);display:flex;align-items:center;justify-content:center;"><svg xmlns='http://www.w3.org/2000/svg' width='11' height='11' viewBox='0 0 24 24' fill='white'><path d='M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z'/></svg></div>`,
  iconSize: [22, 22],
  iconAnchor: [11, 11],
})

// 두 마커를 모두 포함하도록 지도 범위 조정
function FitMapBounds({ userPos, shiftPos }) {
  const map = useMap()
  useEffect(() => {
    if (userPos && shiftPos) {
      map.fitBounds(
        [[userPos.lat, userPos.lng], [shiftPos.lat, shiftPos.lng]],
        { padding: [30, 30], maxZoom: 16, animate: true }
      )
    } else if (userPos) {
      map.flyTo([userPos.lat, userPos.lng], 16, { duration: 1 })
    }
  }, [userPos, shiftPos, map])
  return null
}

// 거리 배지
function DistanceBadge({ distanceM }) {
  if (distanceM < 300) return (
    <span className="inline-flex items-center gap-1 text-[11px] bg-green-50 border border-green-200 text-green-700 rounded-full px-2.5 py-1 font-semibold">
      <CheckCircle2 size={10} />근무지 근처 ({Math.round(distanceM)}m)
    </span>
  )
  if (distanceM < 1000) return (
    <span className="inline-flex items-center gap-1 text-[11px] bg-amber-50 border border-amber-200 text-amber-700 rounded-full px-2.5 py-1 font-semibold">
      <AlertTriangle size={10} />{Math.round(distanceM)}m 거리
    </span>
  )
  return (
    <span className="inline-flex items-center gap-1 text-[11px] bg-red-50 border border-red-200 text-red-600 rounded-full px-2.5 py-1 font-semibold">
      <AlertTriangle size={10} />위치 불일치 ({(distanceM / 1000).toFixed(1)}km)
    </span>
  )
}

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
function getLateMinutes(scheduledStart, now) {
  const [h, m] = (scheduledStart || '00:00').split(':').map(Number)
  const scheduled = h * 60 + m
  const current = now.getHours() * 60 + now.getMinutes()
  return Math.max(0, current - scheduled)
}

function haversineDistance(lat1, lng1, lat2, lng2) {
  const R = 6371000
  const toRad = x => x * Math.PI / 180
  const dLat = toRad(lat2 - lat1)
  const dLng = toRad(lng2 - lng1)
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

const STATUS = {
  scheduled:   { label: '예정',    color: 'bg-gray-100 text-gray-500' },
  in_progress: { label: '근무 중', color: 'bg-blue-100 text-blue-600' },
  completed:   { label: '완료',    color: 'bg-green-100 text-green-600' },
  submitted:   { label: '제출됨',  color: 'bg-navy/10 text-navy' },
}

const EDIT_WINDOW_MIN = 30

function getEditWindowInfo(rec, now) {
  if (rec?.status !== 'completed') return { canEdit: false, remaining: 0 }
  if (!rec?.checkOutAt) return { canEdit: true, remaining: EDIT_WINDOW_MIN }
  const elapsed = (now - new Date(rec.checkOutAt)) / 60000
  const remaining = Math.max(0, EDIT_WINDOW_MIN - elapsed)
  return { canEdit: remaining > 0, remaining: Math.ceil(remaining) }
}

// ── 위치 배지 ──────────────────────────────────────────────
function LocationBadge({ checkInLocation, checkOutLocation }) {
  if (!checkInLocation && !checkOutLocation) return null
  return (
    <div className="flex flex-wrap gap-2">
      {checkInLocation && (
        <span className="inline-flex items-center gap-1 text-[11px] bg-blue-50 border border-blue-100 text-blue-600 rounded-full px-2.5 py-1">
          <Navigation size={10} />
          출근 위치 {checkInLocation.lat.toFixed(4)}°N {checkInLocation.lng.toFixed(4)}°E
        </span>
      )}
      {checkOutLocation && (
        <span className="inline-flex items-center gap-1 text-[11px] bg-green-50 border border-green-100 text-green-600 rounded-full px-2.5 py-1">
          <Navigation size={10} />
          퇴근 위치 {checkOutLocation.lat.toFixed(4)}°N {checkOutLocation.lng.toFixed(4)}°E
        </span>
      )}
    </div>
  )
}

// ── 수정 이력 ──────────────────────────────────────────────
function EditHistory({ history }) {
  const [open, setOpen] = useState(false)
  if (!history?.length) return null
  return (
    <div className="pt-2 border-t border-offwhite-100">
      <button
        onClick={() => setOpen(v => !v)}
        className="flex items-center gap-1.5 text-xs text-gray-400 hover:text-navy transition-colors"
      >
        <History size={11} />
        수정 이력 {history.length}건
        <ChevronRight size={11} className={`transition-transform ${open ? 'rotate-90' : ''}`} />
      </button>
      {open && (
        <div className="mt-2 space-y-1.5">
          {history.map((h, i) => (
            <div key={i} className="text-[11px] bg-gray-50 rounded-lg px-3 py-2 text-gray-500">
              <span className="text-gray-400">
                {new Date(h.editedAt).toLocaleString('ko-KR', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false })}
              </span>
              <div className="mt-0.5">
                출근 <span className="line-through text-gray-400">{h.from.checkIn ?? '—'}</span>
                {' → '}
                <span className="font-semibold text-navy">{h.to.checkIn ?? '—'}</span>
                {h.from.checkOut !== h.to.checkOut && (
                  <> &nbsp; 퇴근 <span className="line-through text-gray-400">{h.from.checkOut ?? '—'}</span>
                  {' → '}
                  <span className="font-semibold text-navy">{h.to.checkOut ?? '—'}</span></>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

// ── 출퇴근 시간 수정 모달 ───────────────────────────────────
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
            <input type="time" value={checkIn} onChange={e => setCheckIn(e.target.value)}
              className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy outline-none focus:border-navy" />
          </div>
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">퇴근 시간</label>
            <input type="time" value={checkOut} onChange={e => setCheckOut(e.target.value)}
              className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy outline-none focus:border-navy" />
          </div>
        </div>
        <div className="flex gap-2 pt-1">
          <button onClick={onClose}
            className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:bg-offwhite transition-colors">
            취소
          </button>
          <button onClick={() => { onSave({ checkIn, checkOut }); onClose() }} disabled={!checkIn}
            className="flex-1 py-2.5 rounded-xl bg-navy text-white text-sm font-bold hover:bg-navy/80 transition-colors disabled:opacity-40">
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
            <div><p className="text-[10px] text-gray-400">출근</p><p className="font-bold text-navy tabular-nums">{record?.checkIn ?? '—'}</p></div>
            <div><p className="text-[10px] text-gray-400">퇴근</p><p className="font-bold text-navy tabular-nums">{record?.checkOut ?? '—'}</p></div>
            {worked && <div><p className="text-[10px] text-gray-400">근무 시간</p><p className="font-bold text-green-600">{worked}</p></div>}
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

// ── 이의신청 모달 ────────────────────────────────────────────
function DisputeModal({ shift, onSubmit, onClose }) {
  const [note, setNote] = useState('')
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4" onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 space-y-4" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-navy">근무 이의신청</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600"><X size={16} /></button>
        </div>
        <div className="bg-offwhite rounded-xl p-3">
          <p className="text-sm font-bold text-navy">{shift.jobTitle}</p>
          <p className="text-xs text-gray-500 mt-0.5">{shift.company} · {shift.shiftDate}</p>
        </div>
        <div className="flex gap-2 items-start bg-blue-50 border border-blue-100 rounded-xl p-3">
          <AlertTriangle size={14} className="text-blue-400 shrink-0 mt-0.5" />
          <p className="text-xs text-blue-700 leading-relaxed">
            실제로 근무했으나 기록이 누락된 경우 신청하세요.<br />
            허위 신청 시 불이익이 발생할 수 있습니다.
          </p>
        </div>
        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
            사유 <span className="text-red-400">*</span>
          </label>
          <textarea rows={3}
            placeholder="예: 앱 오류로 출퇴근 기록이 되지 않았습니다. 실제 10:00~17:00 근무하였습니다."
            value={note} onChange={e => setNote(e.target.value)}
            className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy outline-none focus:border-navy resize-none" />
        </div>
        <div className="flex gap-2">
          <button onClick={onClose}
            className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:bg-offwhite transition-colors">
            취소
          </button>
          <button onClick={() => { if (note.trim()) { onSubmit(note.trim()); onClose() } }} disabled={!note.trim()}
            className="flex-1 py-2.5 rounded-xl bg-navy text-white text-sm font-bold hover:bg-navy/80 transition-colors disabled:opacity-40 flex items-center justify-center gap-1.5">
            <Send size={13} />신청하기
          </button>
        </div>
      </div>
    </div>
  )
}

// ── 출/퇴근 인증 플로우 모달 (사진 + 지각사유 + 현재위치 + 기업 전달) ──
function AttendanceFlowModal({ type, shift, now, onRecord, onClose }) {
  const [photo, setPhoto]           = useState(null)
  const [lateReason, setLateReason] = useState('')
  const [step, setStep]             = useState('upload')
  const [showCamera, setShowCamera] = useState(false)

  // 위치 상태 (MapView.jsx 패턴 동일)
  const [userPos,    setUserPos]    = useState(null)   // { lat, lng, accuracy }
  const [locLoading, setLocLoading] = useState(false)
  const [locError,   setLocError]   = useState(false)

  const lateMinutes = type === 'in' ? getLateMinutes(shift.scheduledStart, now) : 0
  const isLate      = lateMinutes > 5

  function handleLocate() {
    if (!navigator.geolocation) { setLocError(true); return }
    setLocLoading(true); setLocError(false)
    navigator.geolocation.getCurrentPosition(
      pos => {
        setUserPos({ lat: pos.coords.latitude, lng: pos.coords.longitude, accuracy: Math.round(pos.coords.accuracy) })
        setLocLoading(false)
      },
      () => { setLocError(true); setLocLoading(false) },
      { timeout: 8000 }
    )
  }

  function handleRecord() {
    onRecord({ photo: photo?.url ?? null, lateReason: isLate ? lateReason : '', location: userPos })
    setStep('sending')
    setTimeout(() => setStep('done'), 1800)
  }

  const canSubmit = photo !== null && (!isLate || lateReason.trim().length > 0)

  // ── 전달 중 / 완료 화면 ──
  if (step === 'sending' || step === 'done') {
    const isDone = step === 'done'
    return (
      <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/50 p-4">
        <div className="bg-white rounded-2xl w-full max-w-sm p-7 shadow-xl text-center space-y-5">
          {/* 아이콘 */}
          <div className={`w-16 h-16 rounded-full flex items-center justify-center mx-auto transition-colors duration-500 ${isDone ? 'bg-green-50' : 'bg-blue-50'}`}>
            {isDone
              ? <CheckCircle2 size={30} className="text-green-500" />
              : <div className="w-9 h-9 border-4 border-blue-100 border-t-blue-500 rounded-full animate-spin" />
            }
          </div>

          {/* 텍스트 */}
          <div>
            <p className="font-bold text-navy text-lg">
              {isDone
                ? (type === 'in' ? '출근 기록 완료' : '퇴근 기록 완료')
                : (type === 'in' ? '출근 기록 전달 중...' : '퇴근 기록 전달 중...')}
            </p>
            <p className="text-sm text-gray-500 mt-1">{shift.company}</p>
            {isDone && (
              <p className="text-sm text-green-600 font-medium mt-2">담당자에게 전달됐습니다</p>
            )}
          </div>

          {/* 데모 안내 + 닫기 */}
          {isDone && (
            <>
              <div className="flex items-start gap-2.5 bg-blue-50 border border-blue-100 rounded-xl px-4 py-3 text-left">
                <Wifi size={14} className="text-blue-400 shrink-0 mt-0.5" />
                <p className="text-xs text-blue-600 leading-relaxed">
                  <span className="font-semibold">[데모]</span> 실제 서비스에서는 담당자에게<br />
                  푸시 알림으로 실시간 전달됩니다.
                </p>
              </div>
              <button
                onClick={onClose}
                className="w-full py-3 rounded-xl bg-navy text-white font-bold text-sm hover:bg-navy/80 transition-colors"
              >
                확인
              </button>
            </>
          )}
        </div>
      </div>
    )
  }

  // ── 업로드 단계 ──
  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/50 p-4"
      onClick={onClose}>
      <div className="bg-white rounded-2xl w-full max-w-sm shadow-xl overflow-hidden"
        onClick={e => e.stopPropagation()}>

        {/* 헤더 */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
          <div>
            <h2 className="font-bold text-navy">
              {type === 'in' ? '출근 인증' : '퇴근 인증'}
            </h2>
            <p className="text-xs text-gray-500 mt-0.5">{shift.jobTitle} · {shift.company}</p>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-lg text-gray-400 hover:bg-offwhite transition-colors">
            <X size={16} />
          </button>
        </div>

        <div className="p-5 space-y-4">

          {/* 지각 경고 */}
          {isLate && (
            <div className="flex items-center gap-2.5 bg-amber-50 border border-amber-200 rounded-xl px-4 py-3">
              <AlertTriangle size={16} className="text-amber-500 shrink-0" />
              <div>
                <p className="text-sm font-bold text-amber-700">지각 {lateMinutes}분</p>
                <p className="text-xs text-amber-600">예정 출근 {shift.scheduledStart} 기준</p>
              </div>
            </div>
          )}

          {/* 인증 사진 */}
          {showCamera && (
            <CameraCapture
              onCapture={(url) => { setPhoto({ url }); setShowCamera(false) }}
              onClose={() => setShowCamera(false)}
            />
          )}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-gray-500 uppercase tracking-wide">인증 사진</span>
              <span className="text-[11px] text-orange bg-orange/10 border border-orange/20 px-2 py-0.5 rounded-full font-semibold">필수</span>
            </div>

            {photo ? (
              /* 사진 미리보기 */
              <div className="relative rounded-xl overflow-hidden bg-gray-100" style={{ aspectRatio: '4/3' }}>
                <img src={photo.url} alt="인증 사진" className="w-full h-full object-cover" />
                <div className="absolute inset-0 flex flex-col justify-between p-2.5">
                  <button
                    onClick={() => setPhoto(null)}
                    className="self-end w-7 h-7 bg-black/50 rounded-full flex items-center justify-center text-white"
                  >
                    <X size={13} />
                  </button>
                  <button
                    onClick={() => setShowCamera(true)}
                    className="self-center bg-black/50 text-white text-xs px-3 py-1.5 rounded-full flex items-center gap-1.5"
                  >
                    <Camera size={12} />다시 찍기
                  </button>
                </div>
              </div>
            ) : (
              /* 카메라 촬영 버튼 */
              <button
                onClick={() => setShowCamera(true)}
                className="w-full border-2 border-dashed border-offwhite-200 rounded-xl py-8 flex flex-col items-center gap-2 text-gray-400 hover:border-orange/40 hover:text-orange/70 active:bg-orange/5 transition-colors"
              >
                <Camera size={28} />
                <p className="text-sm font-medium">카메라로 촬영하기</p>
                <p className="text-xs">갤러리 업로드 불가 · 현장에서 직접 찍은 사진만 가능</p>
              </button>
            )}
          </div>

          {/* 현재 위치 */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-gray-500 uppercase tracking-wide">현재 위치</span>
              <span className="text-[11px] text-gray-400 bg-gray-50 border border-offwhite-200 px-2 py-0.5 rounded-full">선택</span>
            </div>

            {userPos ? (
              <div className="space-y-2">
                {/* 소형 Leaflet 지도 — 내 위치(파랑) + 근무지(주황) */}
                <div className="rounded-xl overflow-hidden border border-offwhite-200" style={{ height: 160 }}>
                  <MapContainer
                    center={[userPos.lat, userPos.lng]}
                    zoom={16}
                    style={{ width: '100%', height: '100%' }}
                    zoomControl={false}
                    attributionControl={false}
                  >
                    <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                    <Marker position={[userPos.lat, userPos.lng]} icon={userLocationIcon} />
                    {shift.shiftLat && shift.shiftLng && (
                      <Marker position={[shift.shiftLat, shift.shiftLng]} icon={worksiteIcon} />
                    )}
                    <FitMapBounds
                      userPos={userPos}
                      shiftPos={shift.shiftLat ? { lat: shift.shiftLat, lng: shift.shiftLng } : null}
                    />
                  </MapContainer>
                </div>

                {/* 거리 배지 */}
                {shift.shiftLat && shift.shiftLng && (
                  <div className="flex items-center gap-2 flex-wrap">
                    <DistanceBadge distanceM={haversineDistance(userPos.lat, userPos.lng, shift.shiftLat, shift.shiftLng)} />
                    <span className="text-[10px] text-gray-400">
                      <span className="inline-block w-3 h-3 rounded-sm bg-orange align-middle mr-0.5" style={{verticalAlign:'middle'}} />근무지
                      <span className="ml-2 inline-block w-3 h-3 rounded-full bg-blue-500 align-middle mr-0.5" style={{verticalAlign:'middle'}} />내 위치
                    </span>
                  </div>
                )}

                <div className="flex items-center justify-between">
                  <p className="text-[11px] text-blue-600 flex items-center gap-1">
                    <Navigation size={11} />
                    {userPos.lat.toFixed(5)}°N, {userPos.lng.toFixed(5)}°E
                    {userPos.accuracy && <span className="text-gray-400 ml-1">(±{userPos.accuracy}m)</span>}
                  </p>
                  <button
                    onClick={handleLocate}
                    className="text-[11px] text-gray-400 hover:text-navy underline underline-offset-2"
                  >
                    다시 확인
                  </button>
                </div>
              </div>
            ) : (
              <button
                onClick={handleLocate}
                disabled={locLoading}
                className="w-full flex items-center justify-center gap-2 py-3 rounded-xl border border-dashed border-offwhite-200 text-sm text-gray-400 hover:border-navy/30 hover:text-navy/60 disabled:opacity-60 transition-colors"
              >
                <Navigation size={16} className={locLoading ? 'animate-spin' : ''} />
                {locLoading ? '위치 확인 중...' : '현재 위치 확인하기'}
              </button>
            )}
            {locError && (
              <p className="text-[11px] text-red-500 flex items-center gap-1">
                <AlertTriangle size={11} />위치 권한을 허용해주세요
              </p>
            )}
          </div>

          {/* 지각 사유 (지각 시 필수) */}
          {isLate && (
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">
                지각 사유 <span className="text-red-400">*</span>
              </label>
              <textarea
                rows={3}
                placeholder="예: 지하철 지연으로 인해 늦었습니다."
                value={lateReason}
                onChange={e => setLateReason(e.target.value)}
                className="w-full border border-offwhite-200 rounded-xl px-3 py-2.5 text-sm text-navy outline-none focus:border-orange resize-none transition-colors"
              />
              <p className="text-[11px] text-gray-400">사유는 기업 담당자에게 함께 전달됩니다.</p>
            </div>
          )}

          {/* 버튼 */}
          <div className="flex gap-2 pt-1">
            <button
              onClick={onClose}
              className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:bg-offwhite transition-colors"
            >
              취소
            </button>
            <button
              onClick={handleRecord}
              disabled={!canSubmit}
              className={`flex-1 py-2.5 rounded-xl text-white text-sm font-bold transition-colors disabled:opacity-40 flex items-center justify-center gap-2 ${
                type === 'in' ? 'bg-orange hover:bg-orange-600' : 'bg-green-500 hover:bg-green-600'
              }`}
            >
              {type === 'in'
                ? <><LogIn size={14} />출근 기록하기</>
                : <><LogOut size={14} />퇴근 기록하기</>
              }
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

// ── 메인 페이지 ─────────────────────────────────────────────
export default function IndividualAttendancePage() {
  const { user } = useAuth()
  const { checkIn, checkOut, editRecord, deleteRecord, submitRecord, submitDispute, getRecord } = useAttendance()
  const [shifts, setShifts] = useState([])
  const today = todayStr()

  useEffect(() => {
    if (!user) return
    applicationApi.myList()
      .then(res => res.ok ? res.json() : [])
      .then(data => {
        const list = Array.isArray(data) ? data : []
        setShifts(
          list
            .filter(app => app.status === 'APPROVED')
            .map(app => ({
              shiftId:        String(app.id),
              applicationId:  app.id,
              jobTitle:       app.jobPost?.title ?? '—',
              company:        app.jobPost?.companyName ?? '—',
              location:       app.jobPost?.workLocation ?? '—',
              wage:           app.jobPost?.wageAmount
                                ? `${WAGE_LABEL[app.jobPost.wageType] || '시급'} ${Number(app.jobPost.wageAmount).toLocaleString()}원`
                                : '—',
              shiftDate:      app.jobPost?.deadline?.substring(0, 10) ?? today,
              scheduledStart: '09:00',
              scheduledEnd:   '18:00',
              shiftLat:       null,
              shiftLng:       null,
            }))
        )
      })
      .catch(() => {})
  }, [user])

  const [editTarget,    setEditTarget]    = useState(null)
  const [submitTarget,  setSubmitTarget]  = useState(null)
  const [deleteTarget,  setDeleteTarget]  = useState(null)
  const [disputeTarget, setDisputeTarget] = useState(null)
  const [disputes,      setDisputes]      = useState(() => loadDisputes())
  const [now,           setNow]           = useState(new Date())

  // 출/퇴근 인증 플로우
  // 출/퇴근 인증 플로우
  const [flowTarget,      setFlowTarget]      = useState(null)
  const [photoMap,        setPhotoMap]        = useState({})
  const [lateReasonMap,   setLateReasonMap]   = useState({})
  // 지각 시간수정 요청 (localStorage와 동기화)
  const [lateRequests,    setLateRequestsState] = useState(() => loadLateRequests())

  function getDispute(shiftId) { return disputes.find(d => d.shiftId === shiftId) ?? null }

  function handleDisputeSubmit(shift, note) {
    submitDispute(shift, { note })
    setDisputes(loadDisputes())
  }

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 1000)
    return () => clearInterval(timer)
  }, [])

  // 인증 완료 후 실제 기록
  function handleAttendanceRecord({ photo, lateReason, location }) {
    const { type, shift } = flowTarget
    if (photo)      setPhotoMap(prev      => ({ ...prev, [`${shift.shiftId}_${type}`]: photo }))
    if (lateReason) setLateReasonMap(prev => ({ ...prev, [shift.shiftId]: lateReason }))
    if (type === 'in') {
      checkIn(shift.shiftId, location ?? null)
      workAttendanceApi.checkIn(shift.shiftId).catch(() => {})
      if (lateReason) {
        const nowDate = new Date()
        const nowStr  = `${String(nowDate.getHours()).padStart(2, '0')}:${String(nowDate.getMinutes()).padStart(2, '0')}`
        const existing = loadLateRequests()
        const newReq = {
          id:             `late-${Date.now()}`,
          shiftId:        shift.shiftId,
          userName:       user?.name ?? '—',
          userEmail:      user?.email ?? '',
          jobTitle:       shift.jobTitle,
          company:        shift.company,
          shiftDate:      shift.shiftDate,
          scheduledStart: shift.scheduledStart,
          actualCheckIn:  nowStr,
          lateMinutes:    getLateMinutes(shift.scheduledStart, nowDate),
          reason:         lateReason,
          status:         'pending',
          submittedAt:    nowDate.toISOString(),
          reviewedAt:     null,
        }
        saveLateRequests([...existing.filter(r => r.shiftId !== shift.shiftId), newReq])
        setLateRequestsState(loadLateRequests())
      }
    } else {
      checkOut(shift.shiftId, location ?? null)
      workAttendanceApi.checkOut(shift.shiftId).catch(() => {})
    }
  }

  const todayShifts    = shifts.filter(s => s.shiftDate === today)
  const upcomingShifts = shifts.filter(s => s.shiftDate > today).sort((a, b) => a.shiftDate.localeCompare(b.shiftDate))
  const pastShifts     = shifts.filter(s => s.shiftDate < today).sort((a, b) => b.shiftDate.localeCompare(a.shiftDate))

  return (
    <div className="space-y-6">

      {/* ── 모달들 ── */}
      {editTarget && (
        <EditModal record={editTarget.record}
          onSave={(times) => editRecord(editTarget.shiftId, times)}
          onClose={() => setEditTarget(null)} />
      )}
      {disputeTarget && (
        <DisputeModal shift={disputeTarget}
          onSubmit={(note) => handleDisputeSubmit(disputeTarget, note)}
          onClose={() => setDisputeTarget(null)} />
      )}
      {submitTarget && (
        <SubmitModal shift={submitTarget.shift} record={submitTarget.record}
          onConfirm={() => { submitRecord(submitTarget.shift.shiftId); setSubmitTarget(null) }}
          onClose={() => setSubmitTarget(null)} />
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

      {/* 새 출/퇴근 인증 플로우 모달 */}
      {flowTarget && (
        <AttendanceFlowModal
          type={flowTarget.type}
          shift={flowTarget.shift}
          now={now}
          onRecord={handleAttendanceRecord}
          onClose={() => setFlowTarget(null)}
        />
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
            const rec        = getRecord(shift.shiftId)
            const status     = rec?.status ?? 'scheduled'
            const meta       = STATUS[status] ?? STATUS.scheduled
            const canCheckIn  = status === 'scheduled'
            const canCheckOut = status === 'in_progress'
            const isSubmitted = status === 'submitted'
            const { canEdit: canEditWindow, remaining } = getEditWindowInfo(rec, now)
            const lateReq     = lateRequests.find(r => r.shiftId === shift.shiftId) ?? null
            const lateApproved = lateReq?.status === 'approved'
            const canEdit     = canEditWindow || (lateApproved && status === 'completed' && !isSubmitted)
            const editExpired = status === 'completed' && !canEdit
            const worked      = calcDuration(rec?.checkIn, rec?.checkOut)
            const photoIn     = photoMap[`${shift.shiftId}_in`]
            const photoOut    = photoMap[`${shift.shiftId}_out`]
            const lateReason  = lateReasonMap[shift.shiftId]

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

                    <LocationBadge
                      checkInLocation={rec?.checkInLocation}
                      checkOutLocation={rec?.checkOutLocation}
                    />

                    {/* 인증 사진 썸네일 */}
                    {(photoIn || photoOut) && (
                      <div className="flex gap-3">
                        {photoIn && (
                          <div className="space-y-1">
                            <p className="text-[10px] text-gray-400 flex items-center gap-1"><ImageIcon size={10} />출근 인증</p>
                            <img src={photoIn} alt="출근 인증" className="w-16 h-16 rounded-xl object-cover border border-offwhite-200" />
                          </div>
                        )}
                        {photoOut && (
                          <div className="space-y-1">
                            <p className="text-[10px] text-gray-400 flex items-center gap-1"><ImageIcon size={10} />퇴근 인증</p>
                            <img src={photoOut} alt="퇴근 인증" className="w-16 h-16 rounded-xl object-cover border border-offwhite-200" />
                          </div>
                        )}
                      </div>
                    )}

                    {/* 지각 사유 */}
                    {lateReason && (
                      <div className="flex items-start gap-2 bg-amber-50 border border-amber-100 rounded-xl px-3 py-2.5">
                        <AlertTriangle size={12} className="text-amber-500 shrink-0 mt-0.5" />
                        <div>
                          <p className="text-[10px] font-semibold text-amber-600 mb-0.5">지각 사유</p>
                          <p className="text-xs text-amber-700">{lateReason}</p>
                        </div>
                      </div>
                    )}

                    {/* 지각 시간수정 요청 상태 */}
                    {lateReq && (
                      <div className={`flex items-center gap-2 rounded-xl px-3 py-2 text-xs font-semibold ${
                        lateReq.status === 'pending'  ? 'bg-amber-50 border border-amber-100 text-amber-600' :
                        lateReq.status === 'approved' ? 'bg-green-50 border border-green-100 text-green-600' :
                                                        'bg-red-50 border border-red-100 text-red-500'
                      }`}>
                        <Clock size={11} className="shrink-0" />
                        {lateReq.status === 'pending'  ? '기업 검토 중 — 승인 시 시간 수정 가능' :
                         lateReq.status === 'approved' ? '승인됨 — 시간 수정이 허용되었습니다' :
                                                         '거절됨 — 시간 수정이 허용되지 않았습니다'}
                      </div>
                    )}

                    {/* 수정/삭제 버튼 */}
                    {canEdit && (
                      <div className="space-y-2">
                        {canEditWindow && (
                          <p className="text-[11px] text-orange font-semibold flex items-center gap-1">
                            <Clock size={11} />수정 가능 시간 {remaining}분 남음
                          </p>
                        )}
                        <div className="flex gap-2">
                          <button onClick={() => setEditTarget({ shiftId: shift.shiftId, record: rec })}
                            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-offwhite-200 text-xs font-semibold text-gray-500 hover:bg-offwhite hover:text-navy transition-colors">
                            <Pencil size={12} />수정
                          </button>
                          <button onClick={() => setDeleteTarget(shift.shiftId)}
                            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-offwhite-200 text-xs font-semibold text-gray-500 hover:bg-red-50 hover:text-red-500 hover:border-red-200 transition-colors">
                            <Trash2 size={12} />삭제
                          </button>
                        </div>
                      </div>
                    )}
                    {editExpired && (
                      <p className="text-[11px] text-gray-400 flex items-center gap-1">
                        <AlertTriangle size={11} className="text-gray-300" />
                        수정 가능 시간(30분)이 지났습니다. 제출만 가능합니다.
                      </p>
                    )}
                    <EditHistory history={rec?.editHistory} />
                  </div>
                )}

                {/* 출근하기 / 퇴근하기 버튼 */}
                {(canCheckIn || canCheckOut) && (
                  <button
                    onClick={() => setFlowTarget({ type: canCheckIn ? 'in' : 'out', shift })}
                    className={`w-full py-3 rounded-xl font-bold text-sm flex items-center justify-center gap-2 transition-colors ${
                      canCheckIn ? 'bg-orange text-white hover:bg-orange-600' : 'bg-green-500 text-white hover:bg-green-600'
                    }`}
                  >
                    {canCheckIn ? <><LogIn size={16} />출근하기</> : <><LogOut size={16} />퇴근하기</>}
                  </button>
                )}

                {/* 제출하기 버튼 */}
                {canEdit && rec?.checkOut && (
                  <button onClick={() => setSubmitTarget({ shift, record: rec })}
                    className="w-full py-3 rounded-xl bg-navy text-white font-bold text-sm flex items-center justify-center gap-2 hover:bg-navy/80 transition-colors">
                    <Send size={15} />출퇴근 기록 제출하기
                  </button>
                )}

                {/* 제출 완료 */}
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
              const rec         = getRecord(shift.shiftId)
              const dispute     = getDispute(shift.shiftId)
              const isSubmitted = rec?.status === 'submitted'
              const isCompleted = rec?.status === 'completed' || isSubmitted
              const isUnrecorded = !rec && !dispute
              const worked      = calcDuration(rec?.checkIn, rec?.checkOut)
              const photoIn     = photoMap[`${shift.shiftId}_in`]
              const photoOut    = photoMap[`${shift.shiftId}_out`]
              const lateReason  = lateReasonMap[shift.shiftId]
              const pastLateReq = lateRequests.find(r => r.shiftId === shift.shiftId) ?? null

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
                        isSubmitted                       ? 'bg-navy/10 text-navy' :
                        isCompleted                       ? 'bg-green-100 text-green-600' :
                        dispute?.status === 'pending'     ? 'bg-amber-100 text-amber-600' :
                        dispute?.status === 'approved'    ? 'bg-green-100 text-green-600' :
                        dispute?.status === 'rejected'    ? 'bg-red-100 text-red-500' :
                        'bg-gray-100 text-gray-400'
                      }`}>
                        {isSubmitted                    ? '제출됨' :
                         isCompleted                    ? '완료' :
                         dispute?.status === 'pending'  ? '검토 중' :
                         dispute?.status === 'approved' ? '이의신청 승인' :
                         dispute?.status === 'rejected' ? '이의신청 거절' :
                         '미기록'}
                      </span>
                    </div>
                  </div>

                  {/* 미기록 → 이의신청 */}
                  {isUnrecorded && (
                    <div className="mt-3 pt-3 border-t border-offwhite-100 flex items-center justify-between gap-3">
                      <p className="text-xs text-gray-400">출퇴근 기록이 없습니다.</p>
                      <button onClick={() => setDisputeTarget(shift)}
                        className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-amber-200 bg-amber-50 text-xs font-semibold text-amber-600 hover:bg-amber-100 transition-colors shrink-0">
                        <AlertTriangle size={12} />이의신청
                      </button>
                    </div>
                  )}

                  {/* 이의신청 상태 */}
                  {dispute && (
                    <div className={`mt-3 pt-3 border-t border-offwhite-100 rounded-xl p-3 text-xs space-y-1 ${
                      dispute.status === 'pending'  ? 'bg-amber-50 border border-amber-100' :
                      dispute.status === 'approved' ? 'bg-green-50 border border-green-100' :
                                                      'bg-red-50 border border-red-100'
                    }`}>
                      <p className={`font-semibold ${
                        dispute.status === 'pending'  ? 'text-amber-600' :
                        dispute.status === 'approved' ? 'text-green-600' : 'text-red-500'
                      }`}>
                        {dispute.status === 'pending'  ? '이의신청 검토 중 (48시간 이내 처리)' :
                         dispute.status === 'approved' ? '이의신청 승인됨' :
                                                         '이의신청 거절됨'}
                      </p>
                      <p className="text-gray-500">사유: {dispute.note}</p>
                    </div>
                  )}

                  {rec && (
                    <div className="mt-3 pt-3 border-t border-offwhite-100 space-y-2">
                      <div className="flex gap-6 text-sm">
                        <div><p className="text-xs text-gray-400 mb-0.5">출근</p><p className="font-semibold text-navy tabular-nums">{rec.checkIn ?? '—'}</p></div>
                        <div><p className="text-xs text-gray-400 mb-0.5">퇴근</p><p className="font-semibold text-navy tabular-nums">{rec.checkOut ?? '—'}</p></div>
                        {worked && <div><p className="text-xs text-gray-400 mb-0.5">근무 시간</p><p className="font-semibold text-green-600">{worked}</p></div>}
                      </div>

                      <LocationBadge
                        checkInLocation={rec?.checkInLocation}
                        checkOutLocation={rec?.checkOutLocation}
                      />

                      {/* 인증 사진 */}
                      {(photoIn || photoOut) && (
                        <div className="flex gap-3">
                          {photoIn && (
                            <div className="space-y-1">
                              <p className="text-[10px] text-gray-400 flex items-center gap-1"><ImageIcon size={10} />출근 인증</p>
                              <img src={photoIn} alt="출근 인증" className="w-16 h-16 rounded-xl object-cover border border-offwhite-200" />
                            </div>
                          )}
                          {photoOut && (
                            <div className="space-y-1">
                              <p className="text-[10px] text-gray-400 flex items-center gap-1"><ImageIcon size={10} />퇴근 인증</p>
                              <img src={photoOut} alt="퇴근 인증" className="w-16 h-16 rounded-xl object-cover border border-offwhite-200" />
                            </div>
                          )}
                        </div>
                      )}

                      {/* 지각 사유 */}
                      {lateReason && (
                        <div className="flex items-start gap-2 bg-amber-50 border border-amber-100 rounded-xl px-3 py-2.5">
                          <AlertTriangle size={12} className="text-amber-500 shrink-0 mt-0.5" />
                          <div>
                            <p className="text-[10px] font-semibold text-amber-600 mb-0.5">지각 사유</p>
                            <p className="text-xs text-amber-700">{lateReason}</p>
                          </div>
                        </div>
                      )}

                      {/* 지각 시간수정 요청 상태 */}
                      {pastLateReq && (
                        <div className={`flex items-center gap-2 rounded-xl px-3 py-2 text-xs font-semibold ${
                          pastLateReq.status === 'pending'  ? 'bg-amber-50 border border-amber-100 text-amber-600' :
                          pastLateReq.status === 'approved' ? 'bg-green-50 border border-green-100 text-green-600' :
                                                              'bg-red-50 border border-red-100 text-red-500'
                        }`}>
                          <Clock size={11} className="shrink-0" />
                          {pastLateReq.status === 'pending'  ? '기업 검토 중 — 승인 시 시간 수정 가능' :
                           pastLateReq.status === 'approved' ? '승인됨 — 시간 수정이 허용되었습니다' :
                                                               '거절됨 — 시간 수정이 허용되지 않았습니다'}
                        </div>
                      )}

                      {rec.status === 'completed' && (() => {
                        const { canEdit: pastCanEditWindow, remaining: pastRemaining } = getEditWindowInfo(rec, now)
                        const pastCanEdit = pastCanEditWindow || (pastLateReq?.status === 'approved')
                        const pastExpired = !pastCanEdit
                        return (
                          <div className="space-y-2">
                            {pastCanEditWindow && (
                              <p className="text-[11px] text-orange font-semibold flex items-center gap-1">
                                <Clock size={11} />수정 가능 시간 {pastRemaining}분 남음
                              </p>
                            )}
                            {pastExpired && (
                              <p className="text-[11px] text-gray-400 flex items-center gap-1">
                                <AlertTriangle size={11} className="text-gray-300" />
                                수정 가능 시간(30분)이 지났습니다. 제출만 가능합니다.
                              </p>
                            )}
                            <div className="flex gap-2 flex-wrap">
                              {pastCanEdit && (
                                <>
                                  <button onClick={() => setEditTarget({ shiftId: shift.shiftId, record: rec })}
                                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-offwhite-200 text-xs font-semibold text-gray-500 hover:bg-offwhite hover:text-navy transition-colors">
                                    <Pencil size={12} />수정
                                  </button>
                                  <button onClick={() => setDeleteTarget(shift.shiftId)}
                                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-offwhite-200 text-xs font-semibold text-gray-500 hover:bg-red-50 hover:text-red-500 hover:border-red-200 transition-colors">
                                    <Trash2 size={12} />삭제
                                  </button>
                                </>
                              )}
                              {rec.checkOut && (
                                <button onClick={() => setSubmitTarget({ shift, record: rec })}
                                  className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-navy text-white text-xs font-bold hover:bg-navy/80 transition-colors">
                                  <Send size={12} />제출하기
                                </button>
                              )}
                            </div>
                          </div>
                        )
                      })()}
                      <EditHistory history={rec?.editHistory} />
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        </section>
      )}
    </div>
  )
}
