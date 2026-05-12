import { useState, useMemo, useEffect, useRef } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import {
  Star, ChevronLeft, ChevronRight, X,
  Check, UserX, MapPin, Clock, Calendar,
  UserCheck, ArrowUpDown, SlidersHorizontal,
  RotateCcw, Save, CheckSquare, AlertTriangle,
} from 'lucide-react'
import Card from '../../components/ui/Card'
import StatusBadge from '../../components/ui/StatusBadge'
import EmptyState from '../../components/ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'
import { MOCK_APPLICANTS } from '../../data/mockApplicants'

// ── 별점 표시 ────────────────────────────────────────────
function StarRating({ rating, size = 11 }) {
  if (!rating) return <span className="text-xs text-gray-400 italic font-medium">신규</span>
  return (
    <span className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map(i => (
        <Star
          key={i} size={size}
          className={rating >= i ? 'text-yellow-400' : 'text-gray-200'}
          fill={rating >= i ? 'currentColor' : 'none'}
        />
      ))}
      <span className="text-xs font-bold text-gray-700 ml-1">{rating.toFixed(1)}</span>
    </span>
  )
}

// ── 아바타 ───────────────────────────────────────────────
const AVATAR_COLORS = [
  'bg-navy/10 text-navy', 'bg-orange/10 text-orange',
  'bg-green-100 text-green-700', 'bg-purple-100 text-purple-700',
  'bg-blue-100 text-blue-700', 'bg-pink-100 text-pink-700',
]
function Avatar({ name, index, size = 'md' }) {
  const cls = AVATAR_COLORS[index % AVATAR_COLORS.length]
  const dim = size === 'lg' ? 'w-14 h-14 text-xl' : 'w-9 h-9 text-sm'
  return (
    <div className={`${dim} ${cls} rounded-full flex items-center justify-center font-bold shrink-0`}>
      {name[0]}
    </div>
  )
}

// ── 상태 칩 ──────────────────────────────────────────────
const STATUS_CFG = {
  pending:  { label: '미결정',    cls: 'text-gray-500 bg-gray-50 border-gray-200' },
  hired:    { label: '채용 확정', cls: 'text-green-600 bg-green-50 border-green-200' },
  rejected: { label: '거절',      cls: 'text-red-500 bg-red-50 border-red-200' },
}
function StatusChip({ status }) {
  const cfg = STATUS_CFG[status]
  return (
    <span className={`inline-flex text-xs font-semibold px-2.5 py-1 rounded-full border ${cfg.cls}`}>
      {cfg.label}
    </span>
  )
}

// ── 확인 모달 ─────────────────────────────────────────────
function ConfirmModal({ type, hiredCount, requiredStaff, onConfirm, onCancel }) {
  const isReset = type === 'reset'
  const isOver  = !isReset && hiredCount > requiredStaff
  const isUnder = !isReset && hiredCount < requiredStaff

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/40" onClick={onCancel} />
      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 space-y-4">
        <div className="flex items-center gap-3">
          <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${isReset ? 'bg-red-50' : isUnder || isOver ? 'bg-orange-50' : 'bg-green-50'}`}>
            {isReset
              ? <RotateCcw size={18} className="text-red-500" />
              : isUnder || isOver
                ? <AlertTriangle size={18} className="text-orange" />
                : <CheckSquare size={18} className="text-green-600" />
            }
          </div>
          <h3 className="font-bold text-navy">{isReset ? '선택 초기화' : '채용 확정'}</h3>
        </div>

        {isReset && (
          <p className="text-sm text-gray-600">
            모든 채용·거절 결정과 별표 고정이 초기화되며 저장된 데이터도 삭제됩니다. 계속하시겠습니까?
          </p>
        )}
        {!isReset && isUnder && (
          <div className="flex items-start gap-2 p-3 bg-orange-50 rounded-xl text-sm text-orange">
            <AlertTriangle size={14} className="shrink-0 mt-0.5" />
            <span>현재 <strong>{hiredCount}명</strong> 확정 — 모집 정원({requiredStaff}명)보다 <strong>{requiredStaff - hiredCount}명 부족</strong>합니다. 그래도 확정하시겠습니까?</span>
          </div>
        )}
        {!isReset && isOver && (
          <div className="flex items-start gap-2 p-3 bg-orange-50 rounded-xl text-sm text-orange">
            <AlertTriangle size={14} className="shrink-0 mt-0.5" />
            <span>현재 <strong>{hiredCount}명</strong> 확정 — 모집 정원({requiredStaff}명)을 <strong>{hiredCount - requiredStaff}명 초과</strong>합니다. 계속하시겠습니까?</span>
          </div>
        )}
        {!isReset && !isUnder && !isOver && (
          <p className="text-sm text-gray-600">
            <strong>{hiredCount}명</strong>을 최종 채용 확정합니다. 확정 후 계약 현황 페이지로 이동합니다.
          </p>
        )}

        <div className="grid grid-cols-2 gap-2 pt-1">
          <button
            onClick={onCancel}
            className="py-2.5 rounded-xl border border-offwhite-200 text-gray-600 text-sm font-semibold hover:bg-offwhite transition-colors"
          >
            취소
          </button>
          <button
            onClick={onConfirm}
            className={`py-2.5 rounded-xl text-white text-sm font-bold transition-colors ${
              isReset ? 'bg-red-500 hover:bg-red-600' : 'bg-green-600 hover:bg-green-700'
            }`}
          >
            {isReset ? '초기화' : '확정'}
          </button>
        </div>
      </div>
    </div>
  )
}

// ── 사이드 드로워 ────────────────────────────────────────
function SideDrawer({ applicant, index, onClose, onPin, onHire, onReject }) {
  if (!applicant) return null
  const ratingLabel =
    !applicant.rating       ? '평가 없음' :
    applicant.rating >= 4.5 ? '우수' :
    applicant.rating >= 4.0 ? '양호' :
    applicant.rating >= 3.5 ? '보통' : '주의'

  const distanceNote =
    applicant.region.includes('강남') || applicant.region.includes('서초') || applicant.region.includes('송파')
      ? '근무지와 가까운 편 (30분 이내 예상)' :
    applicant.region.includes('경기')
      ? '다소 거리 있음 (1시간+ 예상)'
      : '이동 가능 거리 (약 40–60분 예상)'

  return (
    <div className="fixed inset-0 z-50 flex">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />
      <div className="absolute right-0 top-0 h-full w-full sm:w-[380px] bg-white shadow-2xl flex flex-col">
        <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
          <span className="text-sm font-bold text-navy">지원자 상세</span>
          <button onClick={onClose} className="p-1.5 rounded-lg text-gray-400 hover:bg-offwhite hover:text-navy transition-colors">
            <X size={18} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto p-5 space-y-4">
          <div className="flex items-center gap-4">
            <Avatar name={applicant.name} index={index} size="lg" />
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-lg font-bold text-navy">{applicant.name}</h2>
                {applicant.pinned && <Star size={15} className="text-yellow-400" fill="currentColor" />}
              </div>
              <p className="text-sm text-gray-500 mt-0.5">
                {applicant.age}세 · {applicant.gender} · {applicant.region}
              </p>
              <div className="mt-1.5"><StatusChip status={applicant.status} /></div>
            </div>
          </div>

          <div className="bg-offwhite rounded-xl p-4">
            <p className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">기본 정보</p>
            <div className="space-y-2">
              {[
                { label: '나이',     value: `${applicant.age}세` },
                { label: '성별',     value: applicant.gender === '여' ? '여성' : '남성' },
                { label: '거주 지역', value: applicant.region },
                { label: '가입일',   value: applicant.joinedAt.replace('-', '.') },
              ].map(r => (
                <div key={r.label} className="flex items-center justify-between text-sm">
                  <span className="text-gray-500">{r.label}</span>
                  <span className="font-semibold text-navy">{r.value}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-offwhite rounded-xl p-4">
            <p className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">플랫폼 이력</p>
            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="text-gray-500">평점</span>
                <div className="flex items-center gap-2">
                  <StarRating rating={applicant.rating} size={13} />
                  {applicant.rating && (
                    <span className={`text-xs font-bold px-1.5 py-0.5 rounded-md
                      ${applicant.rating >= 4.5 ? 'bg-green-100 text-green-700' :
                        applicant.rating >= 4.0 ? 'bg-blue-100 text-blue-700' :
                        applicant.rating >= 3.5 ? 'bg-yellow-100 text-yellow-700' :
                        'bg-red-100 text-red-600'}`}>
                      {ratingLabel}
                    </span>
                  )}
                </div>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-gray-500">총 고용 횟수</span>
                <span className="font-bold text-navy">
                  {applicant.hireCount > 0 ? `${applicant.hireCount}회` : '없음 (신규)'}
                </span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-gray-500">경력 등급</span>
                <span className={`font-bold
                  ${applicant.hireCount >= 15 ? 'text-orange' :
                    applicant.hireCount >= 5  ? 'text-blue-600' :
                    applicant.hireCount >= 1  ? 'text-gray-600' : 'text-gray-400'}`}>
                  {applicant.hireCount >= 15 ? '🏆 베테랑' :
                   applicant.hireCount >= 5  ? '⭐ 숙련' :
                   applicant.hireCount >= 1  ? '📋 초보' : '🆕 신규'}
                </span>
              </div>
            </div>
          </div>

          <div className="bg-blue-50 rounded-xl p-3.5 flex items-start gap-2.5">
            <MapPin size={14} className="text-blue-500 shrink-0 mt-0.5" />
            <div>
              <p className="text-xs font-semibold text-blue-700">거주지 참고</p>
              <p className="text-xs text-blue-600 mt-0.5">
                {applicant.region} 거주 — {distanceNote}
              </p>
            </div>
          </div>
        </div>

        <div className="p-4 border-t border-offwhite-200 space-y-2.5">
          <button
            onClick={onPin}
            className={`w-full flex items-center justify-center gap-2 py-2.5 rounded-xl border text-sm font-semibold transition-all
              ${applicant.pinned
                ? 'bg-yellow-50 border-yellow-300 text-yellow-600 hover:bg-yellow-100'
                : 'bg-white border-offwhite-200 text-gray-600 hover:border-yellow-300 hover:text-yellow-600'}`}
          >
            <Star size={14} fill={applicant.pinned ? 'currentColor' : 'none'} />
            {applicant.pinned ? '별표 고정 해제' : '별표 고정 (우선 검토)'}
          </button>
          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={onHire}
              disabled={applicant.status === 'hired'}
              className="flex items-center justify-center gap-1.5 py-2.5 rounded-xl bg-green-600 hover:bg-green-700 text-white text-sm font-bold transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <Check size={14} />채용 확정
            </button>
            <button
              onClick={onReject}
              disabled={applicant.status === 'rejected'}
              className="flex items-center justify-center gap-1.5 py-2.5 rounded-xl bg-red-500 hover:bg-red-600 text-white text-sm font-bold transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <UserX size={14} />거절
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

// ── 정렬·필터 옵션 ───────────────────────────────────────
const SORT_OPTIONS = [
  { key: 'default', label: '기본순' },
  { key: 'rating',  label: '⭐ 평점순' },
  { key: 'pinned',  label: '★ 고정순' },
]
const FILTER_OPTIONS = [
  { key: 'all',      label: '전체' },
  { key: 'pending',  label: '미결정' },
  { key: 'hired',    label: '채용확정' },
  { key: 'rejected', label: '거절' },
]

// ── 메인 컴포넌트 ────────────────────────────────────────
export default function ShiftDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { shifts, finalizeShift, addToast } = useAppData()
  const shift = shifts.find(s => s.id === id)

  const STORAGE_KEY = `staffnow_shift_${id}`

  const baseApplicants = useMemo(() => {
    if (!shift?.applicantIds?.length) return []
    return MOCK_APPLICANTS
      .filter(a => shift.applicantIds.includes(a.id))
      .map(a => ({ ...a, pinned: false, status: 'pending' }))
  }, [shift?.id])

  const [applicants, setApplicants] = useState(baseApplicants)
  const [sortBy,   setSortBy]   = useState('default')
  const [filterBy, setFilterBy] = useState('all')
  const [drawerApplicantId, setDrawerApplicantId] = useState(null)
  const [modal, setModal] = useState(null) // 'reset' | 'finalize' | null

  const initialized  = useRef(false)
  const isFirstSave  = useRef(true)

  // 마운트 시 이전 작업 복원 (우선순위: 임시저장 > 확정 데이터 > 초기값)
  useEffect(() => {
    if (initialized.current || !baseApplicants.length) return
    initialized.current = true

    // 1순위: 임시 저장 데이터
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      if (saved) {
        const { states } = JSON.parse(saved)
        if (Array.isArray(states)) {
          setApplicants(prev => prev.map(a => {
            const s = states.find(x => x.id === a.id)
            return s ? { ...a, pinned: s.pinned, status: s.status } : a
          }))
          return
        }
      }
    } catch {}

    // 2순위: 확정 저장 데이터 (context에서 shift.applicantStates로 복원)
    if (shift?.applicantStates?.length) {
      setApplicants(prev => prev.map(a => {
        const s = shift.applicantStates.find(x => x.id === a.id)
        return s ? { ...a, pinned: s.pinned, status: s.status } : a
      }))
    }
  }, [baseApplicants, STORAGE_KEY, shift?.applicantStates])

  // 상태 변경 시 자동 저장 (600ms 디바운스, 초기 렌더 제외)
  useEffect(() => {
    if (isFirstSave.current) { isFirstSave.current = false; return }
    const timer = setTimeout(() => {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify({
          states: applicants.map(a => ({ id: a.id, pinned: a.pinned, status: a.status })),
          savedAt: Date.now(),
        }))
      } catch {}
    }, 600)
    return () => clearTimeout(timer)
  }, [applicants, STORAGE_KEY])

  if (!shift) return (
    <EmptyState icon={Calendar} title="Shift를 찾을 수 없습니다"
      action={{ label: 'Shift 목록', to: '/shifts' }} />
  )

  const hiredCount    = applicants.filter(a => a.status === 'hired').length
  const rejectedCount = applicants.filter(a => a.status === 'rejected').length
  const pendingCount  = applicants.filter(a => a.status === 'pending').length
  const totalApplicants = shift.applicantCount ?? applicants.length

  const sortedFiltered = [...applicants]
    .filter(a => filterBy === 'all' || a.status === filterBy)
    .sort((a, b) => {
      if (sortBy === 'rating') {
        const diff = (b.rating ?? -1) - (a.rating ?? -1)
        if (diff !== 0) return diff
        return (b.pinned ? 1 : 0) - (a.pinned ? 1 : 0)
      }
      if (a.pinned && !b.pinned) return -1
      if (!a.pinned && b.pinned) return 1
      return 0
    })

  function togglePin(appId) {
    setApplicants(prev => prev.map(a => a.id === appId ? { ...a, pinned: !a.pinned } : a))
  }
  function setStatus(appId, status) {
    setApplicants(prev => prev.map(a => a.id === appId ? { ...a, status } : a))
  }

  const drawerApplicant = drawerApplicantId
    ? applicants.find(a => a.id === drawerApplicantId) ?? null
    : null
  const drawerIndex = applicants.findIndex(a => a.id === drawerApplicantId)

  const fillPct    = Math.min((hiredCount / shift.requiredStaff) * 100, 100)
  const isComplete = hiredCount >= shift.requiredStaff

  function handleReset() {
    setApplicants(baseApplicants.map(a => ({ ...a })))
    isFirstSave.current = false
    localStorage.removeItem(STORAGE_KEY)
    setModal(null)
    addToast({ type: 'info', message: '선택이 초기화되었습니다' })
  }

  function handleSave() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        states: applicants.map(a => ({ id: a.id, pinned: a.pinned, status: a.status })),
        savedAt: Date.now(),
      }))
      addToast({ type: 'success', message: '임시 저장되었습니다' })
    } catch {
      addToast({ type: 'error', message: '저장에 실패했습니다' })
    }
  }

  function handleFinalize() {
    const applicantStates = applicants.map(a => ({ id: a.id, pinned: a.pinned, status: a.status }))
    finalizeShift(shift.id, { hiredCount, applicantStates })
    localStorage.removeItem(STORAGE_KEY)
    setModal(null)
    navigate('/contracts')
  }

  return (
    <div className="space-y-5 max-w-5xl pb-24">
      {/* 뒤로가기 */}
      <Link to="/shifts" className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-navy transition-colors">
        <ChevronLeft size={16} />목록으로
      </Link>

      {/* ── 상단 정보 카드 ────────────────────────────────── */}
      <Card>
        <div className="flex flex-col md:flex-row md:items-start gap-5">
          <div className="flex-1 space-y-3">
            <div className="flex items-center gap-2.5 flex-wrap">
              <h1 className="text-xl font-bold text-navy">{shift.jobTitle}</h1>
              <StatusBadge status={shift.status} />
              {isComplete && (
                <span className="text-xs font-bold text-green-600 bg-green-50 border border-green-200 px-2 py-0.5 rounded-full">
                  정원 완료 ✓
                </span>
              )}
            </div>
            <div className="flex flex-wrap gap-x-5 gap-y-1.5 text-sm text-gray-600">
              <span className="flex items-center gap-1.5">
                <Calendar size={14} className="text-gray-400" />{shift.date}
              </span>
              <span className="flex items-center gap-1.5">
                <Clock size={14} className="text-gray-400" />{shift.startTime} ~ {shift.endTime}
              </span>
              {shift.location && (
                <span className="flex items-center gap-1.5">
                  <MapPin size={14} className="text-gray-400" />{shift.location}
                </span>
              )}
            </div>

            <div className="pt-1 space-y-1.5">
              <div className="flex items-center justify-between text-xs text-gray-500">
                <span>채용 확정 진행률</span>
                <span className="tabular-nums">{hiredCount} / {shift.requiredStaff}명</span>
              </div>
              <div className="h-2 bg-offwhite-200 rounded-full overflow-hidden">
                <div
                  className={`h-full rounded-full transition-all duration-300 ${isComplete ? 'bg-green-500' : 'bg-orange'}`}
                  style={{ width: `${fillPct}%` }}
                />
              </div>
            </div>
          </div>

          {/* 오른쪽: [지원] 총수 + [확정] 핵심 지표 */}
          <div className="grid grid-cols-2 md:flex md:flex-col md:items-end gap-3 md:gap-3 shrink-0 pt-1 md:pt-0 border-t border-offwhite-100 md:border-0">
            <div className="md:text-right">
              <div className="flex items-center gap-1.5 md:justify-end">
                <span className="text-xs font-bold text-gray-400 bg-gray-100 px-2 py-0.5 rounded-md tracking-wide">지원</span>
                <span className="text-2xl font-extrabold text-gray-400 tabular-nums leading-none">{totalApplicants}</span>
                <span className="text-sm text-gray-400">/ {shift.requiredStaff}명</span>
              </div>
              <p className="text-xs text-gray-400 mt-0.5">총 지원 / 모집 정원</p>
            </div>

            <div className="md:text-right">
              <div className="flex items-center gap-1.5 md:justify-end">
                <span className={`text-xs font-extrabold px-2 py-0.5 rounded-md tracking-wide
                  ${isComplete ? 'bg-green-500 text-white' : 'bg-orange text-white'}`}>
                  확정
                </span>
                <span className={`text-3xl md:text-4xl font-extrabold tabular-nums leading-none
                  ${isComplete ? 'text-green-600' : 'text-navy'}`}>
                  {hiredCount}
                </span>
                <span className="text-base md:text-xl font-bold text-gray-400">/ {shift.requiredStaff}명</span>
              </div>
              <p className="text-xs text-gray-500 mt-0.5">채용 확정 현황</p>
            </div>

            <div className="col-span-2 md:col-auto flex items-center gap-1.5 flex-wrap md:justify-end">
              {pendingCount > 0 && (
                <span className="text-xs bg-amber-50 text-amber-600 border border-amber-200 font-semibold px-2 py-0.5 rounded-full">
                  미결정 {pendingCount}
                </span>
              )}
              {rejectedCount > 0 && (
                <span className="text-xs bg-red-50 text-red-500 border border-red-200 font-semibold px-2 py-0.5 rounded-full">
                  거절 {rejectedCount}
                </span>
              )}
            </div>
          </div>
        </div>
      </Card>

      {/* ── 지원자 선별 섹션 ──────────────────────────────── */}
      {applicants.length > 0 ? (
        <div className="space-y-3">
          {/* 컨트롤 바 */}
          <div className="space-y-2">
            {/* 정렬 */}
            <div className="flex items-center gap-1.5 overflow-x-auto scrollbar-hide pb-0.5">
              <ArrowUpDown size={14} className="text-gray-400 shrink-0" />
              <span className="text-xs text-gray-500 font-medium mr-0.5 shrink-0">정렬</span>
              {SORT_OPTIONS.map(opt => (
                <button
                  key={opt.key}
                  onClick={() => setSortBy(opt.key)}
                  className={`shrink-0 text-xs font-semibold px-3 py-1.5 rounded-lg border transition-all ${
                    sortBy === opt.key
                      ? 'bg-navy text-white border-navy'
                      : 'bg-white text-gray-500 border-offwhite-200 hover:border-navy hover:text-navy'
                  }`}
                >
                  {opt.label}
                </button>
              ))}
            </div>

            {/* 필터 */}
            <div className="flex items-center gap-1.5 overflow-x-auto scrollbar-hide pb-0.5">
              <SlidersHorizontal size={14} className="text-gray-400 shrink-0" />
              <span className="text-xs text-gray-500 font-medium mr-0.5 shrink-0">필터</span>
              {FILTER_OPTIONS.map(opt => {
                const cnt = opt.key === 'all'
                  ? applicants.length
                  : applicants.filter(a => a.status === opt.key).length
                return (
                  <button
                    key={opt.key}
                    onClick={() => setFilterBy(opt.key)}
                    className={`shrink-0 text-xs font-semibold px-3 py-1.5 rounded-lg border transition-all ${
                      filterBy === opt.key
                        ? 'bg-orange text-white border-orange'
                        : 'bg-white text-gray-500 border-offwhite-200 hover:border-orange hover:text-orange'
                    }`}
                  >
                    {opt.label} <span className="opacity-75">{cnt}</span>
                  </button>
                )
              })}
            </div>
          </div>

          {/* 지원자 카드 리스트 */}
          <Card padding={false}>
            {sortedFiltered.length === 0 ? (
              <div className="py-10 text-center text-sm text-gray-400">해당 조건의 지원자가 없습니다.</div>
            ) : (
              <div className="divide-y divide-offwhite-100">
                {sortedFiltered.map(a => {
                  const origIdx = applicants.findIndex(x => x.id === a.id)
                  return (
                    <div
                      key={a.id}
                      className={`flex items-center gap-2 px-3 py-3 transition-colors
                        ${a.status === 'hired'    ? 'bg-green-50/60' :
                          a.status === 'rejected' ? 'bg-red-50/30'   : 'hover:bg-offwhite-100/60'}
                        ${a.pinned ? 'border-l-2 border-yellow-400' : 'border-l-2 border-transparent'}`}
                    >
                      {/* 별표 (모바일에서 숨김) */}
                      <button
                        onClick={() => togglePin(a.id)}
                        className={`shrink-0 transition-colors hidden sm:block ${
                          a.pinned ? 'text-yellow-400' : 'text-gray-200 hover:text-yellow-300'
                        }`}
                        title={a.pinned ? '고정 해제' : '별표 고정'}
                      >
                        <Star size={16} fill={a.pinned ? 'currentColor' : 'none'} />
                      </button>

                      <Avatar name={a.name} index={origIdx} />

                      {/* 이름·정보 */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-1.5 flex-wrap">
                          {a.pinned && (
                            <Star size={11} className="text-yellow-400 sm:hidden shrink-0" fill="currentColor" />
                          )}
                          <span className="font-semibold text-navy text-sm truncate">{a.name}</span>
                          <span className="text-xs text-gray-400 whitespace-nowrap">{a.age}세 · {a.gender}</span>
                        </div>
                        <div className="flex items-center gap-2 mt-0.5">
                          <StarRating rating={a.rating} />
                          <span className={`text-xs font-semibold hidden sm:inline
                            ${a.hireCount >= 15 ? 'text-orange' :
                              a.hireCount >= 5  ? 'text-blue-600' : 'text-gray-400'}`}>
                            고용 {a.hireCount}회
                          </span>
                        </div>
                      </div>

                      {/* 상태 버튼 */}
                      <div className="flex items-center gap-1 shrink-0">
                        {a.status === 'pending' ? (
                          <>
                            <button
                              onClick={() => setStatus(a.id, 'hired')}
                              className="flex items-center gap-0.5 text-xs font-semibold text-white bg-green-600 hover:bg-green-700 px-2 py-1.5 rounded-lg transition-colors"
                            >
                              <Check size={11} /><span className="hidden sm:inline">채용</span>
                            </button>
                            <button
                              onClick={() => setStatus(a.id, 'rejected')}
                              className="flex items-center gap-0.5 text-xs font-semibold text-red-500 bg-red-50 hover:bg-red-100 border border-red-200 px-2 py-1.5 rounded-lg transition-colors"
                            >
                              <UserX size={11} /><span className="hidden sm:inline">거절</span>
                            </button>
                          </>
                        ) : (
                          <div className="flex items-center gap-1">
                            <StatusChip status={a.status} />
                            <button
                              onClick={() => setStatus(a.id, 'pending')}
                              className="text-xs text-gray-400 hover:text-navy transition-colors underline"
                            >
                              취소
                            </button>
                          </div>
                        )}
                      </div>

                      <button
                        onClick={() => setDrawerApplicantId(a.id)}
                        className="shrink-0 p-1.5 rounded-lg text-gray-300 hover:text-navy hover:bg-offwhite transition-colors"
                      >
                        <ChevronRight size={16} />
                      </button>
                    </div>
                  )
                })}
              </div>
            )}
          </Card>

          {isComplete && (
            <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-green-50 border border-green-200 text-green-700">
              <UserCheck size={16} />
              <span className="text-sm font-semibold">
                🎉 모집 정원 {shift.requiredStaff}명 채용 확정 완료!
              </span>
            </div>
          )}
          {!isComplete && hiredCount > 0 && (
            <div className="flex items-center gap-3 px-4 py-3 rounded-xl bg-orange-50 border border-orange/20 text-orange">
              <UserCheck size={16} />
              <span className="text-sm font-semibold">
                현재 {hiredCount}명 확정 — {shift.requiredStaff - hiredCount}명 더 채용하면 정원이 찹니다
              </span>
            </div>
          )}
        </div>
      ) : (
        <Card>
          <dl className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <dt className="text-gray-500">날짜</dt>
              <dd className="font-semibold text-navy mt-0.5">{shift.date}</dd>
            </div>
            <div>
              <dt className="text-gray-500">시간</dt>
              <dd className="font-semibold text-navy mt-0.5">{shift.startTime} ~ {shift.endTime}</dd>
            </div>
            <div>
              <dt className="text-gray-500">장소</dt>
              <dd className="font-semibold text-navy mt-0.5">{shift.location}</dd>
            </div>
            <div>
              <dt className="text-gray-500">확정 / 모집</dt>
              <dd className="font-semibold text-navy mt-0.5">
                {shift.confirmedStaff} / {shift.requiredStaff}명
              </dd>
            </div>
          </dl>
        </Card>
      )}

      {/* ── Sticky 하단 액션 푸터 ─────────────────────────── */}
      {applicants.length > 0 && (
        <div className="sticky bottom-0 -mx-4 sm:-mx-6 px-4 sm:px-6 py-2.5 bg-white border-t border-offwhite-200 shadow-[0_-2px_12px_rgba(27,43,72,0.08)]">
          <div className="max-w-5xl flex items-center justify-between gap-2">
            {/* 왼쪽 현황 */}
            <div className="flex items-center gap-1.5 text-xs text-gray-400 min-w-0">
              <span className="tabular-nums font-medium whitespace-nowrap">
                확정 <strong className={isComplete ? 'text-green-600' : 'text-navy'}>{hiredCount}</strong>/{shift.requiredStaff}명
              </span>
              {pendingCount > 0 && (
                <span className="hidden sm:inline whitespace-nowrap">· 미결정 {pendingCount}명</span>
              )}
            </div>
            {/* 오른쪽 버튼 */}
            <div className="flex items-center gap-1.5 shrink-0">
              <button
                onClick={() => setModal('reset')}
                className="flex items-center gap-1 px-2.5 sm:px-4 py-2 rounded-lg border border-offwhite-200 text-gray-500 text-xs sm:text-sm font-semibold hover:border-red-200 hover:text-red-500 transition-colors"
              >
                <RotateCcw size={12} /><span className="hidden sm:inline">초기화</span>
              </button>
              <button
                onClick={handleSave}
                className="flex items-center gap-1 px-2.5 sm:px-4 py-2 rounded-lg border border-navy/20 text-navy text-xs sm:text-sm font-semibold hover:bg-navy/5 transition-colors"
              >
                <Save size={12} /><span className="hidden sm:inline">임시 저장</span><span className="sm:hidden">저장</span>
              </button>
              <button
                onClick={() => setModal('finalize')}
                className={`flex items-center gap-1 px-3 sm:px-5 py-2 rounded-lg text-white text-xs sm:text-sm font-bold transition-colors
                  ${isComplete ? 'bg-green-600 hover:bg-green-700' : 'bg-orange hover:bg-orange-600'}`}
              >
                <CheckSquare size={12} />완료/확정
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 사이드 드로워 */}
      {drawerApplicant && (
        <SideDrawer
          applicant={drawerApplicant}
          index={drawerIndex}
          onClose={() => setDrawerApplicantId(null)}
          onPin={() => togglePin(drawerApplicant.id)}
          onHire={() => setStatus(drawerApplicant.id, 'hired')}
          onReject={() => setStatus(drawerApplicant.id, 'rejected')}
        />
      )}

      {/* 확인 모달 */}
      {modal && (
        <ConfirmModal
          type={modal}
          hiredCount={hiredCount}
          requiredStaff={shift.requiredStaff}
          onConfirm={modal === 'reset' ? handleReset : handleFinalize}
          onCancel={() => setModal(null)}
        />
      )}
    </div>
  )
}
