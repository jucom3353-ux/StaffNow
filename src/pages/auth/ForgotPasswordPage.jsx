import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Zap, ArrowLeft, Mail, CheckCircle2, Lock, Eye, EyeOff } from 'lucide-react'

export default function ForgotPasswordPage() {
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [email, setEmail] = useState('')
  const [code, setCode] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPw, setConfirmPw] = useState('')
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)

  async function handleSendCode(e) {
    e.preventDefault()
    setLoading(true)
    await new Promise(r => setTimeout(r, 700))
    setLoading(false)
    setStep(2)
  }

  async function handleVerifyCode(e) {
    e.preventDefault()
    setLoading(true)
    await new Promise(r => setTimeout(r, 500))
    setLoading(false)
    setStep(3)
  }

  async function handleReset(e) {
    e.preventDefault()
    setLoading(true)
    await new Promise(r => setTimeout(r, 600))
    setLoading(false)
    setStep(4)
  }

  const STEPS = ['이메일 입력', '코드 확인', '새 비밀번호', '완료']

  return (
    <div className="min-h-screen bg-offwhite flex items-center justify-center p-6">
      <div className="bg-white rounded-2xl shadow-lg p-8 w-full max-w-md">
        {/* 헤더 */}
        <div className="flex items-center gap-3 mb-7">
          <button
            onClick={() => step === 1 ? navigate('/login') : setStep(s => s - 1)}
            className="p-1.5 rounded-lg hover:bg-offwhite text-gray-400 hover:text-navy transition-colors"
          >
            <ArrowLeft size={18} />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <div className="w-7 h-7 rounded-lg bg-orange flex items-center justify-center">
                <Zap size={14} className="text-white fill-white" />
              </div>
              <span className="font-bold text-navy">StaffNow</span>
            </div>
            <p className="text-xs text-gray-400 mt-0.5">비밀번호 재설정 · {step}/4단계</p>
          </div>
        </div>

        {/* 진행 표시 */}
        <div className="flex items-center gap-1 mb-7">
          {STEPS.map((s, i) => (
            <div key={i} className={`flex-1 h-1 rounded-full transition-colors ${i + 1 <= step ? 'bg-orange' : 'bg-offwhite-200'}`} />
          ))}
        </div>

        {/* Step 1: 이메일 */}
        {step === 1 && (
          <>
            <h2 className="text-xl font-bold text-navy mb-1">이메일 입력</h2>
            <p className="text-sm text-gray-500 mb-6">가입 시 등록한 이메일로 인증 코드를 보내드립니다.</p>
            <form onSubmit={handleSendCode} className="space-y-4">
              <div>
                <label className="block text-sm font-semibold text-navy mb-1.5">이메일</label>
                <input
                  type="email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required
                  placeholder="example@staffnow.kr"
                  className="w-full border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-navy transition-colors"
                />
              </div>
              <button
                type="submit"
                disabled={loading}
                className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 transition-colors disabled:opacity-60 text-sm"
              >
                {loading ? '전송 중...' : '인증 코드 전송'}
              </button>
            </form>
          </>
        )}

        {/* Step 2: 코드 확인 */}
        {step === 2 && (
          <>
            <h2 className="text-xl font-bold text-navy mb-1">인증 코드 확인</h2>
            <p className="text-sm text-gray-500 mb-1">{email}로 전송된</p>
            <p className="text-sm text-gray-500 mb-6">6자리 인증 코드를 입력해 주세요. <span className="text-xs text-gray-400">(데모: 123456)</span></p>
            <form onSubmit={handleVerifyCode} className="space-y-4">
              <div>
                <label className="block text-sm font-semibold text-navy mb-1.5">인증 코드</label>
                <input
                  type="text"
                  value={code}
                  onChange={e => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  required
                  placeholder="123456"
                  className="w-full border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-navy transition-colors tracking-widest text-center text-lg font-bold"
                />
              </div>
              <button
                type="submit"
                disabled={loading || code.length !== 6}
                className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 transition-colors disabled:opacity-60 text-sm"
              >
                {loading ? '확인 중...' : '코드 확인'}
              </button>
              <button type="button" onClick={() => setStep(1)} className="w-full text-sm text-gray-400 hover:text-navy">
                이메일 다시 입력
              </button>
            </form>
          </>
        )}

        {/* Step 3: 새 비밀번호 */}
        {step === 3 && (
          <>
            <h2 className="text-xl font-bold text-navy mb-1">새 비밀번호 설정</h2>
            <p className="text-sm text-gray-500 mb-6">새로운 비밀번호를 입력해 주세요.</p>
            <form onSubmit={handleReset} className="space-y-4">
              <div>
                <label className="block text-sm font-semibold text-navy mb-1.5">새 비밀번호</label>
                <div className="relative">
                  <input
                    type={showPw ? 'text' : 'password'}
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                    placeholder="8자 이상"
                    className="w-full border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-navy transition-colors pr-11"
                  />
                  <button type="button" onClick={() => setShowPw(v => !v)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                    {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                </div>
              </div>
              <div>
                <label className="block text-sm font-semibold text-navy mb-1.5">비밀번호 확인</label>
                <input
                  type="password"
                  value={confirmPw}
                  onChange={e => setConfirmPw(e.target.value)}
                  required
                  placeholder="비밀번호 재입력"
                  className={`w-full border rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none transition-colors ${confirmPw && password !== confirmPw ? 'border-red-300' : 'border-offwhite-200 focus:border-navy'}`}
                />
                {confirmPw && password !== confirmPw && (
                  <p className="text-xs text-red-500 mt-1">비밀번호가 일치하지 않습니다.</p>
                )}
              </div>
              <button
                type="submit"
                disabled={loading || password !== confirmPw || password.length < 8}
                className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 transition-colors disabled:opacity-60 text-sm"
              >
                {loading ? '변경 중...' : '비밀번호 변경'}
              </button>
            </form>
          </>
        )}

        {/* Step 4: 완료 */}
        {step === 4 && (
          <div className="text-center py-4">
            <div className="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-5">
              <CheckCircle2 size={32} className="text-green-500" />
            </div>
            <h2 className="text-xl font-bold text-navy mb-2">비밀번호 변경 완료</h2>
            <p className="text-sm text-gray-500 mb-7">새 비밀번호로 로그인하세요.</p>
            <button
              onClick={() => navigate('/login')}
              className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 transition-colors text-sm"
            >
              로그인하러 가기
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
