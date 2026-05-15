import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  User, Mail, Phone, MapPin, Pencil, X, Plus, Check,
  Star, Clock, Briefcase, ShieldCheck, FileText, Banknote,
  Lock, LogOut, Trash2, ChevronDown, Upload, AlertTriangle, Camera,
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { useIndividualData } from '../../hooks/useIndividualData'
import { useRatings } from '../../hooks/useRatings'
import { applicationApi } from '../../services/api'

const WAGE_LABEL = { HOURLY: '시급', DAILY: '일급', MONTHLY: '월급', FIXED: '고정급' }

const BANKS = ['국민은행', '신한은행', '하나은행', '우리은행', 'IBK기업은행', 'NH농협은행', 'SC제일은행', '카카오뱅크', '토스뱅크']

const TIME_OPTIONS = ['주말', '평일', '새벽', '오전', '오후', '저녁']

const MBTI_TYPES = [
  'INTJ','INTP','ENTJ','ENTP',
  'INFJ','INFP','ENFJ','ENFP',
  'ISTJ','ISFJ','ESTJ','ESFJ',
  'ISTP','ISFP','ESTP','ESFP',
]

const MBTI_DESCRIPTIONS = {
  INTJ: '전략가형 — 독립적이고 결단력 있는 분석가',
  INTP: '논리학자형 — 창의적이고 탐구적인 사색가',
  ENTJ: '통솔자형 — 대담하고 상상력 풍부한 리더',
  ENTP: '변론가형 — 영리하고 호기심 많은 사상가',
  INFJ: '옹호자형 — 조용하지만 영감을 주는 이상주의자',
  INFP: '중재자형 — 시적이고 친절하며 이타적인 사람',
  ENFJ: '선도자형 — 카리스마 있고 영감을 주는 지도자',
  ENFP: '활동가형 — 열정적이고 창의적이며 사교적',
  ISTJ: '현실주의자형 — 사실에 충실한 신뢰할 수 있는 사람',
  ISFJ: '수호자형 — 헌신적이고 따뜻한 보호자',
  ESTJ: '경영자형 — 탁월한 관리자, 행정의 달인',
  ESFJ: '집정관형 — 배려심 깊고 사교적인 팀플레이어',
  ISTP: '장인형 — 대담하고 실용적인 실험가',
  ISFP: '모험가형 — 유연하고 매력적인 예술가',
  ESTP: '기업가형 — 영리하고 에너지 넘치는 행동파',
  ESFP: '연예인형 — 즉흥적이고 활발한 엔터테이너',
}

const SKILL_SUGGESTIONS = [
  '카페/음료 제조', '바리스타', '행사 진행', '고객 응대', '포스(POS) 조작',
  '주방 보조', '음식 서빙', '홀 서빙', '엑셀/스프레드시트', '파워포인트',
  '영어 회화', '중국어 회화', '일본어 회화', '운전면허', '지게차 운전',
  '홍보/마케팅', '팝업스토어 운영', '물류/포장 작업', '입출고 관리', '사무 보조',
  '사진 촬영', '영상 편집', 'SNS 콘텐츠 관리', '도소매 판매',
]

const REGIONS = {
  '서울': ['강남구','강동구','강북구','강서구','관악구','광진구','구로구','금천구','노원구','도봉구','동대문구','동작구','마포구','서대문구','서초구','성동구','성북구','송파구','양천구','영등포구','용산구','은평구','종로구','중구','중랑구'],
  '부산': ['강서구','금정구','기장군','남구','동구','동래구','부산진구','북구','사상구','사하구','서구','수영구','연제구','영도구','중구','해운대구'],
  '대구': ['남구','달서구','달성군','동구','북구','서구','수성구','중구'],
  '인천': ['강화군','계양구','남동구','동구','미추홀구','부평구','서구','연수구','옹진군','중구'],
  '광주': ['광산구','남구','동구','북구','서구'],
  '대전': ['대덕구','동구','서구','유성구','중구'],
  '울산': ['남구','동구','북구','울주군','중구'],
  '세종': ['세종시'],
  '경기': ['가평군','고양시','과천시','광명시','광주시','구리시','군포시','김포시','남양주시','동두천시','부천시','성남시','수원시','시흥시','안산시','안성시','안양시','양주시','양평군','여주시','연천군','오산시','용인시','의왕시','의정부시','이천시','파주시','평택시','포천시','하남시','화성시'],
  '강원': ['강릉시','고성군','동해시','삼척시','속초시','양구군','양양군','영월군','원주시','인제군','정선군','철원군','춘천시','태백시','평창군','홍천군','화천군','횡성군'],
  '충북': ['괴산군','단양군','보은군','영동군','옥천군','음성군','제천시','증평군','진천군','청주시','충주시'],
  '충남': ['공주시','금산군','논산시','당진시','보령시','부여군','서산시','서천군','아산시','예산군','천안시','청양군','태안군','홍성군','계룡시'],
  '전북': ['고창군','군산시','김제시','남원시','무주군','부안군','순창군','완주군','익산시','임실군','장수군','전주시','정읍시','진안군'],
  '전남': ['강진군','고흥군','곡성군','광양시','구례군','나주시','담양군','목포시','무안군','보성군','순천시','신안군','여수시','영광군','영암군','완도군','장성군','장흥군','진도군','함평군','해남군','화순군'],
  '경북': ['경산시','경주시','고령군','구미시','김천시','문경시','봉화군','상주시','성주군','안동시','영덕군','영양군','영주시','영천시','예천군','울릉군','울진군','의성군','청도군','청송군','칠곡군','포항시'],
  '경남': ['거제시','거창군','고성군','김해시','남해군','밀양시','사천시','산청군','양산시','의령군','진주시','창녕군','창원시','통영시','하동군','함안군','함양군','합천군'],
  '제주': ['서귀포시','제주시'],
}

const TYPE_COLOR = {
  '파트타임': 'bg-blue-50 text-blue-600',
  '단기':     'bg-orange-50 text-orange',
  '프리랜서': 'bg-purple-50 text-purple-600',
}

function Modal({ onClose, children }) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      onClick={e => e.target === e.currentTarget && onClose()}
    >
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 overflow-hidden">
        {children}
      </div>
    </div>
  )
}

function SectionCard({ title, icon: Icon, action, children }) {
  return (
    <div className="bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
      <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
        <div className="flex items-center gap-2">
          {Icon && <Icon size={16} className="text-navy" />}
          <h2 className="font-bold text-navy">{title}</h2>
        </div>
        {action}
      </div>
      <div className="p-5">{children}</div>
    </div>
  )
}

function AvatarCropModal({ photoUrl, onSave, onClose }) {
  const [pos, setPos] = useState({ x: 0, y: 0 })
  const [scale, setScale] = useState(1.2)
  const dragging = useRef(false)
  const lastPt = useRef({ x: 0, y: 0 })

  function onPtrDown(e) {
    dragging.current = true
    lastPt.current = { x: e.clientX, y: e.clientY }
    e.currentTarget.setPointerCapture(e.pointerId)
  }
  function onPtrMove(e) {
    if (!dragging.current) return
    const dx = e.clientX - lastPt.current.x
    const dy = e.clientY - lastPt.current.y
    lastPt.current = { x: e.clientX, y: e.clientY }
    setPos(p => ({ x: p.x + dx, y: p.y + dy }))
  }
  function onPtrUp() { dragging.current = false }

  function apply() {
    const DISP = 192
    const OUT = 256
    const k = OUT / DISP
    const canvas = document.createElement('canvas')
    canvas.width = OUT
    canvas.height = OUT
    const ctx = canvas.getContext('2d')
    const img = new window.Image()
    img.onload = () => {
      ctx.beginPath()
      ctx.arc(OUT / 2, OUT / 2, OUT / 2, 0, Math.PI * 2)
      ctx.clip()
      ctx.translate(OUT / 2 + pos.x * k, OUT / 2 + pos.y * k)
      ctx.scale(scale, scale)
      ctx.translate(-OUT / 2, -OUT / 2)
      const r = img.width / img.height
      let dW = OUT, dH = OUT
      if (r > 1) dW = OUT * r; else dH = OUT / r
      ctx.drawImage(img, (OUT - dW) / 2, (OUT - dH) / 2, dW, dH)
      onSave(canvas.toDataURL('image/jpeg', 0.9))
    }
    img.src = photoUrl
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
      onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm mx-4 p-5 space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="font-bold text-navy">아바타 조정</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-navy"><X size={18} /></button>
        </div>
        <p className="text-xs text-gray-400 text-center">드래그로 위치 조정 · 슬라이더로 크기 조정</p>
        <div className="flex justify-center">
          <div
            className="w-48 h-48 rounded-full overflow-hidden border-2 border-navy/20 cursor-grab active:cursor-grabbing select-none bg-offwhite"
            style={{ touchAction: 'none' }}
            onPointerDown={onPtrDown}
            onPointerMove={onPtrMove}
            onPointerUp={onPtrUp}
            onPointerLeave={onPtrUp}
          >
            <img
              src={photoUrl}
              alt=""
              draggable={false}
              className="w-full h-full object-cover pointer-events-none"
              style={{ transform: `translate(${pos.x}px, ${pos.y}px) scale(${scale})`, transformOrigin: 'center' }}
            />
          </div>
        </div>
        <div className="space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-gray-500">크기</span>
            <span className="text-xs text-gray-400 tabular-nums">{Math.round(scale * 100)}%</span>
          </div>
          <input type="range" min="0.5" max="3" step="0.05"
            value={scale} onChange={e => setScale(Number(e.target.value))}
            className="w-full accent-orange" />
        </div>
        <div className="flex gap-2 pt-1">
          <button onClick={() => { setPos({ x: 0, y: 0 }); setScale(1.2) }}
            className="flex-1 py-2.5 text-xs text-gray-500 border border-offwhite-200 rounded-xl hover:border-navy transition-colors">
            초기화
          </button>
          <button onClick={onClose}
            className="flex-1 py-2.5 text-xs text-gray-500 border border-offwhite-200 rounded-xl hover:border-navy transition-colors">
            취소
          </button>
          <button onClick={apply}
            className="flex-1 py-2.5 text-xs font-semibold text-white bg-orange rounded-xl hover:bg-orange-600 transition-colors">
            적용
          </button>
        </div>
      </div>
    </div>
  )
}

export default function IndividualProfilePage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const { profile, updateProfile } = useIndividualData()
  const { getStaffRatings } = useRatings()

  const [myApplications, setMyApplications] = useState([])

  useEffect(() => {
    applicationApi.myList()
      .then(r => r.ok ? r.json() : [])
      .then(data => setMyApplications(Array.isArray(data) ? data : []))
      .catch(() => {})
  }, [])

  const completedApps  = myApplications.filter(a => a.status === 'COMPLETED')
  const noShowApps     = myApplications.filter(a => a.status === 'NO_SHOW')
  const completedCount = completedApps.length
  const noShowCount    = noShowApps.length
  const totalRelevant  = completedCount + noShowCount
  // 완료+노쇼 합계 5건 이상이고 노쇼 0건일 때만 뱃지 표시
  const hasNoShowZeroBadge = totalRelevant >= 5 && noShowCount === 0

  const myRatings = getStaffRatings(user?.name ?? '')
  const ratingItems = myRatings.map(r => ({
    key: r.id,
    title: r.shiftLabel,
    date: new Date(r.createdAt).toLocaleDateString('ko-KR'),
    stars: r.stars,
    comment: r.comment,
  }))
  const avgRatingDisplay = ratingItems.length > 0
    ? (ratingItems.reduce((s, r) => s + r.stars, 0) / ratingItems.length).toFixed(1)
    : '-'

  const healthFileRef = useRef(null)
  const safetyFileRef = useRef(null)
  const docRefs = { health: healthFileRef, safety: safetyFileRef }

  const [toast, setToast] = useState('')
  function showToast(msg) {
    setToast(msg)
    setTimeout(() => setToast(''), 3000)
  }

  const [showResume,   setShowResume]   = useState(false)
  const [showPwChange, setShowPwChange] = useState(false)
  const [showWithdraw, setShowWithdraw] = useState(false)
  const [statModal,    setStatModal]    = useState(null)

  const [editingProfile, setEditingProfile] = useState(false)
  const [profileForm,    setProfileForm]    = useState(null)
  const [skillInput,     setSkillInput]     = useState('')
  const [showSkillInput, setShowSkillInput] = useState(false)

  function openProfileEdit() {
    setProfileForm({
      phone:   profile.phone   || '',
      address: profile.address || '',
      bio:     profile.bio     || '',
      skills:  [...(profile.skills ?? [])],
    })
    setEditingProfile(true)
  }

  function saveProfile() {
    updateProfile(profileForm)
    setEditingProfile(false)
    showToast('프로필이 저장되었습니다.')
  }

  function handleRemoveSkill(s) {
    if (editingProfile) {
      setProfileForm(p => ({ ...p, skills: p.skills.filter(x => x !== s) }))
    } else {
      updateProfile({ skills: (profile.skills ?? []).filter(x => x !== s) })
    }
  }

  function handleAddSkill(s) {
    if (!s) return
    if (editingProfile) {
      if (!profileForm.skills.includes(s)) setProfileForm(p => ({ ...p, skills: [...p.skills, s] }))
    } else {
      const cur = profile.skills ?? []
      if (!cur.includes(s)) updateProfile({ skills: [...cur, s] })
    }
  }

  const region = profile.regions?.[0] ?? null
  const [showRegionInput, setShowRegionInput] = useState(!region)
  const [regionSido,      setRegionSido]      = useState('')
  const [regionGungu,     setRegionGungu]     = useState('')

  function setRegion() {
    if (!regionSido || !regionGungu) return
    updateProfile({ regions: [`${regionSido} ${regionGungu}`] })
    setRegionSido('')
    setRegionGungu('')
    setShowRegionInput(false)
  }

  const mbti = profile.mbti || ''
  const [editingMbti, setEditingMbti] = useState(false)
  const [mbtiDraft,   setMbtiDraft]   = useState('')

  function openMbtiEdit() { setMbtiDraft(mbti); setEditingMbti(true) }
  function saveMbti() { updateProfile({ mbti: mbtiDraft }); setEditingMbti(false); showToast('MBTI가 저장되었습니다.') }

  const preferredTimes = profile.preferredTimes ?? []

  function toggleTime(t) {
    const next = preferredTimes.includes(t)
      ? preferredTimes.filter(x => x !== t)
      : [...preferredTimes, t]
    updateProfile({ preferredTimes: next })
  }

  const account = profile.account ?? null
  const [editingAccount, setEditingAccount] = useState(false)
  const [accountForm,    setAccountForm]    = useState({ bank: '국민은행', number: '' })

  function saveAccount() {
    updateProfile({ account: accountForm })
    setEditingAccount(false)
    showToast('계좌 정보가 저장되었습니다.')
  }

  const documents = profile.documents ?? { health: false, safety: false }

  function handleDocumentUpload(key, file) {
    if (!file) return
    updateProfile({ documents: { ...documents, [key]: true } })
    showToast(`${file.name} 업로드 완료되었습니다.`)
  }

  function withdrawDocument(key) {
    updateProfile({ documents: { ...documents, [key]: false } })
    showToast('서류가 철회되었습니다.')
  }

  // ── 프로필 사진 (최대 10장, localStorage) ──
  const photoKey = `staffnow_photos_${user?.email?.replace(/[^a-zA-Z0-9]/g, '_') || 'anon'}`
  const [photos, setPhotos] = useState(() => {
    try { return JSON.parse(localStorage.getItem(`staffnow_photos_${user?.email?.replace(/[^a-zA-Z0-9]/g, '_') || 'anon'}`) ?? '[]') }
    catch { return [] }
  })
  const [savedPhotos, setSavedPhotos] = useState(() => {
    try { return JSON.parse(localStorage.getItem(`staffnow_photos_${user?.email?.replace(/[^a-zA-Z0-9]/g, '_') || 'anon'}`) ?? '[]') }
    catch { return [] }
  })
  const isDirty = photos.length !== savedPhotos.length || photos.some((p, i) => p.id !== savedPhotos[i]?.id)
  const savedPhotoIds = new Set(savedPhotos.map(p => p.id))
  const photoInputRef = useRef(null)
  const [showAvatarAdjust, setShowAvatarAdjust] = useState(false)

  function savePhotoList(list) {
    try {
      localStorage.setItem(photoKey, JSON.stringify(list))
      if (user?.name) {
        localStorage.setItem(`staffnow_photocount_${user.name}`, String(list.length))
        if (list.length > 0) {
          localStorage.setItem(`staffnow_avatar_${user.name}`, list[0].url)
        } else {
          localStorage.removeItem(`staffnow_avatar_${user.name}`)
        }
      }
    } catch {}
  }

  function handlePhotoFiles(e) {
    const files = Array.from(e.target.files ?? [])
    e.target.value = ''
    if (!files.length) return
    files.slice(0, 10 - photos.length).forEach(file => {
      const reader = new FileReader()
      reader.onload = ev => {
        setPhotos(prev => {
          if (prev.length >= 10) return prev
          return [...prev, { id: `${Date.now()}-${Math.random()}`, url: ev.target.result }]
        })
      }
      reader.readAsDataURL(file)
    })
  }

  function removePhoto(id) {
    setPhotos(prev => prev.filter(p => p.id !== id))
  }

  function handlePhotoSave() {
    savePhotoList(photos)
    setSavedPhotos(photos)
    window.dispatchEvent(new Event('staffnow:avatar-updated'))
    showToast('사진이 저장되었습니다.')
  }

  function handlePhotoCancel() {
    setPhotos(savedPhotos)
  }

  function handleAvatarCropSave(croppedUrl) {
    try {
      if (user?.name) localStorage.setItem(`staffnow_avatar_${user.name}`, croppedUrl)
    } catch {}
    setShowAvatarAdjust(false)
    window.dispatchEvent(new Event('staffnow:avatar-updated'))
    showToast('아바타가 업데이트되었습니다.')
  }

  const [pwForm,  setPwForm]  = useState({ current: '', next: '', confirm: '' })
  const [pwError, setPwError] = useState('')

  function handlePwChange() {
    if (!pwForm.current) { setPwError('현재 비밀번호를 입력해 주세요.'); return }
    if (pwForm.next.length < 8) { setPwError('새 비밀번호는 8자 이상이어야 합니다.'); return }
    if (pwForm.next !== pwForm.confirm) { setPwError('새 비밀번호가 일치하지 않습니다.'); return }
    setShowPwChange(false)
    setPwForm({ current: '', next: '', confirm: '' })
    setPwError('')
    showToast('비밀번호가 변경되었습니다.')
  }

  function handleLogout()  { logout(); navigate('/login') }
  function handleWithdraw() { logout(); navigate('/login') }

  const displaySkills = editingProfile ? profileForm.skills : (profile.skills ?? [])

  function maskAccount(num) {
    if (!num) return '-'
    const parts = num.split('-')
    if (parts.length >= 2) {
      return parts.map((p, i) => (i === parts.length - 1 ? '****' : p)).join('-')
    }
    return num.slice(0, -4).replace(/./g, '*') + num.slice(-4)
  }

  const stats = [
    { key: 'jobs',   label: '완료한 업무',  value: `${completedCount}건`,   icon: Briefcase,   color: 'text-blue-500',   bg: 'bg-blue-50' },
    { key: 'hours',  label: '총 근무 시간', value: '집계 예정',             icon: Clock,       color: 'text-purple-500', bg: 'bg-purple-50' },
    { key: 'rating', label: '평균 별점',    value: avgRatingDisplay,        icon: Star,        color: 'text-yellow-500', bg: 'bg-yellow-50', suffix: '/ 5.0' },
    { key: 'attend', label: '근태 지수',    value: `노쇼 ${noShowCount}회`, icon: ShieldCheck, color: 'text-green-600',  bg: 'bg-green-50', badge: true },
  ]

  return (
    <div className="space-y-5 max-w-2xl pb-10">

      {toast && (
        <div className="fixed top-5 left-1/2 -translate-x-1/2 z-50 bg-navy text-white text-sm font-medium px-5 py-2.5 rounded-xl shadow-lg transition-all">
          {toast}
        </div>
      )}

      {showAvatarAdjust && savedPhotos.length > 0 && (
        <AvatarCropModal
          photoUrl={savedPhotos[0].url}
          onSave={handleAvatarCropSave}
          onClose={() => setShowAvatarAdjust(false)}
        />
      )}

      <h1 className="text-2xl font-bold text-navy">내 프로필</h1>

      {/* Stats 카드 */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map(s => (
          <button
            key={s.label}
            onClick={() => s.key !== 'hours' ? setStatModal(s.key) : undefined}
            className={`bg-white rounded-2xl border border-offwhite-200 p-4 text-left transition-all group ${
              s.key !== 'hours' ? 'hover:border-navy hover:shadow-sm cursor-pointer' : 'cursor-default'
            }`}
          >
            <div className={`w-9 h-9 rounded-xl ${s.bg} flex items-center justify-center mb-3`}>
              <s.icon size={18} className={s.color} />
            </div>
            <p className="text-xl font-extrabold text-navy tabular-nums">
              {s.value}
              {s.suffix && <span className="text-xs font-normal text-gray-400 ml-1">{s.suffix}</span>}
            </p>
            <p className={`text-xs text-gray-500 mt-0.5 ${s.key !== 'hours' ? 'group-hover:text-navy transition-colors' : ''}`}>
              {s.label}
            </p>
            {s.badge && (
              <span className="inline-block mt-1.5 text-[10px] font-bold bg-green-100 text-green-700 px-2 py-0.5 rounded-full">
                최근 {Math.min(totalRelevant, 20)}회 중 {noShowCount}건
              </span>
            )}
          </button>
        ))}
      </div>

      {/* 프로필 카드 */}
      <div className="bg-white rounded-2xl border border-offwhite-200 p-5">
        <div className="flex items-start justify-between gap-3 mb-5">
          <div className="flex items-center gap-4">
            <button
              type="button"
              onClick={savedPhotos.length > 0 ? () => setShowAvatarAdjust(true) : undefined}
              className={`w-16 h-16 rounded-full overflow-hidden bg-orange flex items-center justify-center shrink-0 relative group ${savedPhotos.length > 0 ? 'cursor-pointer' : 'cursor-default'}`}
            >
              {savedPhotos.length > 0
                ? <img src={savedPhotos[0].url} alt="" className="w-full h-full object-cover" />
                : <span className="text-white text-2xl font-bold">{user?.avatar}</span>
              }
              {savedPhotos.length > 0 && (
                <div className="absolute inset-0 bg-black/0 group-hover:bg-black/30 transition-colors rounded-full flex items-center justify-center">
                  <Pencil size={14} className="text-white opacity-0 group-hover:opacity-100 transition-opacity" />
                </div>
              )}
            </button>
            <div>
              <div className="flex items-center gap-2 flex-wrap">
                <h2 className="text-lg font-bold text-navy">{user?.name}</h2>
                {hasNoShowZeroBadge && (
                  <span className="text-[10px] font-bold bg-green-100 text-green-700 px-2 py-0.5 rounded-full flex items-center gap-1">
                    <ShieldCheck size={10} />노쇼 제로
                  </span>
                )}
              </div>
              <p className="text-sm text-orange font-medium mt-0.5">{user?.roleLabel}</p>
            </div>
          </div>
          <div className="text-right shrink-0">
            <p className="text-[10px] text-gray-400">정산 대기 중</p>
            <p className="text-base font-extrabold text-orange">0원</p>
          </div>
        </div>

        <div className="flex items-center justify-between mb-4">
          <button
            onClick={() => setShowResume(true)}
            className="flex items-center gap-1.5 text-xs font-semibold text-navy border border-offwhite-200 px-3 py-1.5 rounded-lg hover:border-navy transition-colors"
          >
            <FileText size={13} />디지털 이력서 확인
          </button>

          {!editingProfile ? (
            <button onClick={openProfileEdit} className="flex items-center gap-1.5 text-sm font-semibold text-orange hover:underline">
              <Pencil size={13} />수정
            </button>
          ) : (
            <div className="flex gap-2">
              <button onClick={() => setEditingProfile(false)}
                className="flex items-center gap-1 text-xs text-gray-500 px-3 py-1.5 rounded-lg border border-offwhite-200 hover:border-navy transition-colors">
                <X size={12} />취소
              </button>
              <button onClick={saveProfile}
                className="flex items-center gap-1 text-xs font-semibold text-white bg-orange px-3 py-1.5 rounded-lg hover:bg-orange-600 transition-colors">
                <Check size={12} />저장
              </button>
            </div>
          )}
        </div>

        {editingProfile ? (
          <div className="space-y-3">
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-1">이메일</label>
              <input value={user?.email} disabled
                className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm bg-offwhite text-gray-400 cursor-not-allowed" />
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-1">연락처</label>
              <input value={profileForm.phone}
                onChange={e => setProfileForm(p => ({ ...p, phone: e.target.value }))}
                placeholder="010-0000-0000"
                className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy" />
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-1">거주지</label>
              <input value={profileForm.address}
                onChange={e => setProfileForm(p => ({ ...p, address: e.target.value }))}
                placeholder="서울 강남구"
                className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy" />
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-1">한 줄 소개</label>
              <input value={profileForm.bio}
                onChange={e => setProfileForm(p => ({ ...p, bio: e.target.value }))}
                placeholder="간단한 자기소개를 입력하세요"
                className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy" />
            </div>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {[
              { icon: Mail,   label: '이메일', value: user?.email || '-' },
              { icon: Phone,  label: '연락처', value: profile.phone || '-' },
              { icon: MapPin, label: '거주지', value: profile.address || '-' },
              { icon: User,   label: '소개',   value: profile.bio || '미입력' },
            ].map(item => (
              <div key={item.label} className="flex items-center gap-3 p-3 bg-offwhite rounded-xl">
                <item.icon size={15} className="text-orange shrink-0" />
                <div>
                  <p className="text-[10px] text-gray-400">{item.label}</p>
                  <p className={`text-sm font-medium ${item.value === '-' || item.value === '미입력' ? 'text-gray-300' : 'text-navy'}`}>
                    {item.value}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* 프로필 사진 */}
      <SectionCard
        title="프로필 사진"
        icon={Camera}
        action={
          isDirty ? (
            <div className="flex items-center gap-1.5">
              <button onClick={handlePhotoCancel}
                className="text-xs text-gray-400 px-2.5 py-1 rounded-lg hover:bg-offwhite transition-colors">
                취소
              </button>
              <button onClick={handlePhotoSave}
                className="text-xs font-semibold text-white bg-orange px-3 py-1 rounded-lg hover:bg-orange-600 transition-colors">
                저장
              </button>
            </div>
          ) : (
            <span className="text-xs text-gray-400 font-medium">{savedPhotos.length} / 10</span>
          )
        }
      >
        <input
          ref={photoInputRef}
          type="file"
          accept="image/*"
          multiple
          className="hidden"
          onChange={handlePhotoFiles}
        />
        <div className="grid grid-cols-3 sm:grid-cols-5 gap-2">
          {photos.map(photo => {
            const isPending = !savedPhotoIds.has(photo.id)
            return (
              <div key={photo.id}
                className={`relative aspect-square rounded-xl overflow-hidden group ${isPending ? 'ring-2 ring-orange ring-offset-1' : ''}`}>
                <img src={photo.url} alt="" className="w-full h-full object-cover" />
                {isPending && (
                  <span className="absolute bottom-1 left-1 text-[8px] font-bold bg-orange text-white px-1 py-0.5 rounded leading-none">
                    저장 전
                  </span>
                )}
                <button
                  onClick={() => removePhoto(photo.id)}
                  className="absolute top-1 right-1 w-5 h-5 rounded-full bg-black/60 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                >
                  <X size={10} />
                </button>
              </div>
            )
          })}
          {photos.length < 10 && (
            <button
              onClick={() => photoInputRef.current?.click()}
              className="aspect-square rounded-xl border-2 border-dashed border-offwhite-200 flex flex-col items-center justify-center gap-1 hover:border-orange hover:bg-orange/5 transition-colors text-gray-300 hover:text-orange"
            >
              <Plus size={20} />
              <span className="text-[10px] font-semibold">추가</span>
            </button>
          )}
        </div>
        {photos.length === 0 && (
          <p className="text-xs text-gray-400 mt-3 text-center">
            사진을 추가하면 기업 담당자에게 더 잘 어필할 수 있어요.
          </p>
        )}
        {savedPhotos.length > 0 && savedPhotos.length < 5 && !isDirty && (
          <div className="mt-3 p-3 bg-blue-50 rounded-xl border border-blue-100">
            <div className="flex items-center justify-between mb-1.5">
              <span className="text-xs font-semibold text-blue-700">상단 노출 부스트</span>
              <span className="text-xs text-blue-500 font-bold tabular-nums">{savedPhotos.length} / 5장</span>
            </div>
            <div className="w-full bg-blue-100 rounded-full h-1.5 mb-2">
              <div className="bg-blue-500 h-1.5 rounded-full transition-all"
                style={{ width: `${(savedPhotos.length / 5) * 100}%` }} />
            </div>
            <p className="text-xs text-blue-600">
              사진 {5 - savedPhotos.length}장만 더 추가하면 기업 담당자의 추천 인력 목록 상단에 우선 노출돼요.
            </p>
          </div>
        )}
        {isDirty && photos.length >= 5 && (
          <p className="text-xs text-blue-500 mt-3 text-center font-medium">
            저장하면 부스트가 활성화됩니다!
          </p>
        )}
        {savedPhotos.length >= 5 && !isDirty && (
          <div className="mt-3 p-3 bg-green-50 rounded-xl border border-green-200 flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-green-100 flex items-center justify-center shrink-0">
              <ShieldCheck size={16} className="text-green-600" />
            </div>
            <div>
              <p className="text-xs font-bold text-green-700">부스트 활성화 중!</p>
              <p className="text-xs text-green-600 mt-0.5">기업 담당자 추천 인력 목록 최상단에 우선 노출되고 있어요.</p>
            </div>
          </div>
        )}
      </SectionCard>

      {/* 보유 스킬 */}
      <SectionCard title="보유 스킬" icon={Star}
        action={
          <button onClick={() => setShowSkillInput(v => !v)}
            className="flex items-center gap-1 text-xs font-semibold text-orange hover:underline">
            <Plus size={13} />{showSkillInput ? '닫기' : '스킬 추가'}
          </button>
        }
      >
        <div className="flex flex-wrap gap-2 mb-3">
          {displaySkills.map(s => (
            <span key={s} className="flex items-center gap-1 text-sm bg-offwhite px-3 py-1.5 rounded-full text-gray-600 border border-offwhite-200">
              {s}
              <button onClick={() => handleRemoveSkill(s)} className="hover:text-red-500 transition-colors ml-0.5">
                <X size={11} />
              </button>
            </span>
          ))}
          {displaySkills.length === 0 && (
            <p className="text-sm text-gray-400">등록된 스킬이 없습니다. 오른쪽 버튼을 눌러 추가하세요.</p>
          )}
        </div>
        {showSkillInput && (
          <div className="space-y-3 pt-1">
            <div className="flex gap-2">
              <input type="text" placeholder="직접 입력 후 Enter" value={skillInput}
                onChange={e => setSkillInput(e.target.value)}
                onKeyDown={e => { if (e.key === 'Enter') { handleAddSkill(skillInput.trim()); setSkillInput('') } }}
                className="flex-1 border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy" />
              <button onClick={() => { handleAddSkill(skillInput.trim()); setSkillInput('') }}
                className="flex items-center gap-1 px-3 py-2 bg-navy text-white text-sm font-semibold rounded-lg hover:bg-navy/80 transition-colors">
                <Plus size={13} />추가
              </button>
            </div>
            <div>
              <p className="text-[11px] text-gray-400 mb-2">추천 스킬 (클릭하여 바로 추가)</p>
              <div className="flex flex-wrap gap-1.5">
                {SKILL_SUGGESTIONS.filter(s => !displaySkills.includes(s)).map(s => (
                  <button key={s} onClick={() => handleAddSkill(s)}
                    className="text-xs bg-blue-50 border border-blue-100 text-blue-600 px-2.5 py-1 rounded-full hover:bg-blue-100 active:scale-95 transition-all font-medium">
                    + {s}
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}
      </SectionCard>

      {/* MBTI */}
      <SectionCard title="MBTI" icon={User}
        action={
          !editingMbti ? (
            <button onClick={openMbtiEdit}
              className="flex items-center gap-1 text-xs font-semibold text-orange hover:underline">
              <Pencil size={13} />{mbti ? '수정' : '입력'}
            </button>
          ) : (
            <div className="flex gap-2">
              <button onClick={() => setEditingMbti(false)}
                className="flex items-center gap-1 text-xs text-gray-500 px-3 py-1.5 rounded-lg border border-offwhite-200 hover:border-navy transition-colors">
                <X size={12} />취소
              </button>
              <button onClick={saveMbti}
                className="flex items-center gap-1 text-xs font-semibold text-white bg-orange px-3 py-1.5 rounded-lg hover:bg-orange-600 transition-colors">
                <Check size={12} />저장
              </button>
            </div>
          )
        }
      >
        {editingMbti ? (
          <div className="space-y-3">
            <p className="text-xs text-gray-400">하나만 선택하세요</p>
            <div className="grid grid-cols-4 gap-2">
              {MBTI_TYPES.map(type => (
                <button key={type} onClick={() => setMbtiDraft(type)}
                  className={`py-2 rounded-xl text-sm font-bold transition-all border ${
                    mbtiDraft === type
                      ? 'bg-orange text-white border-orange shadow-sm'
                      : 'bg-offwhite text-gray-500 border-offwhite-200 hover:border-orange hover:text-orange'
                  }`}>
                  {type}
                </button>
              ))}
            </div>
            {mbtiDraft && (
              <p className="text-xs text-gray-500 bg-offwhite rounded-xl px-3 py-2">
                <span className="font-semibold text-navy">{mbtiDraft}</span> · {MBTI_DESCRIPTIONS[mbtiDraft]}
              </p>
            )}
          </div>
        ) : mbti ? (
          <div className="flex items-start gap-3 bg-offwhite rounded-xl p-3">
            <span className="text-base font-extrabold text-orange shrink-0">{mbti}</span>
            <p className="text-sm text-gray-600 leading-snug">{MBTI_DESCRIPTIONS[mbti]}</p>
          </div>
        ) : (
          <p className="text-sm text-gray-400">아직 MBTI를 입력하지 않았습니다. 오른쪽 버튼을 눌러 추가하세요.</p>
        )}
      </SectionCard>

      {/* 활동 가능 지역 */}
      <SectionCard title="활동 가능 지역" icon={MapPin}
        action={
          region && !showRegionInput ? (
            <button onClick={() => setShowRegionInput(true)} className="text-xs font-semibold text-orange hover:underline">
              변경
            </button>
          ) : null
        }
      >
        {region && !showRegionInput ? (
          <span className="inline-flex items-center gap-1.5 text-sm bg-navy/5 text-navy px-3 py-1.5 rounded-full border border-navy/10">
            <MapPin size={11} className="text-orange" />{region}
          </span>
        ) : (
          <div className="space-y-2">
            <p className="text-xs text-gray-400">본인 거주 지역을 선택해주세요 (1개만 설정 가능)</p>
            <div className="flex gap-2">
              <div className="relative flex-1">
                <select value={regionSido}
                  onChange={e => { setRegionSido(e.target.value); setRegionGungu('') }}
                  className="w-full appearance-none border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy bg-white text-navy">
                  <option value="">시/도 선택</option>
                  {Object.keys(REGIONS).map(s => <option key={s} value={s}>{s}</option>)}
                </select>
                <ChevronDown size={13} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
              </div>
              <div className="relative flex-1">
                <select value={regionGungu} onChange={e => setRegionGungu(e.target.value)} disabled={!regionSido}
                  className="w-full appearance-none border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy bg-white text-navy disabled:text-gray-300 disabled:bg-offwhite">
                  <option value="">구/군 선택</option>
                  {(REGIONS[regionSido] ?? []).map(g => <option key={g} value={g}>{g}</option>)}
                </select>
                <ChevronDown size={13} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
              </div>
            </div>
            <div className="flex gap-2">
              <button onClick={setRegion} disabled={!regionSido || !regionGungu}
                className="px-4 py-2 bg-navy text-white text-sm font-semibold rounded-lg hover:bg-navy/80 disabled:opacity-40 transition-colors">
                설정
              </button>
              {region && (
                <button onClick={() => { setShowRegionInput(false); setRegionSido(''); setRegionGungu('') }}
                  className="px-4 py-2 text-sm text-gray-400 rounded-lg border border-offwhite-200 hover:border-gray-400 transition-colors">
                  취소
                </button>
              )}
            </div>
          </div>
        )}
      </SectionCard>

      {/* 선호 업무 시간 */}
      <SectionCard title="선호 업무 시간" icon={Clock}>
        <div className="flex flex-wrap gap-2">
          {TIME_OPTIONS.map(t => {
            const selected = preferredTimes.includes(t)
            return (
              <button key={t} onClick={() => toggleTime(t)}
                className={`text-sm px-4 py-2 rounded-full border font-medium transition-all ${
                  selected ? 'bg-orange text-white border-orange' : 'bg-white text-gray-500 border-offwhite-200 hover:border-orange hover:text-orange'
                }`}>
                {t}
              </button>
            )
          })}
        </div>
        {preferredTimes.length === 0 && (
          <p className="text-xs text-gray-400 mt-2">선호 시간대를 선택해 주세요.</p>
        )}
      </SectionCard>

      {/* 정산 계좌 */}
      <SectionCard title="정산 계좌" icon={Banknote}
        action={
          !editingAccount ? (
            <button onClick={() => { setAccountForm(account ?? { bank: '국민은행', number: '' }); setEditingAccount(true) }}
              className="flex items-center gap-1 text-xs font-semibold text-orange hover:underline">
              <Pencil size={13} />수정
            </button>
          ) : (
            <div className="flex gap-2">
              <button onClick={() => setEditingAccount(false)} className="text-xs text-gray-400 hover:text-navy">취소</button>
              <button onClick={saveAccount} className="text-xs font-semibold text-orange hover:underline">저장</button>
            </div>
          )
        }
      >
        {editingAccount ? (
          <div className="space-y-3">
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-1">은행</label>
              <div className="relative">
                <select value={accountForm.bank}
                  onChange={e => setAccountForm(p => ({ ...p, bank: e.target.value }))}
                  className="w-full appearance-none border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy bg-white">
                  {BANKS.map(b => <option key={b}>{b}</option>)}
                </select>
                <ChevronDown size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
              </div>
            </div>
            <div>
              <label className="block text-xs font-semibold text-gray-500 mb-1">계좌번호</label>
              <input value={accountForm.number}
                onChange={e => setAccountForm(p => ({ ...p, number: e.target.value }))}
                placeholder="000-000-0000000"
                className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy" />
            </div>
          </div>
        ) : account ? (
          <div className="flex items-center gap-4 p-4 bg-offwhite rounded-xl">
            <div className="w-10 h-10 rounded-xl bg-navy/10 flex items-center justify-center shrink-0">
              <Banknote size={18} className="text-navy" />
            </div>
            <div>
              <p className="text-xs text-gray-400 mb-0.5">등록된 계좌</p>
              <p className="text-sm font-bold text-navy">{account.bank}</p>
              <p className="text-xs text-gray-500 mt-0.5">{maskAccount(account.number)}</p>
            </div>
          </div>
        ) : (
          <div className="flex items-center gap-3 p-4 bg-offwhite rounded-xl text-gray-400">
            <Banknote size={18} className="text-gray-300 shrink-0" />
            <p className="text-sm">등록된 계좌가 없습니다. 수정 버튼을 눌러 계좌를 등록하세요.</p>
          </div>
        )}
      </SectionCard>

      {/* 필수 서류 */}
      <SectionCard title="필수 서류" icon={FileText}>
        <input ref={healthFileRef} type="file" accept="image/*,.pdf" className="hidden"
          onChange={e => { handleDocumentUpload('health', e.target.files?.[0]); e.target.value = '' }} />
        <input ref={safetyFileRef} type="file" accept="image/*,.pdf" className="hidden"
          onChange={e => { handleDocumentUpload('safety', e.target.files?.[0]); e.target.value = '' }} />
        <div className="space-y-3">
          {[
            { key: 'health', label: '보건증',          desc: '발급일로부터 1년 이내' },
            { key: 'safety', label: '안전교육 이수증', desc: '연간 1회 의무 교육' },
          ].map(doc => {
            const submitted = documents[doc.key]
            return (
              <div key={doc.key} className="flex items-center justify-between p-3 bg-offwhite rounded-xl">
                <div>
                  <p className="text-sm font-semibold text-navy">{doc.label}</p>
                  <p className="text-xs text-gray-400 mt-0.5">{doc.desc}</p>
                </div>
                <div className="flex items-center gap-3 shrink-0">
                  <span className={`text-xs font-bold px-2.5 py-1 rounded-full ${submitted ? 'bg-green-100 text-green-700' : 'bg-red-50 text-red-500'}`}>
                    {submitted ? '제출 완료' : '미제출'}
                  </span>
                  {submitted ? (
                    <button onClick={() => withdrawDocument(doc.key)}
                      className="flex items-center gap-1 text-xs font-semibold text-gray-500 border border-offwhite-200 hover:border-gray-400 px-3 py-1.5 rounded-lg transition-colors">
                      <X size={12} />철회
                    </button>
                  ) : (
                    <button onClick={() => docRefs[doc.key].current?.click()}
                      className="flex items-center gap-1 text-xs font-semibold bg-orange text-white hover:bg-orange-600 px-3 py-1.5 rounded-lg transition-colors">
                      <Upload size={12} />업로드
                    </button>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      </SectionCard>

      {/* 계정 관리 */}
      <div className="bg-white rounded-2xl border border-offwhite-200 p-5">
        <h2 className="font-bold text-navy mb-4">계정 관리</h2>
        <div className="space-y-2">
          <button onClick={() => setShowPwChange(true)}
            className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-offwhite transition-colors text-left">
            <Lock size={16} className="text-gray-400 shrink-0" />
            <div>
              <p className="text-sm font-medium text-navy">비밀번호 변경</p>
              <p className="text-xs text-gray-400">주기적으로 비밀번호를 변경하면 보안이 강화됩니다</p>
            </div>
          </button>
          <button onClick={handleLogout}
            className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-offwhite transition-colors text-left">
            <LogOut size={16} className="text-gray-400 shrink-0" />
            <div>
              <p className="text-sm font-medium text-navy">로그아웃</p>
              <p className="text-xs text-gray-400">현재 기기에서 로그아웃됩니다</p>
            </div>
          </button>
          <button onClick={() => setShowWithdraw(true)}
            className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-red-50 transition-colors text-left">
            <Trash2 size={16} className="text-red-400 shrink-0" />
            <div>
              <p className="text-sm font-medium text-red-500">회원 탈퇴</p>
              <p className="text-xs text-gray-400">탈퇴 후 모든 데이터가 삭제됩니다</p>
            </div>
          </button>
        </div>
      </div>

      {/* 모달: 디지털 이력서 */}
      {showResume && (
        <Modal onClose={() => setShowResume(false)}>
          <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
            <h3 className="font-bold text-navy">디지털 이력서</h3>
            <button onClick={() => setShowResume(false)} className="text-gray-400 hover:text-navy transition-colors">
              <X size={18} />
            </button>
          </div>
          <div className="p-5">
            <div className="flex items-center gap-3 mb-5 p-4 bg-offwhite rounded-xl">
              <div className="w-12 h-12 rounded-full overflow-hidden bg-orange flex items-center justify-center shrink-0">
                {savedPhotos.length > 0
                  ? <img src={savedPhotos[0].url} alt="" className="w-full h-full object-cover" />
                  : <span className="text-white text-xl font-bold">{user?.avatar}</span>
                }
              </div>
              <div>
                <p className="font-bold text-navy">{user?.name}</p>
                <p className="text-sm text-gray-500">{user?.email}</p>
              </div>
              {hasNoShowZeroBadge && (
                <span className="ml-auto text-[10px] font-bold bg-green-100 text-green-700 px-2 py-1 rounded-full">
                  노쇼 제로
                </span>
              )}
            </div>
            <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-3">경력 사항</h4>
            <p className="text-sm text-gray-400 text-center py-4">등록된 경력이 없습니다.</p>
          </div>
          <div className="px-5 pb-4">
            <button onClick={() => setShowResume(false)}
              className="w-full py-2.5 text-sm font-semibold text-gray-500 border border-offwhite-200 rounded-xl hover:border-navy transition-colors">
              닫기
            </button>
          </div>
        </Modal>
      )}

      {/* 모달: 비밀번호 변경 */}
      {showPwChange && (
        <Modal onClose={() => { setShowPwChange(false); setPwForm({ current: '', next: '', confirm: '' }); setPwError('') }}>
          <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
            <h3 className="font-bold text-navy">비밀번호 변경</h3>
            <button onClick={() => setShowPwChange(false)} className="text-gray-400 hover:text-navy transition-colors">
              <X size={18} />
            </button>
          </div>
          <div className="p-5 space-y-3">
            {[
              { key: 'current', label: '현재 비밀번호',    placeholder: '현재 비밀번호 입력' },
              { key: 'next',    label: '새 비밀번호',      placeholder: '8자 이상 입력' },
              { key: 'confirm', label: '새 비밀번호 확인', placeholder: '새 비밀번호 재입력' },
            ].map(f => (
              <div key={f.key}>
                <label className="block text-xs font-semibold text-gray-500 mb-1">{f.label}</label>
                <input type="password" value={pwForm[f.key]} placeholder={f.placeholder}
                  onChange={e => setPwForm(p => ({ ...p, [f.key]: e.target.value }))}
                  className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy" />
              </div>
            ))}
            {pwError && <p className="text-xs text-red-500">{pwError}</p>}
          </div>
          <div className="px-5 pb-5 flex gap-2">
            <button onClick={() => { setShowPwChange(false); setPwError('') }}
              className="flex-1 py-2.5 text-sm font-semibold text-gray-500 border border-offwhite-200 rounded-xl hover:border-navy transition-colors">
              취소
            </button>
            <button onClick={handlePwChange}
              className="flex-1 py-2.5 text-sm font-semibold text-white bg-orange rounded-xl hover:bg-orange-600 transition-colors">
              변경하기
            </button>
          </div>
        </Modal>
      )}

      {/* 모달: 회원 탈퇴 */}
      {showWithdraw && (
        <Modal onClose={() => setShowWithdraw(false)}>
          <div className="p-6 text-center">
            <div className="w-14 h-14 rounded-full bg-red-50 flex items-center justify-center mx-auto mb-4">
              <AlertTriangle size={28} className="text-red-500" />
            </div>
            <h3 className="text-lg font-bold text-navy mb-2">정말 탈퇴하시겠습니까?</h3>
            <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-4 mb-5 text-left">
              <p className="text-sm font-semibold text-yellow-800 mb-1.5">⚠️ 탈퇴 전 반드시 확인하세요</p>
              <ul className="text-xs text-yellow-700 space-y-1 list-disc list-inside">
                <li>정산되지 않은 금액이 있는지 확인하세요.</li>
                <li>탈퇴 후에는 계정 및 모든 데이터를 복구할 수 없습니다.</li>
              </ul>
            </div>
            <div className="flex gap-2">
              <button onClick={() => setShowWithdraw(false)}
                className="flex-1 py-2.5 text-sm font-semibold text-gray-600 border border-offwhite-200 rounded-xl hover:border-navy transition-colors">
                취소
              </button>
              <button onClick={handleWithdraw}
                className="flex-1 py-2.5 text-sm font-semibold text-white bg-red-500 rounded-xl hover:bg-red-600 transition-colors">
                탈퇴 진행
              </button>
            </div>
          </div>
        </Modal>
      )}

      {/* 모달: 완료한 업무 */}
      {statModal === 'jobs' && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setStatModal(null)}>
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 overflow-hidden flex flex-col max-h-[80vh]">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200 shrink-0">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-xl bg-blue-50 flex items-center justify-center">
                  <Briefcase size={16} className="text-blue-500" />
                </div>
                <h3 className="font-bold text-navy">완료한 업무 <span className="text-blue-500">{completedCount}건</span></h3>
              </div>
              <button onClick={() => setStatModal(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="overflow-y-auto flex-1">
              {completedCount === 0 ? (
                <p className="text-sm text-gray-400 text-center py-10">완료한 업무가 없습니다.</p>
              ) : (
                Object.entries(
                  completedApps.reduce((acc, app) => {
                    const co = app.jobPost?.companyName ?? '미상'
                    if (!acc[co]) acc[co] = []
                    acc[co].push(app)
                    return acc
                  }, {})
                ).map(([company, apps]) => (
                  <div key={company}>
                    <div className="px-5 py-2.5 bg-offwhite border-b border-offwhite-200 flex items-center justify-between">
                      <p className="text-xs font-bold text-navy">{company}</p>
                      <span className="text-xs text-gray-400">{apps.length}건</span>
                    </div>
                    {apps.map(app => (
                      <div key={app.id} className="px-5 py-3 border-b border-offwhite-200 flex items-center justify-between gap-4 hover:bg-offwhite-100 transition-colors">
                        <div>
                          <p className="text-sm font-medium text-navy">{app.jobPost?.title ?? '-'}</p>
                          <p className="text-xs text-gray-400 mt-0.5">{app.createdAt?.substring(0, 10)}</p>
                        </div>
                        <span className="text-sm font-semibold text-orange shrink-0">
                          {WAGE_LABEL[app.jobPost?.wageType] ?? '시급'} {(app.jobPost?.wageAmount ?? 0).toLocaleString()}원
                        </span>
                      </div>
                    ))}
                  </div>
                ))
              )}
            </div>
            <div className="px-5 py-3 bg-offwhite-100 border-t border-offwhite-200 shrink-0">
              <span className="text-xs text-gray-500">총 {completedCount}건</span>
            </div>
          </div>
        </div>
      )}

      {/* 모달: 평균 별점 */}
      {statModal === 'rating' && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setStatModal(null)}>
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 overflow-hidden flex flex-col max-h-[80vh]">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200 shrink-0">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-xl bg-yellow-50 flex items-center justify-center">
                  <Star size={16} className="text-yellow-500" />
                </div>
                <h3 className="font-bold text-navy">
                  기업별 평점
                  {avgRatingDisplay !== '-' && <span className="text-yellow-500 ml-1">평균 {avgRatingDisplay}</span>}
                </h3>
              </div>
              <button onClick={() => setStatModal(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="overflow-y-auto flex-1 divide-y divide-offwhite-200">
              {ratingItems.length === 0 ? (
                <p className="text-sm text-gray-400 text-center py-10">아직 받은 평가가 없습니다.</p>
              ) : [...ratingItems].sort((a, b) => b.stars - a.stars).map(r => (
                <div key={r.key} className="px-5 py-4 hover:bg-offwhite-100 transition-colors">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="text-sm font-bold text-navy">{r.title}</p>
                        <span className="text-xs text-gray-400">{r.date}</span>
                      </div>
                      <p className="text-xs text-gray-500 mt-1.5 leading-relaxed">{r.comment || '코멘트 없음'}</p>
                    </div>
                    <div className="flex items-center gap-1 shrink-0">
                      <Star size={14} className="text-yellow-400 fill-yellow-400" />
                      <span className="text-base font-extrabold text-navy tabular-nums">{r.stars.toFixed(1)}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            <div className="px-5 py-3 bg-offwhite-100 border-t border-offwhite-200 flex items-center justify-between shrink-0">
              <span className="text-xs text-gray-500">총 {ratingItems.length}개 평가 · 평균</span>
              <div className="flex items-center gap-1">
                <Star size={13} className="text-yellow-400 fill-yellow-400" />
                <span className="text-sm font-extrabold text-navy">{avgRatingDisplay}</span>
                <span className="text-xs text-gray-400">/ 5.0</span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 모달: 근태 지수 */}
      {statModal === 'attend' && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setStatModal(null)}>
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 overflow-hidden flex flex-col max-h-[80vh]">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200 shrink-0">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-xl bg-green-50 flex items-center justify-center">
                  <ShieldCheck size={16} className="text-green-600" />
                </div>
                <h3 className="font-bold text-navy">근태 기록</h3>
              </div>
              <button onClick={() => setStatModal(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="px-5 py-4 bg-green-50 border-b border-green-100 flex items-center gap-4 shrink-0">
              <div className="flex items-center gap-2">
                <ShieldCheck size={20} className="text-green-600" />
                <span className="text-sm font-bold text-green-700">노쇼 {noShowCount}회</span>
              </div>
              <span className="text-xs text-gray-500 bg-white border border-green-200 px-2.5 py-1 rounded-full">
                최근 {Math.min(totalRelevant, 20)}회 근무 중 {noShowCount}건 결근
              </span>
            </div>
            <div className="overflow-y-auto flex-1 divide-y divide-offwhite-200">
              {totalRelevant === 0 ? (
                <p className="text-sm text-gray-400 text-center py-10">근태 기록이 없습니다.</p>
              ) : (
                [...completedApps, ...noShowApps]
                  .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
                  .map(app => (
                    <div key={app.id} className="px-5 py-3 flex items-center justify-between hover:bg-offwhite-100 transition-colors">
                      <div>
                        <p className="text-sm font-medium text-navy">{app.jobPost?.companyName ?? '-'}</p>
                        <p className="text-xs text-gray-400 mt-0.5">{app.createdAt?.substring(0, 10)}</p>
                      </div>
                      <span className={`text-xs font-bold px-2.5 py-1 rounded-full ${
                        app.status === 'NO_SHOW' ? 'bg-red-100 text-red-600' : 'bg-green-100 text-green-700'
                      }`}>
                        {app.status === 'NO_SHOW' ? '노쇼' : '정상 출근'}
                      </span>
                    </div>
                  ))
              )}
            </div>
            <div className="px-5 py-3 bg-offwhite-100 border-t border-offwhite-200 flex items-center justify-between shrink-0">
              <span className="text-xs text-gray-500">총 {totalRelevant}건 기록</span>
              {totalRelevant > 0 && (
                <span className="text-xs font-bold text-green-600">
                  출근율 {Math.round((completedCount / totalRelevant) * 100)}%
                </span>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
