import { useState } from 'react'
import { RotateCcw, AlertTriangle, CheckCircle, User, CreditCard, Star, Plus, X, Building2, Lock, Eye, EyeOff } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'

const SETTINGS_KEY = 'staffnow_company_settings'
const MOCK_PREFERRED_TAGS = ['활발함', '인근 거주자', '장기 근무 가능', '행사 경험자', '외국어 가능', '야간 근무 가능']

function loadSettings(user) {
  try {
    const saved = localStorage.getItem(SETTINGS_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      // 최초 저장 시 user 데이터로 빈 필드 채우기
      return {
        ...parsed,
        companyName: parsed.companyName || user?.company || '',
        bizNumber:   parsed.bizNumber   || user?.bizNumber || '',
        address:     parsed.address     || user?.address || '',
        addressDetail: parsed.addressDetail || user?.addressDetail || '',
        manager: {
          name:  parsed.manager?.name  || user?.name  || '',
          title: parsed.manager?.title || '',
          phone: parsed.manager?.phone || user?.phone || '',
          email: parsed.manager?.email || user?.email || '',
        },
        card: parsed.card || null,
        tags: parsed.tags || ['활발함', '인근 거주자', '행사 경험자'],
      }
    }
  } catch {}
  return {
    companyName:   user?.company || '',
    bizNumber:     user?.bizNumber || '',
    address:       user?.address || '',
    addressDetail: user?.addressDetail || '',
    manager: {
      name:  user?.name  || '',
      title: '',
      phone: user?.phone || '',
      email: user?.email || '',
    },
    card: null,
    tags: ['활발함', '인근 거주자', '행사 경험자'],
  }
}

function formatCardNumber(v) {
  const digits = v.replace(/\D/g, '').slice(0, 16)
  return digits.replace(/(.{4})/g, '$1-').replace(/-$/, '')
}

function CardRegisterModal({ current, onClose, onSave }) {
  const [cardNumber, setCardNumber] = useState('')
  const [cvc, setCvc]               = useState('')
  const [pw2, setPw2]               = useState('')
  const [showCvc, setShowCvc]       = useState(false)
  const [showPw, setShowPw]         = useState(false)
  const [loading, setLoading]       = useState(false)
  const [done, setDone]             = useState(false)
  const [errors, setErrors]         = useState({})

  function validate() {
    const e = {}
    if (cardNumber.replace(/\D/g, '').length < 16) e.cardNumber = '카드번호 16자리를 입력해주세요'
    if (cvc.length < 3) e.cvc = 'CVC 3자리를 입력해주세요'
    if (pw2.length < 2) e.pw2 = '비밀번호 앞 2자리를 입력해주세요'
    return e
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }
    setLoading(true)
    await new Promise(r => setTimeout(r, 1800))
    setLoading(false)
    setDone(true)
    await new Promise(r => setTimeout(r, 1200))
    const masked = `${cardNumber.slice(0, 4)} **** **** ${cardNumber.slice(-4)}`
    onSave({ masked, registeredAt: new Date().toISOString().slice(0, 10) })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
        <div className="flex items-center justify-between mb-5">
          <div className="flex items-center gap-2">
            <CreditCard size={18} className="text-navy" />
            <h2 className="text-base font-bold text-navy">카드 등록</h2>
          </div>
          <button onClick={onClose} className="text-gray-400 hover:text-navy transition-colors">
            <X size={18} />
          </button>
        </div>

        {done ? (
          <div className="flex flex-col items-center gap-3 py-6">
            <div className="w-14 h-14 rounded-full bg-green-100 flex items-center justify-center">
              <CheckCircle size={28} className="text-green-500" />
            </div>
            <p className="font-semibold text-navy text-sm">카드 등록이 완료되었습니다</p>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="p-3 bg-navy-50 rounded-xl border border-navy-100 flex items-center gap-2">
              <Lock size={13} className="text-navy shrink-0" />
              <p className="text-xs text-gray-500">입력하신 카드 정보는 암호화되어 안전하게 처리됩니다</p>
            </div>

            <div>
              <label className="block text-xs font-semibold text-navy mb-1.5">카드번호</label>
              <input
                type="text"
                inputMode="numeric"
                placeholder="0000-0000-0000-0000"
                value={cardNumber}
                onChange={e => setCardNumber(formatCardNumber(e.target.value))}
                className={`w-full border rounded-xl px-4 py-2.5 text-sm tracking-widest focus:outline-none focus:border-navy transition-colors ${errors.cardNumber ? 'border-red-400' : 'border-offwhite-200'}`}
              />
              {errors.cardNumber && <p className="text-xs text-red-500 mt-1">{errors.cardNumber}</p>}
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-navy mb-1.5">CVC</label>
                <div className="relative">
                  <input
                    type={showCvc ? 'text' : 'password'}
                    inputMode="numeric"
                    placeholder="000"
                    maxLength={3}
                    value={cvc}
                    onChange={e => setCvc(e.target.value.replace(/\D/g, '').slice(0, 3))}
                    className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-navy transition-colors pr-10 ${errors.cvc ? 'border-red-400' : 'border-offwhite-200'}`}
                  />
                  <button type="button" onClick={() => setShowCvc(v => !v)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-navy">
                    {showCvc ? <EyeOff size={14} /> : <Eye size={14} />}
                  </button>
                </div>
                {errors.cvc && <p className="text-xs text-red-500 mt-1">{errors.cvc}</p>}
              </div>

              <div>
                <label className="block text-xs font-semibold text-navy mb-1.5">결제 비밀번호 앞 2자리</label>
                <div className="relative">
                  <input
                    type={showPw ? 'text' : 'password'}
                    inputMode="numeric"
                    placeholder="••"
                    maxLength={2}
                    value={pw2}
                    onChange={e => setPw2(e.target.value.replace(/\D/g, '').slice(0, 2))}
                    className={`w-full border rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-navy transition-colors pr-10 ${errors.pw2 ? 'border-red-400' : 'border-offwhite-200'}`}
                  />
                  <button type="button" onClick={() => setShowPw(v => !v)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-navy">
                    {showPw ? <EyeOff size={14} /> : <Eye size={14} />}
                  </button>
                </div>
                {errors.pw2 && <p className="text-xs text-red-500 mt-1">{errors.pw2}</p>}
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-navy text-white font-bold py-3 rounded-xl hover:bg-navy-700 transition-colors disabled:opacity-60 text-sm mt-2"
            >
              {loading ? '인증 중...' : '카드 등록하기'}
            </button>
          </form>
        )}
      </div>
    </div>
  )
}

export default function CompanySettingsPage() {
  const { resetDemoData } = useAppData()
  const { user } = useAuth()
  const [confirmReset, setConfirmReset] = useState(false)
  const [resetDone, setResetDone]       = useState(false)
  const [showCardModal, setShowCardModal] = useState(false)

  const initial = loadSettings(user)

  const [companyName,    setCompanyName]    = useState(initial.companyName)
  const [bizNumber,      setBizNumber]      = useState(initial.bizNumber)
  const [address,        setAddress]        = useState(initial.address)
  const [addressDetail,  setAddressDetail]  = useState(initial.addressDetail)
  const [basicSaved,     setBasicSaved]     = useState(false)

  const [manager,        setManager]        = useState(initial.manager)
  const [managerSaved,   setManagerSaved]   = useState(false)

  const [registeredCard, setRegisteredCard] = useState(initial.card)

  const [tags,     setTags]     = useState(initial.tags)
  const [tagInput, setTagInput] = useState('')

  function persist(patch) {
    try {
      const current = JSON.parse(localStorage.getItem(SETTINGS_KEY) || '{}')
      localStorage.setItem(SETTINGS_KEY, JSON.stringify({ ...current, ...patch }))
    } catch {}
  }

  function handleSaveBasic() {
    persist({ companyName, bizNumber, address, addressDetail })
    setBasicSaved(true)
    setTimeout(() => setBasicSaved(false), 2500)
  }

  function handleSaveManager() {
    persist({ manager })
    setManagerSaved(true)
    setTimeout(() => setManagerSaved(false), 2500)
  }

  function handleCardSave(card) {
    setRegisteredCard(card)
    persist({ card })
    setShowCardModal(false)
  }

  function addTag() {
    const t = tagInput.trim()
    if (t && !tags.includes(t)) {
      const next = [...tags, t]
      setTags(next)
      persist({ tags: next })
    }
    setTagInput('')
  }

  function removeTag(t) {
    const next = tags.filter(x => x !== t)
    setTags(next)
    persist({ tags: next })
  }

  function handleReset() {
    resetDemoData()
    setConfirmReset(false)
    setResetDone(true)
    setTimeout(() => setResetDone(false), 3000)
  }

  return (
    <div className="max-w-2xl space-y-5">
      <h1 className="text-xl font-bold text-navy">회사 설정</h1>

      {/* ── 기본 정보 ─────────────────────────────────────── */}
      <Card header={
        <div className="flex items-center gap-2">
          <Building2 size={15} className="text-navy" />
          <h2 className="font-semibold text-navy">기본 정보</h2>
        </div>
      }>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-navy mb-1">회사명</label>
            <input
              value={companyName}
              onChange={e => setCompanyName(e.target.value)}
              placeholder="주식회사 JU Company"
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy mb-1">사업자 등록번호</label>
            <input
              value={bizNumber}
              onChange={e => setBizNumber(e.target.value)}
              placeholder="123-45-67890"
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy mb-1">주소</label>
            <input
              value={address}
              onChange={e => setAddress(e.target.value)}
              placeholder="도로명 주소"
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-navy mb-1">상세 주소</label>
            <input
              value={addressDetail}
              onChange={e => setAddressDetail(e.target.value)}
              placeholder="상세 주소 (동/호수 등)"
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
        </div>
        <div className="flex justify-end mt-4">
          {basicSaved
            ? <span className="flex items-center gap-1.5 text-sm text-green-600 font-semibold"><CheckCircle size={14} />저장됐습니다</span>
            : <Button size="sm" onClick={handleSaveBasic}>저장</Button>
          }
        </div>
      </Card>

      {/* ── 채용 담당자 정보 ────────────────────────────────── */}
      <Card header={
        <div className="flex items-center gap-2">
          <User size={15} className="text-navy" />
          <h2 className="font-semibold text-navy">채용 담당자 정보</h2>
        </div>
      }>
        <p className="text-xs text-gray-400 mb-4">공고에 노출될 담당자 정보를 설정합니다. 매번 공고마다 입력할 필요 없이 기본값으로 사용됩니다.</p>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">담당자 성함</label>
            <input
              value={manager.name}
              onChange={e => setManager(p => ({ ...p, name: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">직급</label>
            <input
              value={manager.title}
              onChange={e => setManager(p => ({ ...p, title: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">연락처</label>
            <input
              value={manager.phone}
              onChange={e => setManager(p => ({ ...p, phone: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-500 mb-1">이메일</label>
            <input
              value={manager.email}
              onChange={e => setManager(p => ({ ...p, email: e.target.value }))}
              className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
            />
          </div>
        </div>
        <div className="flex justify-end mt-4">
          {managerSaved
            ? <span className="flex items-center gap-1.5 text-sm text-green-600 font-semibold"><CheckCircle size={14} />저장됐습니다</span>
            : <Button size="sm" onClick={handleSaveManager}>저장</Button>
          }
        </div>
      </Card>

      {/* ── 결제 카드 ─────────────────────────────────────── */}
      <Card header={
        <div className="flex items-center gap-2">
          <CreditCard size={15} className="text-navy" />
          <h2 className="font-semibold text-navy">결제 카드</h2>
        </div>
      }>
        <p className="text-xs text-gray-400 mb-4">정산 탭에서 확정된 급여 지급 시 사용할 카드를 등록합니다.</p>

        {registeredCard ? (
          <div className="flex items-center justify-between p-4 bg-navy-50 rounded-xl border border-navy-100">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-lg bg-navy flex items-center justify-center">
                <CreditCard size={18} className="text-white" />
              </div>
              <div>
                <p className="text-sm font-semibold text-navy">{registeredCard.masked}</p>
                <p className="text-xs text-gray-400">{registeredCard.registeredAt} 등록</p>
              </div>
            </div>
            <button
              onClick={() => setShowCardModal(true)}
              className="text-xs text-gray-400 hover:text-navy transition-colors font-medium underline underline-offset-2"
            >
              변경
            </button>
          </div>
        ) : (
          <button
            onClick={() => setShowCardModal(true)}
            className="w-full flex items-center justify-center gap-2 py-4 border-2 border-dashed border-offwhite-200 rounded-xl text-sm text-gray-400 hover:border-navy hover:text-navy transition-colors"
          >
            <CreditCard size={16} />
            카드 등록하기
          </button>
        )}
      </Card>

      {/* ── 우대 조건 템플릿 ───────────────────────────────── */}
      <Card header={
        <div className="flex items-center gap-2">
          <Star size={15} className="text-navy" />
          <h2 className="font-semibold text-navy">우대 조건 템플릿</h2>
        </div>
      }>
        <p className="text-xs text-gray-400 mb-4">자주 찾는 인재상을 태그로 저장해두고 공고 등록 시 불러와 사용합니다.</p>
        <div className="flex flex-wrap gap-2 mb-3">
          {tags.map(t => (
            <span key={t} className="flex items-center gap-1 bg-navy-50 text-navy text-xs font-semibold px-2.5 py-1 rounded-full border border-navy-100">
              {t}
              <button onClick={() => removeTag(t)} className="hover:text-red-500 transition-colors ml-0.5">
                <X size={11} />
              </button>
            </span>
          ))}
        </div>
        <div className="flex flex-wrap gap-1.5 mb-3">
          {MOCK_PREFERRED_TAGS.filter(t => !tags.includes(t)).map(t => (
            <button
              key={t}
              onClick={() => { const next = [...tags, t]; setTags(next); persist({ tags: next }) }}
              className="text-xs text-gray-500 border border-dashed border-gray-300 px-2.5 py-1 rounded-full hover:border-navy hover:text-navy transition-colors"
            >
              + {t}
            </button>
          ))}
        </div>
        <div className="flex gap-2">
          <input
            type="text"
            placeholder="직접 입력..."
            value={tagInput}
            onChange={e => setTagInput(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && addTag()}
            className="flex-1 border border-offwhite-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-navy"
          />
          <Button size="sm" icon={Plus} onClick={addTag}>추가</Button>
        </div>
      </Card>

      {/* ── 데모 관리 ─────────────────────────────────────── */}
      <Card header={
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-orange bg-orange-50 border border-orange/20 px-2 py-0.5 rounded-full">DEMO</span>
          <h2 className="font-semibold text-navy">데모 데이터 관리</h2>
        </div>
      }>
        <div className="space-y-4">
          <div className="flex items-start gap-3 p-3.5 bg-amber-50 rounded-xl border border-amber-200">
            <AlertTriangle size={15} className="text-amber-500 shrink-0 mt-0.5" />
            <p className="text-xs text-amber-700 leading-relaxed">
              시연 연습을 여러 번 진행하다가 데이터가 꼬였을 때 사용하세요.
              모든 Shift·공고 상태, 채용 확정 기록, 임시 저장 데이터가 <strong>초기 시연용 상태로 완전히 되돌아갑니다.</strong>
            </p>
          </div>
          <div className="flex items-start justify-between gap-4">
            <div className="text-sm text-gray-600 space-y-1">
              <p className="font-semibold text-navy">초기화 범위</p>
              <ul className="text-xs text-gray-500 space-y-0.5 list-disc list-inside">
                <li>모든 Shift 상태 (예정 / 완료 등)</li>
                <li>채용 확정 인원수 및 지원자 선별 기록</li>
                <li>초대/확정 내역</li>
                <li>임시 저장 데이터 (staffnow_shift_*)</li>
                <li>알림 읽음 상태</li>
              </ul>
            </div>
            {!confirmReset ? (
              <button
                onClick={() => setConfirmReset(true)}
                className="shrink-0 flex items-center gap-2 px-4 py-2.5 rounded-xl border border-red-200 text-red-500 text-sm font-semibold hover:bg-red-50 transition-colors"
              >
                <RotateCcw size={14} />데모 초기화
              </button>
            ) : (
              <div className="shrink-0 flex flex-col items-end gap-2">
                <p className="text-xs font-semibold text-red-500">정말 초기화하시겠습니까?</p>
                <div className="flex gap-2">
                  <button
                    onClick={() => setConfirmReset(false)}
                    className="px-3 py-1.5 rounded-lg border border-offwhite-200 text-gray-500 text-xs font-semibold hover:bg-offwhite transition-colors"
                  >
                    취소
                  </button>
                  <button
                    onClick={handleReset}
                    className="px-3 py-1.5 rounded-lg bg-red-500 hover:bg-red-600 text-white text-xs font-bold transition-colors"
                  >
                    초기화 실행
                  </button>
                </div>
              </div>
            )}
          </div>
          {resetDone && (
            <div className="flex items-center gap-2 p-3 bg-green-50 rounded-xl border border-green-200 text-green-700 text-sm font-semibold">
              <CheckCircle size={15} />
              초기화 완료 — 깨끗한 데모 상태로 되돌아왔습니다
            </div>
          )}
        </div>
      </Card>

      {showCardModal && (
        <CardRegisterModal
          current={registeredCard}
          onClose={() => setShowCardModal(false)}
          onSave={handleCardSave}
        />
      )}
    </div>
  )
}
