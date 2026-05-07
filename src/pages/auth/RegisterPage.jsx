import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Zap, User, Building2, ArrowLeft, Eye, EyeOff, CheckCircle2 } from 'lucide-react'

const ROLE_OPTIONS = [
  {
    key: 'INDIVIDUAL',
    icon: User,
    title: '개인 회원',
    desc: '구직자로 가입하여 공고를 검색하고 지원하세요.',
  },
  {
    key: 'BUSINESS',
    icon: Building2,
    title: '기업 회원',
    desc: '채용담당자로 가입하여 인력을 모집하고 관리하세요.',
  },
]

export default function RegisterPage() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [role, setRole] = useState(null)
  const [form, setForm] = useState({ name: '', email: '', password: '', confirmPw: '', company: '' })
  const [showPw, setShowPw] = useState(false)
  const [done, setDone] = useState(false)

  function handleChange(e) {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    await new Promise(r => setTimeout(r, 600))
    setDone(true)
  }

  if (done) {
    return (
      <div className="min-h-screen bg-offwhite flex items-center justify-center p-6">
        <div className="bg-white rounded-2xl shadow-lg p-10 max-w-sm w-full text-center">
          <div className="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-5">
            <CheckCircle2 size={32} className="text-green-500" />
          </div>
          <h2 className="text-xl font-bold text-navy mb-2">가입 완료!</h2>
          <p className="text-sm text-gray-500 mb-6">
            {form.email}로 가입이 완료되었습니다.<br />로그인하여 서비스를 이용해보세요.
          </p>
          <button
            onClick={() => navigate('/login')}
            className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 transition-colors text-sm"
          >
            로그인하러 가기
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-offwhite flex items-center justify-center p-6">
      <div className="bg-white rounded-2xl shadow-lg p-8 w-full max-w-md">
        {/* 헤더 */}
        <div className="flex items-center gap-3 mb-7">
          <button onClick={() => step === 1 ? navigate('/login') : setStep(1)} className="p-1.5 rounded-lg hover:bg-offwhite text-gray-400 hover:text-navy transition-colors">
            <ArrowLeft size={18} />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <div className="w-7 h-7 rounded-lg bg-orange flex items-center justify-center">
                <Zap size={14} className="text-white fill-white" />
              </div>
              <span className="font-bold text-navy">StaffNow</span>
            </div>
            <p className="text-xs text-gray-400 mt-0.5">
              {step === 1 ? '회원 유형 선택' : '기본 정보 입력'}  ·  {step}/2단계
            </p>
          </div>
        </div>

        <h2 className="text-xl font-bold text-navy mb-1">회원가입</h2>
        <p className="text-sm text-gray-500 mb-6">
          {step === 1 ? '어떤 유형으로 가입하시겠어요?' : `${ROLE_OPTIONS.find(r => r.key === role)?.title}으로 가입합니다.`}
        </p>

        {step === 1 ? (
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
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            {role === 'BUSINESS' && (
              <div>
                <label className="block text-sm font-semibold text-navy mb-1.5">회사명</label>
                <input
                  name="company"
                  value={form.company}
                  onChange={handleChange}
                  required
                  placeholder="(주)스태프나우"
                  className="w-full border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-navy transition-colors"
                />
              </div>
            )}
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">이름</label>
              <input
                name="name"
                value={form.name}
                onChange={handleChange}
                required
                placeholder="홍길동"
                className="w-full border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-navy transition-colors"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">이메일</label>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                required
                placeholder="example@staffnow.kr"
                className="w-full border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-navy transition-colors"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">비밀번호</label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  required
                  placeholder="8자 이상"
                  className="w-full border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-navy transition-colors pr-11"
                />
                <button type="button" onClick={() => setShowPw(v => !v)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-navy">
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">비밀번호 확인</label>
              <input
                type="password"
                name="confirmPw"
                value={form.confirmPw}
                onChange={handleChange}
                required
                placeholder="비밀번호 재입력"
                className={`w-full border rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none transition-colors ${form.confirmPw && form.password !== form.confirmPw ? 'border-red-300 focus:border-red-400' : 'border-offwhite-200 focus:border-navy'}`}
              />
              {form.confirmPw && form.password !== form.confirmPw && (
                <p className="text-xs text-red-500 mt-1">비밀번호가 일치하지 않습니다.</p>
              )}
            </div>
            <button
              type="submit"
              disabled={form.password !== form.confirmPw}
              className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 transition-colors disabled:opacity-50 text-sm mt-2"
            >
              가입하기
            </button>
          </form>
        )}

        <p className="text-center text-sm text-gray-500 mt-5">
          이미 회원이신가요?{' '}
          <button onClick={() => navigate('/login')} className="text-orange font-semibold hover:underline">로그인</button>
        </p>
      </div>
    </div>
  )
}
