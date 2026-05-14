import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { User, Building2, ShieldCheck, Eye, EyeOff, CheckCircle2 } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { useAppData } from '../../context/AppDataContext'
import AppFooter from '../../components/ui/AppFooter'

const ROLES = [
  { key: 'INDIVIDUAL', label: '개인 회원',  icon: User,        hint: '구직자로 로그인',     email: 'user@staffnow.kr' },
  { key: 'BUSINESS',   label: '기업 회원',  icon: Building2,   hint: '채용담당자로 로그인',  email: 'biz@staffnow.kr' },
  { key: 'ADMIN',      label: '관리자',     icon: ShieldCheck, hint: '플랫폼 운영자',       email: 'admin@staffnow.kr' },
]

const FEATURES = [
  '개인·기업·관리자 삼원화 플랫폼',
  '실시간 공고 매칭 및 지원 관리',
  '스태프 근태·정산 통합 관리',
]

export default function LoginPage() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const { reinitializeConversations } = useAppData()
  const [role, setRole] = useState('BUSINESS')
  const [email, setEmail] = useState('biz@staffnow.kr')
  const [password, setPassword] = useState('demo1234')
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError]     = useState('')

  function handleRoleChange(r) {
    setRole(r)
    setError('')
    setEmail(ROLES.find(x => x.key === r).email)
    setPassword('demo1234')
  }

  async function handleLogin(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    await new Promise(r => setTimeout(r, 500))
    const result = login(role, email, password)
    setLoading(false)
    if (result.error) {
      setError(result.error)
    } else {
      reinitializeConversations()
      navigate(result.path)
    }
  }

  return (
    <div className="min-h-screen flex">
      {/* 왼쪽: 브랜딩 패널 */}
      <div className="hidden lg:flex flex-col justify-between w-[52%] bg-navy px-14 py-12">
        {/* 로고 */}
        <div className="flex items-center gap-3">
          <img src="/favicon.png" alt="StaffNow" className="w-10 h-10 object-contain shrink-0" />
          <div>
            <span className="text-white font-bold text-xl leading-none block">StaffNow</span>
            <span className="text-navy-200 text-xs">인력 운영 플랫폼</span>
          </div>
        </div>

        {/* 메인 카피 */}
        <div className="space-y-6">
          <div>
            <h1 className="text-4xl font-extrabold text-white leading-tight">
              인력 운영의<br />
              <span className="text-orange">새로운 기준</span>
            </h1>
            <p className="text-navy-200 text-base mt-4 leading-relaxed">
              개인 구직자부터 기업 운영, 플랫폼 관리까지<br />
              하나의 시스템으로 연결합니다.
            </p>
          </div>

          <div className="space-y-3">
            {FEATURES.map((f, i) => (
              <div key={i} className="flex items-center gap-3">
                <CheckCircle2 size={16} className="text-orange shrink-0" />
                <span className="text-navy-200 text-sm">{f}</span>
              </div>
            ))}
          </div>
        </div>

        {/* 하단 통계 */}
        <div className="grid grid-cols-3 gap-6 border-t border-navy-700 pt-8">
          {[
            { value: '12,847', label: '등록 회원' },
            { value: '3,892', label: '누적 공고' },
            { value: '28,491', label: '매칭 건수' },
          ].map(s => (
            <div key={s.label}>
              <p className="text-2xl font-extrabold text-white tabular-nums">{s.value}</p>
              <p className="text-xs text-navy-200 mt-0.5">{s.label}</p>
            </div>
          ))}
        </div>
      </div>

      {/* 오른쪽: 로그인 폼 */}
      <div className="flex-1 flex items-center justify-center bg-offwhite px-6 py-12">
        <div className="w-full max-w-md">
          {/* 모바일 로고 */}
          <div className="flex items-center gap-2.5 mb-8 lg:hidden">
            <img src="/favicon.png" alt="StaffNow" className="w-9 h-9 object-contain" />
            <span className="text-navy font-bold text-xl">StaffNow</span>
          </div>

          <div className="mb-8">
            <h2 className="text-2xl font-bold text-navy">로그인</h2>
            <p className="text-sm text-gray-500 mt-1">회원 유형을 선택하고 로그인하세요</p>
          </div>

          {/* role 선택 */}
          <div className="grid grid-cols-3 gap-2.5 mb-7">
            {ROLES.map(r => {
              const Icon = r.icon
              const active = role === r.key
              return (
                <button
                  key={r.key}
                  type="button"
                  onClick={() => handleRoleChange(r.key)}
                  className={`flex flex-col items-center gap-1.5 py-3.5 px-2 rounded-xl border-2 transition-all text-center
                    ${active
                      ? 'border-orange bg-orange-50 text-orange'
                      : 'border-offwhite-200 bg-white text-gray-400 hover:border-navy-100 hover:text-navy'}`}
                >
                  <Icon size={20} strokeWidth={active ? 2.5 : 1.5} />
                  <span className={`text-xs font-semibold leading-tight ${active ? 'text-orange' : 'text-navy'}`}>
                    {r.label}
                  </span>
                  <span className="text-[10px] leading-tight text-gray-400">{r.hint}</span>
                </button>
              )
            })}
          </div>

          {/* 로그인 폼 */}
          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">이메일</label>
              <input
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                className="w-full border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-navy transition-colors"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-navy mb-1.5">비밀번호</label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="w-full border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm bg-white focus:outline-none focus:border-navy transition-colors pr-11"
                />
                <button
                  type="button"
                  onClick={() => setShowPw(v => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-navy transition-colors"
                >
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            <div className="flex items-center justify-between text-xs">
              <label className="flex items-center gap-1.5 text-gray-500 cursor-pointer">
                <input type="checkbox" className="rounded" />
                로그인 상태 유지
              </label>
              <Link to="/forgot-password" className="text-navy hover:text-orange transition-colors font-medium text-xs">
                비밀번호 찾기
              </Link>
            </div>

            {error && (
              <p className="flex items-center gap-1.5 text-sm text-red-500 bg-red-50 border border-red-200 rounded-xl px-4 py-2.5">
                <span className="shrink-0">⚠</span>{error}
              </p>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-orange text-white font-bold py-3 rounded-xl hover:bg-orange-600 active:bg-orange-700 transition-colors disabled:opacity-60 text-sm mt-2"
            >
              {loading ? '로그인 중...' : '로그인'}
            </button>
          </form>

          <div className="mt-6 text-center text-sm text-gray-500 space-y-2">
            <p>
              아직 회원이 아니신가요?{' '}
              <Link to="/register" className="text-orange font-semibold hover:underline">회원가입</Link>
            </p>
          </div>

          {/* 데모 안내 */}
          <div className="mt-6 p-3.5 bg-navy-50 rounded-xl border border-navy-100">
            <p className="text-xs font-semibold text-navy mb-1.5">데모 계정 안내</p>
            <div className="space-y-0.5">
              {ROLES.map(r => (
                <p key={r.key} className="text-xs text-gray-500">
                  <span className="font-medium text-navy">{r.label}</span>: {r.email} / demo1234
                </p>
              ))}
            </div>
          </div>

          <AppFooter />
        </div>
      </div>
    </div>
  )
}
