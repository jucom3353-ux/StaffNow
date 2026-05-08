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
  { label: '완료한 업무', value: '23건',    icon: Briefcase, color: 'text-blue-500',  bg: 'bg-blue-50' },
  { label: '총 근무 시간', value: '184시간', icon: Clock,     color: 'text-purple-500', bg: 'bg-purple-50' },
  { label: '평균 별점',   value: '4.8',     icon: Star,      color: 'text-yellow-500', bg: 'bg-yellow-50', suffix: '/ 5.0' },
  { label: '근태 지수',   value: '노쇼 0회', icon: ShieldCheck, color: 'text-green-600', bg: 'bg-green-50', badge: true },
]

const CAREER_LIST = [
  { id: 1, company: '스타벅스 역삼점', role: '바리스타 파트타임', period: '2024.03 – 2024.12', type: '파트타임' },
  { id: 2, company: '코엑스 전시',     role: '행사 스태프',       period: '2024.01 – 2024.02', type: '단기' },
  { id: 3, company: '브랜드X',         role: '팝업스토어 운영',   period: '2023.10 – 2023.12', type: '단기' },
  { id: 4, company: '마케팅 에이전시', role: '홍보 활동',         period: '2023.09 – 2023.09', type: '프리랜서' },
]

const BANKS = ['국민은행', '신한은행', '하나은행', '우리은행', 'IBK기업은행', 'NH농협은행', 'SC제일은행', '카카오뱅크', '토스뱅크']

const TIME_OPTIONS = ['주말', '평일', '새벽', '오전', '오후', '저녁']
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
  const [regionInput,     setRegionInput]     = useState('')

  function addRegion() {
    const r = regionInput.trim()
    if (r && !regions.includes(r)) {
      updateProfile({ regions: [...regions, r] })
    }
    setRegionInput('')
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
          <div key={s.label} className="bg-white rounded-2xl border border-offwhite-200 p-4">
            <div className={`w-9 h-9 rounded-xl ${s.bg} flex items-center justify-center mb-3`}>
              <s.icon size={18} className={s.color} />
            </div>
            <p className="text-xl font-extrabold text-navy tabular-nums">
              {s.value}
              {s.suffix && <span className="text-xs font-normal text-gray-400 ml-1">{s.suffix}</span>}
            </p>
            <p className="text-xs text-gray-500 mt-0.5">{s.label}</p>
            {s.badge && (
              <span className="inline-block mt-1.5 text-[10px] font-bold bg-green-100 text-green-700 px-2 py-0.5 rounded-full">
                최근 20회 중 0건
              </span>
            )}
          </div>
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
          <div className="flex gap-2 mt-2">
            <input type="text" placeholder="예: 서울 마포구" value={regionInput}
              onChange={e => setRegionInput(e.target.value)}
              onKeyDown={e => { if (e.key === 'Enter') addRegion() }}
              autoFocus
              className="flex-1 border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy" />
            <button onClick={addRegion}
              className="px-3 py-2 bg-navy text-white text-sm font-semibold rounded-lg hover:bg-navy-700 transition-colors">
              추가
            </button>
            <button onClick={() => setShowRegionInput(false)}
              className="px-3 py-2 text-sm text-gray-400 rounded-lg border border-offwhite-200 hover:border-gray-400 transition-colors">
              취소
            </button>
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
    </div>
  )
}
