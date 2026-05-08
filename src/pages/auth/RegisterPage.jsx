import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Zap, User, Building2, ArrowLeft, Eye, EyeOff,
  Phone, Shield, Search, Check, CheckCircle2, AlertCircle,
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { useAppData } from '../../context/AppDataContext'


// ── Mock terms text ───────────────────────────────────────────────────────────
const T_SERVICE = `제1조 (목적)\n이 약관은 주식회사 스태프나우(이하 "회사")가 제공하는 StaffNow 서비스의 이용에 관한 조건 및 절차, 회사와 이용자의 권리·의무 및 책임사항을 규정함을 목적으로 합니다.\n\n제2조 (용어의 정의)\n"서비스"란 회사가 제공하는 단기 인력 매칭 플랫폼 및 이와 관련된 제반 서비스를 의미합니다. "이용자"란 이 약관에 동의하고 서비스를 이용하는 회원을 말합니다.\n\n제3조 (약관의 효력)\n이 약관은 서비스를 이용하는 모든 이용자에게 적용됩니다. 회사는 관련 법령을 위반하지 않는 범위 내에서 이 약관을 변경할 수 있습니다.\n\n제4조 (서비스의 제공)\n회사는 인력 매칭, 공고 게시, 근태 및 정산 관리 등의 서비스를 제공하며, 필요에 따라 서비스 내용을 변경할 수 있습니다.\n\n제5조 (이용자의 의무)\n이용자는 서비스 이용 시 관련 법령 및 이 약관의 규정을 준수하여야 합니다.`

const T_PRIVACY = `1. 개인정보 수집·이용 목적\n회원 가입 및 관리, 서비스 제공 및 운영, 고객 상담 및 민원 처리\n\n2. 수집하는 개인정보 항목\n[필수] 회사명, 대표자명, 사업자등록번호, 이메일, 휴대폰 번호\n[선택] 사업장 주소, 담당자 직급\n\n3. 개인정보 보유 및 이용 기간\n회원 탈퇴 시까지. 단, 관련 법령에 따라 일정 기간 보관될 수 있습니다.\n\n4. 개인정보의 제3자 제공\n원칙적으로 이용자의 개인정보를 외부에 제공하지 않습니다.`

const T_THIRD = `[선택] 마케팅 및 서비스 개선 목적의 제3자 제공\n\n제공받는 자: 파트너 구인·구직 플랫폼사\n제공 목적: 맞춤형 공고 추천 및 서비스 개선\n제공 항목: 회사명, 업종, 채용 이력(통계)\n보유 기간: 제공일로부터 1년\n\n※ 본 동의는 선택사항으로, 동의하지 않아도 서비스 이용에 제한이 없습니다.`

const BIZ_STEPS = ['약관 동의', '본인·기업 인증', '기업 상세 정보']

const ROLE_OPTIONS = [
  { key: 'INDIVIDUAL', icon: User,     title: '개인 회원', desc: '구직자로 가입하여 공고를 검색하고 지원하세요.' },
  { key: 'BUSINESS',   icon: Building2, title: '기업 회원', desc: '채용담당자로 가입하여 인력을 모집하고 관리하세요.' },
]

// ── Stepper (BUSINESS only) ───────────────────────────────────────────────────
function Stepper({ current }) {
  return (
    <div className="flex items-start mb-7">
      {BIZ_STEPS.map((label, i) => {
        const done   = i < current
        const active = i === current
        return (
          <div key={i} className="flex items-center flex-1 min-w-0">
            <div className="flex flex-col items-center gap-1 shrink-0">
              <div className={`
                w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all
                ${done ? 'bg-orange text-white' : active ? 'bg-navy text-white' : 'bg-gray-200 text-gray-400'}
              `}>
                {done ? <Check size={14} /> : i + 1}
              </div>
              <p className={`
                text-[10px] font-semibold text-center whitespace-nowrap
                ${active ? 'text-navy' : done ? 'text-orange' : 'text-gray-400'}
              `}>
                {label}
              </p>
            </div>
            {i < BIZ_STEPS.length - 1 && (
              <div className={`h-px flex-1 mx-2 -mt-3.5 transition-all ${done ? 'bg-orange' : 'bg-gray-200'}`} />
            )}
          </div>
        )
      })}
    </div>
  )
}

// ── TermsBlock ────────────────────────────────────────────────────────────────
function TermsBlock({ title, required, content, checked, onChange }) {
  return (
    <div className="border border-offwhite-200 rounded-xl overflow-hidden">
      <div
        className="flex items-center gap-3 px-4 py-3 cursor-pointer hover:bg-offwhite-100 select-none"
        onClick={() => onChange(!checked)}
      >
        <div className={`
          w-5 h-5 rounded flex items-center justify-center shrink-0 border-2 transition-all
          ${checked ? 'bg-orange border-orange' : 'border-gray-300'}
        `}>
          {checked && <Check size={11} className="text-white" />}
        </div>
        <p className="flex-1 text-sm font-semibold text-navy">{title}</p>
        <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${required ? 'bg-red-50 text-red-500' : 'bg-gray-100 text-gray-400'}`}>
          {required ? '필수' : '선택'}
        </span>
      </div>
      <div className="px-4 pb-3">
        <div className="bg-offwhite-50 rounded-lg px-3 py-2.5 h-[72px] overflow-y-auto text-[11px] text-gray-400 leading-relaxed whitespace-pre-line border border-offwhite-200">
          {content}
        </div>
      </div>
    </div>
  )
}

// ── FieldError ────────────────────────────────────────────────────────────────
function FieldError({ msg }) {
  if (!msg) return null
  return (
    <p className="flex items-center gap-1 text-xs text-red-500 mt-1">
      <AlertCircle size={11} />{msg}
    </p>
  )
}

// ── Floating Toast ────────────────────────────────────────────────────────────
function FloatToast({ visible, message }) {
  if (!visible) return null
  return (
    <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 flex items-center gap-2 bg-green-600 text-white text-sm font-semibold px-5 py-3 rounded-xl shadow-lg animate-fade-in">
      <CheckCircle2 size={15} />
      {message}
    </div>
  )
}

// ── VerifiedBadge ─────────────────────────────────────────────────────────────
function VerifiedBadge({ label }) {
  return (
    <div className="mt-2 flex items-center gap-2 text-sm text-green-700 bg-green-50 border border-green-200 rounded-xl px-4 py-2.5">
      <CheckCircle2 size={15} className="shrink-0" />
      {label}
    </div>
  )
}

// ── Spinner ───────────────────────────────────────────────────────────────────
function Spinner() {
  return <span className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin inline-block" />
}

// ── Main component ────────────────────────────────────────────────────────────
export default function RegisterPage() {
  const navigate = useNavigate()
  const { signup, login } = useAuth()
  const { reinitializeConversations } = useAppData()

  // step: 1=role, 2=terms(biz)/info(ind), 3=verify(biz), 4=details(biz)
  const [step, setStep] = useState(1)
  const [role, setRole] = useState(null)

  const [form, setForm] = useState({
    name: '', email: '', password: '', confirmPw: '',
    company: '', representative: '', address: '', addressDetail: '', phone: '', bizNumber: '',
  })
  const [showPw, setShowPw]   = useState(false)
  const [errors, setErrors]   = useState({})

  // Terms state
  const [terms, setTerms] = useState({ service: false, privacy: false, thirdParty: false })
  const allTerms = terms.service && terms.privacy && terms.thirdParty

  // Verification state
  const [phoneSending, setPhoneSending]     = useState(false)
  const [phoneCodeSent, setPhoneCodeSent]   = useState(false)
  const [phoneVerified, setPhoneVerified]   = useState(false)
  const [bizVerifying, setBizVerifying]     = useState(false)
  const [bizVerified, setBizVerified]       = useState(false)
  const [toastVisible, setToastVisible]     = useState(false)

  // ── Helpers ─────────────────────────────────────────────────────────────────
  function setField(field) {
    return e => {
      setForm(f => ({ ...f, [field]: e.target.value }))
      if (errors[field]) setErrors(prev => ({ ...prev, [field]: '' }))
    }
  }

  function goBack() {
    if (step === 1) navigate('/login')
    else setStep(s => s - 1)
  }

  function showToast() {
    setToastVisible(true)
    setTimeout(() => setToastVisible(false), 3000)
  }

  function formatPhone(value) {
    const digits = value.replace(/\D/g, '').slice(0, 11)
    if (digits.length < 4)  return digits
    if (digits.length < 8)  return `${digits.slice(0,3)}-${digits.slice(3)}`
    return `${digits.slice(0,3)}-${digits.slice(3,7)}-${digits.slice(7)}`
  }

  // ── Phone verification simulation ────────────────────────────────────────────
  function sendPhoneCode() {
    if (!form.phone.trim() || phoneSending) return
    setPhoneSending(true)
    setTimeout(() => {
      setPhoneSending(false)
      setPhoneCodeSent(true)
      // auto-verify 1.5s after code "arrives"
      setTimeout(() => setPhoneVerified(true), 1500)
    }, 3000)
  }

  // ── Biz number verification simulation ───────────────────────────────────────
  function verifyBizNumber() {
    if (!form.bizNumber.trim() || bizVerifying) return
    setBizVerifying(true)
    setTimeout(() => {
      setBizVerifying(false)
      setBizVerified(true)
      showToast()
    }, 1000)
  }

  // ── Validation ───────────────────────────────────────────────────────────────
  function validateBizDetails() {
    const e = {}
    if (!form.company.trim())       e.company       = '회사명을 입력해 주세요'
    if (!form.representative.trim()) e.representative = '대표자명을 입력해 주세요'
    if (!form.email.trim())          e.email          = '이메일을 입력해 주세요'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = '올바른 이메일 형식이 아닙니다'
    if (!form.password)              e.password = '비밀번호를 입력해 주세요'
    else if (form.password.length < 8) e.password = '비밀번호는 8자 이상이어야 합니다'
    if (form.password !== form.confirmPw) e.confirmPw = '비밀번호가 일치하지 않습니다'
    return e
  }

  function validateIndividual() {
    const e = {}
    if (!form.name.trim())  e.name = '이름을 입력해 주세요'
    if (!form.email.trim()) e.email = '이메일을 입력해 주세요'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = '올바른 이메일 형식이 아닙니다'
    if (!form.password)       e.password = '비밀번호를 입력해 주세요'
    else if (form.password.length < 8) e.password = '비밀번호는 8자 이상이어야 합니다'
    if (form.password !== form.confirmPw) e.confirmPw = '비밀번호가 일치하지 않습니다'
    return e
  }

  // ── Submit handlers ───────────────────────────────────────────────────────────
  function handleBizSubmit() {
    const errs = validateBizDetails()
    if (Object.keys(errs).length) { setErrors(errs); return }
    const payload = {
      name: form.representative,
      email: form.email,
      company: form.company,
      phone: form.phone,
      bizNumber: form.bizNumber,
      address: form.address,
      addressDetail: form.addressDetail,
      password: form.password,
    }
    signup('BUSINESS', payload)
    const result = login('BUSINESS', form.email, form.password)
    if (result.path) { reinitializeConversations(); navigate(result.path) }
  }

  function handleIndividualSubmit(e) {
    e.preventDefault()
    const errs = validateIndividual()
    if (Object.keys(errs).length) { setErrors(errs); return }
    console.log('[StaffNow] 개인 회원가입 완료:', { role: 'INDIVIDUAL', name: form.name, email: form.email })
    signup('INDIVIDUAL', { name: form.name, email: form.email, password: form.password })
    const result = login('INDIVIDUAL', form.email, form.password)
    if (result.path) { reinitializeConversations(); navigate(result.path) }
  }

  // ── Shared input className helper ─────────────────────────────────────────────
  function inputCls(field) {
    return `w-full border rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none transition-colors ${
      errors[field] ? 'border-red-300 focus:border-red-400' : 'border-offwhite-200 focus:border-navy'
    }`
  }

  // ─────────────────────────────────────────────────────────────────────────────
  return (
    <div className="min-h-screen bg-offwhite flex items-start justify-center py-10 px-6">
      <div className="bg-white rounded-2xl shadow-lg p-8 w-full max-w-lg">

        {/* Header */}
        <div className="flex items-center gap-3 mb-6">
          <button
            onClick={goBack}
            className="p-1.5 rounded-lg hover:bg-offwhite text-gray-400 hover:text-navy transition-colors"
          >
            <ArrowLeft size={18} />
          </button>
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-orange flex items-center justify-center">
              <Zap size={14} className="text-white fill-white" />
            </div>
            <span className="font-bold text-navy">StaffNow</span>
          </div>
        </div>

        {/* Stepper — BUSINESS steps 2~4 */}
        {role === 'BUSINESS' && step >= 2 && <Stepper current={step - 2} />}

        {/* Title */}
        <h2 className="text-xl font-bold text-navy mb-1">회원가입</h2>
        <p className="text-sm text-gray-500 mb-6">
          {step === 1                             && '어떤 유형으로 가입하시겠어요?'}
          {role === 'BUSINESS' && step === 2      && '서비스 이용에 동의해 주세요.'}
          {role === 'BUSINESS' && step === 3      && '본인 및 기업 인증을 진행합니다.'}
          {role === 'BUSINESS' && step === 4      && '기업 기본 정보를 입력해 주세요.'}
          {role === 'INDIVIDUAL' && step === 2    && '개인 회원으로 가입합니다.'}
        </p>

        {/* ══ STEP 1: Role selection ═══════════════════════════════════════════ */}
        {step === 1 && (
          <div className="space-y-3">
            {ROLE_OPTIONS.map(opt => {
              const Icon = opt.icon
              return (
                <button
                  key={opt.key}
                  onClick={() => { setRole(opt.key); setStep(2) }}
                  className="w-full flex items-center gap-4 p-4 rounded-xl border-2 border-offwhite-200 hover:border-orange hover:bg-orange-50 transition-all text-left group"
                >
                  <div className="w-11 h-11 rounded-xl bg-offwhite group-hover:bg-orange/10 flex items-center justify-center shrink-0 transition-colors">
                    <Icon size={22} className="text-navy group-hover:text-orange transition-colors" />
                  </div>
                  <div>
                    <p className="font-bold text-navy text-sm">{opt.title}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{opt.desc}</p>
                  </div>
                </button>
              )
            })}
          </div>
        )}

        {/* ══ STEP 2 (BIZ): Terms ══════════════════════════════════════════════ */}
        {role === 'BUSINESS' && step === 2 && (
          <div className="space-y-3">
            {/* 전체 동의 */}
            <div
              onClick={() => setTerms({ service: !allTerms, privacy: !allTerms, thirdParty: !allTerms })}
              className="flex items-center gap-3 px-4 py-3.5 bg-navy/5 rounded-xl cursor-pointer hover:bg-navy/10 transition-colors select-none"
            >
              <div className={`w-5 h-5 rounded flex items-center justify-center shrink-0 border-2 transition-all ${allTerms ? 'bg-navy border-navy' : 'border-gray-300'}`}>
                {allTerms && <Check size={11} className="text-white" />}
              </div>
              <p className="font-bold text-navy text-sm flex-1">전체 동의</p>
              <p className="text-xs text-gray-400">필수 2개 · 선택 1개</p>
            </div>

            <div className="border-t border-offwhite-200 pt-3 space-y-3">
              <TermsBlock
                title="이용약관" required content={T_SERVICE}
                checked={terms.service}
                onChange={v => setTerms(t => ({ ...t, service: v }))}
              />
              <TermsBlock
                title="개인정보 처리방침" required content={T_PRIVACY}
                checked={terms.privacy}
                onChange={v => setTerms(t => ({ ...t, privacy: v }))}
              />
              <TermsBlock
                title="제3자 정보 제공 동의" required={false} content={T_THIRD}
                checked={terms.thirdParty}
                onChange={v => setTerms(t => ({ ...t, thirdParty: v }))}
              />
            </div>

            <button
              disabled={!terms.service || !terms.privacy}
              onClick={() => setStep(3)}
              className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 transition-colors disabled:opacity-40 text-sm"
            >
              다음
            </button>
          </div>
        )}

        {/* ══ STEP 3 (BIZ): Verification ═══════════════════════════════════════ */}
        {role === 'BUSINESS' && step === 3 && (
          <div className="space-y-6">
            {/* 휴대폰 인증 */}
            <div>
              <label className="flex items-center gap-1.5 text-sm font-semibold text-navy mb-2">
                <Phone size={13} />휴대폰 인증
              </label>
              <div className="flex gap-2">
                <input
                  value={form.phone}
                  onChange={e => setForm(f => ({ ...f, phone: formatPhone(e.target.value) }))}
                  placeholder="010-0000-0000"
                  disabled={phoneVerified || phoneSending || phoneCodeSent}
                  className="flex-1 border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-navy disabled:bg-offwhite disabled:text-gray-400 transition-colors"
                />
                {!phoneVerified && (
                  <button
                    onClick={sendPhoneCode}
                    disabled={!form.phone.trim() || phoneSending || phoneCodeSent}
                    className="px-4 py-2.5 text-sm font-semibold text-white bg-navy rounded-xl hover:bg-navy-700 disabled:opacity-50 transition-colors whitespace-nowrap flex items-center gap-2"
                  >
                    {phoneSending ? <Spinner /> : '인증번호 전송'}
                  </button>
                )}
              </div>

              {phoneCodeSent && !phoneVerified && (
                <div className="mt-2 flex items-center gap-2 text-xs text-blue-600 bg-blue-50 border border-blue-200 rounded-xl px-4 py-2.5">
                  <span className="w-3.5 h-3.5 border-2 border-blue-300 border-t-blue-600 rounded-full animate-spin shrink-0" />
                  인증번호 확인 중...
                </div>
              )}
              {phoneVerified && <VerifiedBadge label="휴대폰 인증이 완료되었습니다" />}
            </div>

            {/* 사업자등록번호 인증 */}
            <div>
              <label className="flex items-center gap-1.5 text-sm font-semibold text-navy mb-2">
                <Shield size={13} />사업자등록번호 인증
              </label>
              <div className="flex gap-2">
                <input
                  value={form.bizNumber}
                  onChange={setField('bizNumber')}
                  placeholder="000-00-00000"
                  disabled={bizVerified || bizVerifying}
                  className="flex-1 border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:border-navy disabled:bg-offwhite disabled:text-gray-400 transition-colors"
                />
                {!bizVerified && (
                  <button
                    onClick={verifyBizNumber}
                    disabled={!form.bizNumber.trim() || bizVerifying}
                    className="px-4 py-2.5 text-sm font-semibold text-white bg-navy rounded-xl hover:bg-navy-700 disabled:opacity-50 transition-colors whitespace-nowrap flex items-center gap-2"
                  >
                    {bizVerifying ? <Spinner /> : '확인'}
                  </button>
                )}
              </div>
              {bizVerified && <VerifiedBadge label="사업자등록번호가 인증되었습니다" />}
            </div>

            <button
              disabled={!phoneVerified || !bizVerified}
              onClick={() => setStep(4)}
              className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 transition-colors disabled:opacity-40 text-sm"
            >
              다음
            </button>
          </div>
        )}

        {/* ══ STEP 4 (BIZ): Company details ════════════════════════════════════ */}
        {role === 'BUSINESS' && step === 4 && (
          <div className="space-y-4">
            {/* 회사명 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">회사명</label>
              <input value={form.company} onChange={setField('company')} placeholder="(주)스태프나우" className={inputCls('company')} />
              <FieldError msg={errors.company} />
            </div>

            {/* 대표자명 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">대표자명</label>
              <input value={form.representative} onChange={setField('representative')} placeholder="홍길동" className={inputCls('representative')} />
              <FieldError msg={errors.representative} />
            </div>

            {/* 사업장 주소 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">사업장 주소</label>
              <div className="flex gap-2 mb-2">
                <input
                  value={form.address}
                  readOnly
                  placeholder="주소 검색 후 자동 입력"
                  className="flex-1 border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-offwhite-50 text-gray-400 cursor-not-allowed"
                />
                <button
                  type="button"
                  onClick={() => {
                    new window.daum.Postcode({
                      oncomplete: (data) => {
                        setForm(f => ({ ...f, address: data.roadAddress || data.jibunAddress, addressDetail: '' }))
                        setTimeout(() => document.getElementById('addressDetail')?.focus(), 100)
                      },
                    }).open()
                  }}
                  className="flex items-center gap-1.5 px-4 py-2.5 text-sm font-semibold text-navy border-2 border-navy/30 rounded-xl hover:border-navy hover:bg-navy hover:text-white transition-all whitespace-nowrap"
                >
                  <Search size={13} />주소 검색
                </button>
              </div>
              <input
                id="addressDetail"
                value={form.addressDetail}
                onChange={setField('addressDetail')}
                placeholder="상세 주소 입력 (동, 층, 호수 등)"
                className="w-full border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-navy transition-colors"
              />
            </div>

            {/* 이메일 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">이메일 (로그인 ID)</label>
              <input type="email" value={form.email} onChange={setField('email')} placeholder="example@company.kr" className={inputCls('email')} />
              <FieldError msg={errors.email} />
            </div>

            {/* 비밀번호 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">비밀번호</label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  value={form.password}
                  onChange={setField('password')}
                  placeholder="8자 이상 입력"
                  className={inputCls('password') + ' pr-11'}
                />
                <button type="button" onClick={() => setShowPw(v => !v)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-navy">
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              <FieldError msg={errors.password} />
            </div>

            {/* 비밀번호 확인 */}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">비밀번호 확인</label>
              <input type="password" value={form.confirmPw} onChange={setField('confirmPw')} placeholder="비밀번호 재입력" className={inputCls('confirmPw')} />
              <FieldError msg={errors.confirmPw} />
            </div>

            <button
              onClick={handleBizSubmit}
              className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 transition-colors text-sm mt-1"
            >
              가입하기
            </button>
          </div>
        )}

        {/* ══ STEP 2 (INDIVIDUAL): Info ══════════════════════════════════════ */}
        {role === 'INDIVIDUAL' && step === 2 && (
          <form onSubmit={handleIndividualSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">이름</label>
              <input value={form.name} onChange={setField('name')} placeholder="홍길동" className={inputCls('name')} />
              <FieldError msg={errors.name} />
            </div>
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">이메일</label>
              <input type="email" value={form.email} onChange={setField('email')} placeholder="example@staffnow.kr" className={inputCls('email')} />
              <FieldError msg={errors.email} />
            </div>
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">비밀번호</label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  value={form.password}
                  onChange={setField('password')}
                  placeholder="8자 이상"
                  className={inputCls('password') + ' pr-11'}
                />
                <button type="button" onClick={() => setShowPw(v => !v)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-navy">
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              <FieldError msg={errors.password} />
            </div>
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">비밀번호 확인</label>
              <input type="password" value={form.confirmPw} onChange={setField('confirmPw')} placeholder="비밀번호 재입력" className={inputCls('confirmPw')} />
              <FieldError msg={errors.confirmPw} />
            </div>
            <button type="submit" className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 transition-colors text-sm mt-2">
              가입하기
            </button>
          </form>
        )}

        <p className="text-center text-sm text-gray-500 mt-5">
          이미 회원이신가요?{' '}
          <button onClick={() => navigate('/login')} className="text-orange font-semibold hover:underline">로그인</button>
        </p>
      </div>

      {/* Floating toast for biz verification */}
      <FloatToast visible={toastVisible} message="사업자등록번호가 인증되었습니다" />

    </div>
  )
}
