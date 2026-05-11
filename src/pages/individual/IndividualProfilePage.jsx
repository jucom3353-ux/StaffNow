import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  User, Mail, Phone, MapPin, Pencil, X, Plus, Check,
  Star, Clock, Briefcase, ShieldCheck, FileText, Banknote,
  Lock, LogOut, Trash2, ChevronDown, Upload, AlertTriangle,
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { useIndividualData } from '../../hooks/useIndividualData'

// ─── 하드코딩 데모 데이터 ────────────────────────────────────────────────────
const DEMO_STATS = [
  { key: 'jobs',   label: '완료한 업무', value: '23건',    icon: Briefcase,   color: 'text-blue-500',   bg: 'bg-blue-50' },
  { key: 'hours',  label: '총 근무 시간', value: '184시간', icon: Clock,       color: 'text-purple-500', bg: 'bg-purple-50' },
  { key: 'rating', label: '평균 별점',   value: '4.8',     icon: Star,        color: 'text-yellow-500', bg: 'bg-yellow-50', suffix: '/ 5.0' },
  { key: 'attend', label: '근태 지수',   value: '노쇼 0회', icon: ShieldCheck, color: 'text-green-600',  bg: 'bg-green-50', badge: true },
]

const CAREER_LIST = [
  { id: 1, company: '스타벅스 역삼점', role: '바리스타 파트타임', period: '2024.03 – 2024.12', type: '파트타임' },
  { id: 2, company: '코엑스 전시',     role: '행사 스태프',       period: '2024.01 – 2024.02', type: '단기' },
  { id: 3, company: '브랜드X',         role: '팝업스토어 운영',   period: '2023.10 – 2023.12', type: '단기' },
  { id: 4, company: '마케팅 에이전시', role: '홍보 활동',         period: '2023.09 – 2023.09', type: '프리랜서' },
]

const DEMO_COMPLETED_JOBS = [
  { id: 1,  company: '스타벅스 역삼점',  role: '바리스타 파트타임',    date: '2024.12.20', hours: 8,  pay: 80000 },
  { id: 2,  company: '스타벅스 역삼점',  role: '바리스타 파트타임',    date: '2024.12.14', hours: 8,  pay: 80000 },
  { id: 3,  company: '스타벅스 역삼점',  role: '바리스타 파트타임',    date: '2024.12.07', hours: 8,  pay: 80000 },
  { id: 4,  company: '코엑스 전시',      role: '행사 스태프',          date: '2024.02.03', hours: 10, pay: 120000 },
  { id: 5,  company: '코엑스 전시',      role: '행사 스태프',          date: '2024.02.04', hours: 10, pay: 120000 },
  { id: 6,  company: '코엑스 전시',      role: '행사 스태프',          date: '2024.01.27', hours: 8,  pay: 96000 },
  { id: 7,  company: '브랜드X',          role: '팝업스토어 운영',      date: '2023.12.10', hours: 9,  pay: 108000 },
  { id: 8,  company: '브랜드X',          role: '팝업스토어 운영',      date: '2023.12.03', hours: 9,  pay: 108000 },
  { id: 9,  company: '브랜드X',          role: '팝업스토어 운영',      date: '2023.11.26', hours: 9,  pay: 108000 },
  { id: 10, company: '마케팅 에이전시',  role: '홍보 활동',            date: '2023.09.15', hours: 6,  pay: 72000 },
  { id: 11, company: '스타벅스 역삼점',  role: '바리스타 파트타임',    date: '2024.11.30', hours: 8,  pay: 80000 },
  { id: 12, company: '스타벅스 역삼점',  role: '바리스타 파트타임',    date: '2024.11.23', hours: 8,  pay: 80000 },
  { id: 13, company: '강남 행사 스태프', role: '행사 진행 보조',       date: '2024.10.05', hours: 7,  pay: 84000 },
  { id: 14, company: '강남 행사 스태프', role: '행사 진행 보조',       date: '2024.09.28', hours: 7,  pay: 84000 },
  { id: 15, company: '편의점 GS25',      role: '야간 파트타임',        date: '2024.08.10', hours: 8,  pay: 76000 },
  { id: 16, company: '편의점 GS25',      role: '야간 파트타임',        date: '2024.08.03', hours: 8,  pay: 76000 },
  { id: 17, company: '편의점 GS25',      role: '야간 파트타임',        date: '2024.07.27', hours: 8,  pay: 76000 },
  { id: 18, company: '물류창고 A',       role: '입출고 작업',          date: '2024.06.15', hours: 9,  pay: 108000 },
  { id: 19, company: '물류창고 A',       role: '입출고 작업',          date: '2024.06.08', hours: 9,  pay: 108000 },
  { id: 20, company: '카페 봄날',        role: '카운터 파트타임',      date: '2024.05.18', hours: 7,  pay: 70000 },
  { id: 21, company: '카페 봄날',        role: '카운터 파트타임',      date: '2024.05.11', hours: 7,  pay: 70000 },
  { id: 22, company: '현대백화점',       role: '판촉 행사 스태프',     date: '2024.04.20', hours: 8,  pay: 112000 },
  { id: 23, company: '현대백화점',       role: '판촉 행사 스태프',     date: '2024.04.13', hours: 8,  pay: 112000 },
]

const DEMO_RATINGS = [
  { company: '스타벅스 역삼점',  rating: 4.9, comment: '친절하고 빠른 업무 처리, 적극적인 태도가 인상적이었습니다.',    date: '2024.12.22' },
  { company: '코엑스 전시',      rating: 4.7, comment: '지시 사항을 잘 이해하고 성실하게 임했습니다.',                  date: '2024.02.05' },
  { company: '브랜드X',          rating: 4.8, comment: '고객 응대가 훌륭했고 팀워크도 좋았습니다.',                    date: '2023.12.12' },
  { company: '마케팅 에이전시',  rating: 4.6, comment: '홍보 활동에 적극적으로 참여하여 목표를 달성하였습니다.',        date: '2023.09.16' },
  { company: '강남 행사 스태프', rating: 5.0, comment: '매우 뛰어난 서비스 마인드로 행사를 성공적으로 진행했습니다.',  date: '2024.10.06' },
  { company: '편의점 GS25',      rating: 4.5, comment: '야간 근무임에도 성실하게 임했습니다.',                        date: '2024.08.12' },
  { company: '물류창고 A',       rating: 4.7, comment: '작업 속도가 빠르고 정확하게 업무를 수행했습니다.',              date: '2024.06.16' },
  { company: '카페 봄날',        rating: 4.8, comment: '밝은 미소와 친절한 서비스가 손님들에게 호응이 좋았습니다.',    date: '2024.05.19' },
  { company: '현대백화점',       rating: 5.0, comment: '행사 스태프로서 완벽한 역할을 수행했습니다. 재고용 의사 있음.', date: '2024.04.21' },
]

const DEMO_ATTENDANCE = [
  { date: '2024.12.20', company: '스타벅스 역삼점',  status: '정상 출근' },
  { date: '2024.12.14', company: '스타벅스 역삼점',  status: '정상 출근' },
  { date: '2024.12.07', company: '스타벅스 역삼점',  status: '정상 출근' },
  { date: '2024.11.30', company: '스타벅스 역삼점',  status: '정상 출근' },
  { date: '2024.11.23', company: '스타벅스 역삼점',  status: '정상 출근' },
  { date: '2024.10.06', company: '강남 행사 스태프', status: '정상 출근' },
  { date: '2024.09.28', company: '강남 행사 스태프', status: '정상 출근' },
  { date: '2024.08.10', company: '편의점 GS25',      status: '정상 출근' },
  { date: '2024.08.03', company: '편의점 GS25',      status: '정상 출근' },
  { date: '2024.07.27', company: '편의점 GS25',      status: '정상 출근' },
  { date: '2024.06.15', company: '물류창고 A',       status: '정상 출근' },
  { date: '2024.06.08', company: '물류창고 A',       status: '정상 출근' },
  { date: '2024.05.18', company: '카페 봄날',        status: '정상 출근' },
  { date: '2024.05.11', company: '카페 봄날',        status: '정상 출근' },
  { date: '2024.04.20', company: '현대백화점',       status: '정상 출근' },
  { date: '2024.04.13', company: '현대백화점',       status: '정상 출근' },
  { date: '2024.02.04', company: '코엑스 전시',      status: '정상 출근' },
  { date: '2024.02.03', company: '코엑스 전시',      status: '정상 출근' },
  { date: '2024.01.27', company: '코엑스 전시',      status: '정상 출근' },
  { date: '2023.12.10', company: '브랜드X',          status: '정상 출근' },
]

const BANKS = ['국민은행', '신한은행', '하나은행', '우리은행', 'IBK기업은행', 'NH농협은행', 'SC제일은행', '카카오뱅크', '토스뱅크']

const TIME_OPTIONS = ['주말', '평일', '새벽', '오전', '오후', '저녁']

// ─── 행정구역 데이터 ─────────────────────────────────────────────────────────
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
  '파트타임':   'bg-blue-50 text-blue-600',
  '단기':       'bg-orange-50 text-orange',
  '프리랜서':   'bg-purple-50 text-purple-600',
}

// ─── 모달 공통 래퍼 ─────────────────────────────────────────────────────────
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

// ─── 섹션 카드 래퍼 ─────────────────────────────────────────────────────────
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

// ─── 메인 컴포넌트 ──────────────────────────────────────────────────────────
export default function IndividualProfilePage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const { profile, updateProfile } = useIndividualData()
  const healthFileRef = useRef(null)
  const safetyFileRef = useRef(null)
  const docRefs = { health: healthFileRef, safety: safetyFileRef }

  // ── 토스트 ──
  const [toast, setToast] = useState('')
  function showToast(msg) {
    setToast(msg)
    setTimeout(() => setToast(''), 3000)
  }

  // ── 모달 상태 ──
  const [showResume,   setShowResume]   = useState(false)
  const [showPwChange, setShowPwChange] = useState(false)
  const [showWithdraw, setShowWithdraw] = useState(false)
  const [statModal,    setStatModal]    = useState(null) // 'jobs' | 'hours' | 'rating' | 'attend'

  // ── 프로필 편집 ──
  const [editingProfile, setEditingProfile] = useState(false)
  const [profileForm, setProfileForm]       = useState(null)
  const [skillInput, setSkillInput]         = useState('')

  function openProfileEdit() {
    setProfileForm({
      phone:   profile.phone   || '',
      address: profile.address || '',
      bio:     profile.bio     || '',
      skills:  [...(profile.skills?.length ? profile.skills : ['행사 진행', '고객 응대', '엑셀'])],
    })
    setEditingProfile(true)
  }

  function saveProfile() {
    updateProfile(profileForm)
    setEditingProfile(false)
    showToast('프로필이 저장되었습니다.')
  }

  function addSkill() {
    const s = skillInput.trim()
    if (s && !profileForm.skills.includes(s)) {
      setProfileForm(p => ({ ...p, skills: [...p.skills, s] }))
    }
    setSkillInput('')
  }

  // ── 활동 가능 지역 ──
  const regions = profile.regions?.length ? profile.regions : ['서울 강남구', '서울 서초구', '경기 성남시']
  const [showRegionInput, setShowRegionInput] = useState(false)
  const [regionSido,      setRegionSido]      = useState('')
  const [regionGungu,     setRegionGungu]     = useState('')

  function addRegion() {
    if (!regionSido || !regionGungu) return
    const r = `${regionSido} ${regionGungu}`
    if (!regions.includes(r)) {
      updateProfile({ regions: [...regions, r] })
    }
    setRegionSido('')
    setRegionGungu('')
    setShowRegionInput(false)
  }

  function removeRegion(r) {
    updateProfile({ regions: regions.filter(x => x !== r) })
  }

  // ── 선호 업무 시간 ──
  const preferredTimes = profile.preferredTimes?.length ? profile.preferredTimes : ['주말', '오후']

  function toggleTime(t) {
    const next = preferredTimes.includes(t)
      ? preferredTimes.filter(x => x !== t)
      : [...preferredTimes, t]
    updateProfile({ preferredTimes: next })
  }

  // ── 정산 계좌 ──
  const account = profile.account ?? { bank: '국민은행', number: '123-456-7890123' }
  const [editingAccount, setEditingAccount] = useState(false)
  const [accountForm,    setAccountForm]    = useState(account)

  function saveAccount() {
    updateProfile({ account: accountForm })
    setEditingAccount(false)
    showToast('계좌 정보가 저장되었습니다.')
  }

  // ── 필수 서류 ──
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

  // ── 비밀번호 변경 ──
  const [pwForm, setPwForm] = useState({ current: '', next: '', confirm: '' })
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

  // ── 로그아웃 ──
  function handleLogout() {
    logout()
    navigate('/login')
  }

  // ── 회원 탈퇴 ──
  function handleWithdraw() {
    logout()
    navigate('/login')
  }

  const displaySkills = editingProfile
    ? profileForm.skills
    : (profile.skills?.length ? profile.skills : ['행사 진행', '고객 응대', '엑셀'])

  // ── 마스킹 계좌번호 ──
  function maskAccount(num) {
    if (!num) return '-'
    const parts = num.split('-')
    if (parts.length >= 2) {
      return parts.map((p, i) => (i === parts.length - 1 ? '****' : p)).join('-')
    }
    return num.slice(0, -4).replace(/./g, '*') + num.slice(-4)
  }

  return (
    <div className="space-y-5 max-w-2xl pb-10">

      {/* ── 토스트 ── */}
      {toast && (
        <div className="fixed top-5 left-1/2 -translate-x-1/2 z-50 bg-navy text-white text-sm font-medium px-5 py-2.5 rounded-xl shadow-lg transition-all">
          {toast}
        </div>
      )}

      {/* ── 페이지 헤더 ── */}
      <h1 className="text-2xl font-bold text-navy">내 프로필</h1>

      {/* ── 1. Stats 카드 ── */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {DEMO_STATS.map(s => (
          <button
            key={s.label}
            onClick={() => setStatModal(s.key)}
            className="bg-white rounded-2xl border border-offwhite-200 p-4 text-left hover:border-navy hover:shadow-sm transition-all group"
          >
            <div className={`w-9 h-9 rounded-xl ${s.bg} flex items-center justify-center mb-3`}>
              <s.icon size={18} className={s.color} />
            </div>
            <p className="text-xl font-extrabold text-navy tabular-nums">
              {s.value}
              {s.suffix && <span className="text-xs font-normal text-gray-400 ml-1">{s.suffix}</span>}
            </p>
            <p className="text-xs text-gray-500 mt-0.5 group-hover:text-navy transition-colors">{s.label}</p>
            {s.badge && (
              <span className="inline-block mt-1.5 text-[10px] font-bold bg-green-100 text-green-700 px-2 py-0.5 rounded-full">
                최근 20회 중 0건
              </span>
            )}
          </button>
        ))}
      </div>

      {/* ── 2. 프로필 카드 ── */}
      <div className="bg-white rounded-2xl border border-offwhite-200 p-5">
        {/* 헤더 */}
        <div className="flex items-start justify-between gap-3 mb-5">
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-full bg-orange flex items-center justify-center shrink-0">
              <span className="text-white text-2xl font-bold">{user?.avatar}</span>
            </div>
            <div>
              <div className="flex items-center gap-2 flex-wrap">
                <h2 className="text-lg font-bold text-navy">{user?.name}</h2>
                <span className="text-[10px] font-bold bg-green-100 text-green-700 px-2 py-0.5 rounded-full flex items-center gap-1">
                  <ShieldCheck size={10} />노쇼 제로
                </span>
              </div>
              <p className="text-sm text-orange font-medium mt-0.5">{user?.roleLabel}</p>
            </div>
          </div>

          <div className="text-right shrink-0">
            <p className="text-[10px] text-gray-400">정산 대기 중</p>
            <p className="text-base font-extrabold text-orange">120,033원</p>
          </div>
        </div>

        {/* 수정 버튼 행 */}
        <div className="flex items-center justify-between mb-4">
          <button
            onClick={() => setShowResume(true)}
            className="flex items-center gap-1.5 text-xs font-semibold text-navy border border-offwhite-200 px-3 py-1.5 rounded-lg hover:border-navy transition-colors"
          >
            <FileText size={13} />디지털 이력서 확인
          </button>

          {!editingProfile ? (
            <button
              onClick={openProfileEdit}
              className="flex items-center gap-1.5 text-sm font-semibold text-orange hover:underline"
            >
              <Pencil size={13} />수정
            </button>
          ) : (
            <div className="flex gap-2">
              <button
                onClick={() => setEditingProfile(false)}
                className="flex items-center gap-1 text-xs text-gray-500 px-3 py-1.5 rounded-lg border border-offwhite-200 hover:border-navy transition-colors"
              >
                <X size={12} />취소
              </button>
              <button
                onClick={saveProfile}
                className="flex items-center gap-1 text-xs font-semibold text-white bg-orange px-3 py-1.5 rounded-lg hover:bg-orange-600 transition-colors"
              >
                <Check size={12} />저장
              </button>
            </div>
          )}
        </div>

        {/* 정보 필드 */}
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

      {/* ── 3. 보유 스킬 ── */}
      <SectionCard title="보유 스킬" icon={Star}>
        <div className="flex flex-wrap gap-2 mb-3">
          {displaySkills.map(s => (
            <span key={s} className="flex items-center gap-1 text-sm bg-offwhite px-3 py-1.5 rounded-full text-gray-600 border border-offwhite-200">
              {s}
              {editingProfile && (
                <button onClick={() => setProfileForm(p => ({ ...p, skills: p.skills.filter(x => x !== s) }))}
                  className="hover:text-red-500 transition-colors ml-0.5">
                  <X size={11} />
                </button>
              )}
            </span>
          ))}
          {displaySkills.length === 0 && !editingProfile && (
            <p className="text-sm text-gray-400">등록된 스킬이 없습니다.</p>
          )}
        </div>
        {editingProfile && (
          <div className="flex gap-2">
            <input type="text" placeholder="스킬 추가" value={skillInput}
              onChange={e => setSkillInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && addSkill()}
              className="flex-1 border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy" />
            <button onClick={addSkill}
              className="flex items-center gap-1 px-3 py-2 bg-navy text-white text-sm font-semibold rounded-lg hover:bg-navy-700 transition-colors">
              <Plus size={13} />추가
            </button>
          </div>
        )}
      </SectionCard>

      {/* ── 4. 활동 가능 지역 ── */}
      <SectionCard title="활동 가능 지역" icon={MapPin}
        action={
          <button onClick={() => setShowRegionInput(v => !v)}
            className="flex items-center gap-1 text-xs font-semibold text-orange hover:underline">
            <Plus size={13} />추가
          </button>
        }
      >
        <div className="flex flex-wrap gap-2 mb-3">
          {regions.map(r => (
            <span key={r} className="flex items-center gap-1 text-sm bg-navy/5 text-navy px-3 py-1.5 rounded-full border border-navy/10">
              <MapPin size={11} className="text-orange" />{r}
              <button onClick={() => removeRegion(r)} className="hover:text-red-500 ml-0.5 transition-colors">
                <X size={11} />
              </button>
            </span>
          ))}
        </div>
        {showRegionInput && (
          <div className="mt-2 space-y-2">
            <div className="flex gap-2">
              {/* 시/도 */}
              <div className="relative flex-1">
                <select
                  value={regionSido}
                  onChange={e => { setRegionSido(e.target.value); setRegionGungu('') }}
                  className="w-full appearance-none border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy bg-white text-navy"
                >
                  <option value="">시/도 선택</option>
                  {Object.keys(REGIONS).map(s => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
                <ChevronDown size={13} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
              </div>

              {/* 구/군 */}
              <div className="relative flex-1">
                <select
                  value={regionGungu}
                  onChange={e => setRegionGungu(e.target.value)}
                  disabled={!regionSido}
                  className="w-full appearance-none border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy bg-white text-navy disabled:text-gray-300 disabled:bg-offwhite"
                >
                  <option value="">구/군 선택</option>
                  {(REGIONS[regionSido] ?? []).map(g => (
                    <option key={g} value={g}>{g}</option>
                  ))}
                </select>
                <ChevronDown size={13} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
              </div>
            </div>

            <div className="flex gap-2">
              <button
                onClick={addRegion}
                disabled={!regionSido || !regionGungu}
                className="px-4 py-2 bg-navy text-white text-sm font-semibold rounded-lg hover:bg-navy/80 disabled:opacity-40 transition-colors"
              >
                추가
              </button>
              <button
                onClick={() => { setShowRegionInput(false); setRegionSido(''); setRegionGungu('') }}
                className="px-4 py-2 text-sm text-gray-400 rounded-lg border border-offwhite-200 hover:border-gray-400 transition-colors"
              >
                취소
              </button>
            </div>
          </div>
        )}
      </SectionCard>

      {/* ── 5. 선호 업무 시간 ── */}
      <SectionCard title="선호 업무 시간" icon={Clock}>
        <div className="flex flex-wrap gap-2">
          {TIME_OPTIONS.map(t => {
            const selected = preferredTimes.includes(t)
            return (
              <button key={t} onClick={() => toggleTime(t)}
                className={`text-sm px-4 py-2 rounded-full border font-medium transition-all ${
                  selected
                    ? 'bg-orange text-white border-orange'
                    : 'bg-white text-gray-500 border-offwhite-200 hover:border-orange hover:text-orange'
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

      {/* ── 6. 정산 계좌 ── */}
      <SectionCard title="정산 계좌" icon={Banknote}
        action={
          !editingAccount ? (
            <button onClick={() => { setAccountForm(account); setEditingAccount(true) }}
              className="flex items-center gap-1 text-xs font-semibold text-orange hover:underline">
              <Pencil size={13} />수정
            </button>
          ) : (
            <div className="flex gap-2">
              <button onClick={() => setEditingAccount(false)}
                className="text-xs text-gray-400 hover:text-navy">취소</button>
              <button onClick={saveAccount}
                className="text-xs font-semibold text-orange hover:underline">저장</button>
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
        ) : (
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
        )}
      </SectionCard>

      {/* ── 7. 필수 서류 ── */}
      <SectionCard title="필수 서류" icon={FileText}>
        {/* 숨김 파일 입력 */}
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
                  <span className={`text-xs font-bold px-2.5 py-1 rounded-full ${
                    submitted ? 'bg-green-100 text-green-700' : 'bg-red-50 text-red-500'
                  }`}>
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

      {/* ── 8. 계정 관리 ── */}
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

      {/* ── 모달: 디지털 이력서 ── */}
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
              <div className="w-12 h-12 rounded-full bg-orange flex items-center justify-center shrink-0">
                <span className="text-white text-xl font-bold">{user?.avatar}</span>
              </div>
              <div>
                <p className="font-bold text-navy">{user?.name}</p>
                <p className="text-sm text-gray-500">{user?.email}</p>
              </div>
              <span className="ml-auto text-[10px] font-bold bg-green-100 text-green-700 px-2 py-1 rounded-full">
                노쇼 제로
              </span>
            </div>

            <h4 className="text-xs font-semibold text-gray-500 uppercase tracking-wide mb-3">경력 사항</h4>
            <div className="space-y-3">
              {CAREER_LIST.map(c => (
                <div key={c.id} className="flex items-start gap-3 p-3 border border-offwhite-200 rounded-xl hover:border-navy/20 transition-colors">
                  <div className="w-8 h-8 rounded-lg bg-navy/10 flex items-center justify-center shrink-0 mt-0.5">
                    <Briefcase size={14} className="text-navy" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold text-navy">{c.role}</p>
                    <p className="text-xs text-gray-500 mt-0.5">{c.company}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{c.period}</p>
                  </div>
                  <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full shrink-0 mt-0.5 ${TYPE_COLOR[c.type] || 'bg-gray-100 text-gray-500'}`}>
                    {c.type}
                  </span>
                </div>
              ))}
            </div>
          </div>
          <div className="px-5 pb-4">
            <button onClick={() => setShowResume(false)}
              className="w-full py-2.5 text-sm font-semibold text-gray-500 border border-offwhite-200 rounded-xl hover:border-navy transition-colors">
              닫기
            </button>
          </div>
        </Modal>
      )}

      {/* ── 모달: 비밀번호 변경 ── */}
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
              { key: 'current', label: '현재 비밀번호', placeholder: '현재 비밀번호 입력' },
              { key: 'next',    label: '새 비밀번호',   placeholder: '8자 이상 입력' },
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

      {/* ── 모달: 회원 탈퇴 ── */}
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
                <li>현재 정산 대기 중인 금액: <span className="font-bold">120,033원</span></li>
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

      {/* ── 모달: 완료한 업무 ── */}
      {statModal === 'jobs' && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setStatModal(null)}>
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 overflow-hidden flex flex-col max-h-[80vh]">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200 shrink-0">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-xl bg-blue-50 flex items-center justify-center">
                  <Briefcase size={16} className="text-blue-500" />
                </div>
                <h3 className="font-bold text-navy">완료한 업무 <span className="text-blue-500">23건</span></h3>
              </div>
              <button onClick={() => setStatModal(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="overflow-y-auto flex-1">
              {Object.entries(
                DEMO_COMPLETED_JOBS.reduce((acc, j) => {
                  if (!acc[j.company]) acc[j.company] = []
                  acc[j.company].push(j)
                  return acc
                }, {})
              ).map(([company, jobs]) => (
                <div key={company}>
                  <div className="px-5 py-2.5 bg-offwhite border-b border-offwhite-200 flex items-center justify-between">
                    <p className="text-xs font-bold text-navy">{company}</p>
                    <span className="text-xs text-gray-400">{jobs.length}건</span>
                  </div>
                  {jobs.map(j => (
                    <div key={j.id} className="px-5 py-3 border-b border-offwhite-200 flex items-center justify-between gap-4 hover:bg-offwhite-100 transition-colors">
                      <div>
                        <p className="text-sm font-medium text-navy">{j.role}</p>
                        <p className="text-xs text-gray-400 mt-0.5">{j.date} · {j.hours}시간</p>
                      </div>
                      <span className="text-sm font-semibold text-orange shrink-0">
                        {j.pay.toLocaleString()}원
                      </span>
                    </div>
                  ))}
                </div>
              ))}
            </div>
            <div className="px-5 py-3 bg-offwhite-100 border-t border-offwhite-200 flex items-center justify-between shrink-0">
              <span className="text-xs text-gray-500">총 수입</span>
              <span className="text-sm font-extrabold text-navy">
                {DEMO_COMPLETED_JOBS.reduce((s, j) => s + j.pay, 0).toLocaleString()}원
              </span>
            </div>
          </div>
        </div>
      )}

      {/* ── 모달: 총 근무 시간 ── */}
      {statModal === 'hours' && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setStatModal(null)}>
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 overflow-hidden flex flex-col max-h-[80vh]">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200 shrink-0">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-xl bg-purple-50 flex items-center justify-center">
                  <Clock size={16} className="text-purple-500" />
                </div>
                <h3 className="font-bold text-navy">총 근무 시간 <span className="text-purple-500">184시간</span></h3>
              </div>
              <button onClick={() => setStatModal(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="overflow-y-auto flex-1 p-5 space-y-3">
              {Object.entries(
                DEMO_COMPLETED_JOBS.reduce((acc, j) => {
                  if (!acc[j.company]) acc[j.company] = 0
                  acc[j.company] += j.hours
                  return acc
                }, {})
              )
                .sort((a, b) => b[1] - a[1])
                .map(([company, hours]) => {
                  const pct = Math.round((hours / 184) * 100)
                  return (
                    <div key={company}>
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-sm font-medium text-navy">{company}</span>
                        <span className="text-sm font-bold text-purple-600">{hours}시간</span>
                      </div>
                      <div className="h-2 bg-offwhite rounded-full overflow-hidden">
                        <div className="h-full bg-purple-400 rounded-full transition-all" style={{ width: `${pct}%` }} />
                      </div>
                      <p className="text-[10px] text-gray-400 mt-0.5 text-right">{pct}%</p>
                    </div>
                  )
                })}
            </div>
            <div className="px-5 py-3 bg-offwhite-100 border-t border-offwhite-200 flex items-center justify-between shrink-0">
              <span className="text-xs text-gray-500">총 근무 시간</span>
              <span className="text-sm font-extrabold text-navy">184시간</span>
            </div>
          </div>
        </div>
      )}

      {/* ── 모달: 평균 별점 ── */}
      {statModal === 'rating' && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setStatModal(null)}>
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 overflow-hidden flex flex-col max-h-[80vh]">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200 shrink-0">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-xl bg-yellow-50 flex items-center justify-center">
                  <Star size={16} className="text-yellow-500" />
                </div>
                <h3 className="font-bold text-navy">기업별 평점 <span className="text-yellow-500">평균 4.8</span></h3>
              </div>
              <button onClick={() => setStatModal(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="overflow-y-auto flex-1 divide-y divide-offwhite-200">
              {DEMO_RATINGS.sort((a, b) => b.rating - a.rating).map(r => (
                <div key={r.company} className="px-5 py-4 hover:bg-offwhite-100 transition-colors">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="text-sm font-bold text-navy">{r.company}</p>
                        <span className="text-xs text-gray-400">{r.date}</span>
                      </div>
                      <p className="text-xs text-gray-500 mt-1.5 leading-relaxed">{r.comment}</p>
                    </div>
                    <div className="flex items-center gap-1 shrink-0">
                      <Star size={14} className="text-yellow-400 fill-yellow-400" />
                      <span className="text-base font-extrabold text-navy tabular-nums">{r.rating.toFixed(1)}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            <div className="px-5 py-3 bg-offwhite-100 border-t border-offwhite-200 flex items-center justify-between shrink-0">
              <span className="text-xs text-gray-500">총 {DEMO_RATINGS.length}개 평가 · 평균</span>
              <div className="flex items-center gap-1">
                <Star size={13} className="text-yellow-400 fill-yellow-400" />
                <span className="text-sm font-extrabold text-navy">
                  {(DEMO_RATINGS.reduce((s, r) => s + r.rating, 0) / DEMO_RATINGS.length).toFixed(1)}
                </span>
                <span className="text-xs text-gray-400">/ 5.0</span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ── 모달: 근태 지수 ── */}
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
                <span className="text-sm font-bold text-green-700">노쇼 0회</span>
              </div>
              <span className="text-xs text-gray-500 bg-white border border-green-200 px-2.5 py-1 rounded-full">
                최근 20회 근무 중 0건 결근
              </span>
            </div>
            <div className="overflow-y-auto flex-1 divide-y divide-offwhite-200">
              {DEMO_ATTENDANCE.map((a, i) => (
                <div key={i} className="px-5 py-3 flex items-center justify-between hover:bg-offwhite-100 transition-colors">
                  <div>
                    <p className="text-sm font-medium text-navy">{a.company}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{a.date}</p>
                  </div>
                  <span className="text-xs font-bold bg-green-100 text-green-700 px-2.5 py-1 rounded-full">
                    {a.status}
                  </span>
                </div>
              ))}
            </div>
            <div className="px-5 py-3 bg-offwhite-100 border-t border-offwhite-200 flex items-center justify-between shrink-0">
              <span className="text-xs text-gray-500">총 {DEMO_ATTENDANCE.length}건 기록</span>
              <span className="text-xs font-bold text-green-600">출근율 100%</span>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
