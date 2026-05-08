import { useState } from 'react'
import { Plus, X, Mail, Shield, User, AlertCircle, CheckCircle2 } from 'lucide-react'
import Card from '../../components/ui/Card'
import Avatar from '../../components/ui/Avatar'
import Button from '../../components/ui/Button'
import { useAppData } from '../../context/AppDataContext'

const ROLE_OPTIONS = [
  { value: '관리자',    label: '관리자',    desc: '모든 기능 접근 가능',          icon: Shield },
  { value: '운영 담당', label: '운영 담당', desc: '공고·Shift·정산 관리 가능',     icon: User },
  { value: '조회 전용', label: '조회 전용', desc: '대시보드 및 데이터 조회만 가능', icon: User },
]

const INITIAL_MEMBERS = [
  { id: 1, name: '김운영', email: 'admin@jucompany.kr',   role: '관리자',    initials: '김', joinedAt: '2025-01-01' },
  { id: 2, name: '박담당', email: 'manager@jucompany.kr', role: '운영 담당', initials: '박', joinedAt: '2025-03-15' },
]

const ROLE_COLOR = {
  '관리자':    'text-orange bg-orange/10',
  '운영 담당': 'text-navy bg-navy/10',
  '조회 전용': 'text-gray-500 bg-gray-100',
}

function InviteModal({ onClose, onInvite }) {
  const [email, setEmail] = useState('')
  const [role, setRole] = useState('운영 담당')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function validate() {
    if (!email.trim()) return '이메일을 입력해 주세요'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return '올바른 이메일 형식이 아닙니다'
    return ''
  }

  function handleSubmit(e) {
    e.preventDefault()
    const err = validate()
    if (err) { setError(err); return }
    setLoading(true)
    setTimeout(() => {
      onInvite({ email: email.trim(), role })
      setLoading(false)
      onClose()
    }, 800)
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 p-6" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-5">
          <div>
            <h2 className="text-base font-bold text-navy">멤버 초대</h2>
            <p className="text-xs text-gray-400 mt-0.5">이메일로 초대장을 발송합니다 (데모)</p>
          </div>
          <button onClick={onClose} className="w-8 h-8 rounded-lg hover:bg-offwhite-100 flex items-center justify-center text-gray-400">
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* 이메일 */}
          <div>
            <label className="text-xs font-semibold text-gray-500 block mb-1.5">이메일 주소 *</label>
            <div className="relative">
              <Mail size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
              <input
                type="email"
                value={email}
                onChange={e => { setEmail(e.target.value); setError('') }}
                placeholder="invite@example.com"
                className={`w-full pl-9 pr-4 py-2.5 text-sm border rounded-xl focus:outline-none focus:ring-1
                  ${error ? 'border-red-400 focus:ring-red-300' : 'border-offwhite-200 focus:border-orange focus:ring-orange/30'}`}
              />
            </div>
            {error && (
              <p className="flex items-center gap-1 text-xs text-red-500 mt-1">
                <AlertCircle size={11} />{error}
              </p>
            )}
          </div>

          {/* 역할 선택 */}
          <div>
            <label className="text-xs font-semibold text-gray-500 block mb-1.5">역할 *</label>
            <div className="space-y-2">
              {ROLE_OPTIONS.map(opt => {
                const Icon = opt.icon
                const active = role === opt.value
                return (
                  <button
                    key={opt.value}
                    type="button"
                    onClick={() => setRole(opt.value)}
                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl border text-left transition-all
                      ${active
                        ? 'border-orange bg-orange/5 text-navy'
                        : 'border-offwhite-200 hover:border-gray-300 text-gray-600'}`}
                  >
                    <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${active ? 'bg-orange/10' : 'bg-offwhite-100'}`}>
                      <Icon size={15} className={active ? 'text-orange' : 'text-gray-400'} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-semibold">{opt.label}</p>
                      <p className="text-xs text-gray-400">{opt.desc}</p>
                    </div>
                    {active && (
                      <div className="w-4 h-4 rounded-full bg-orange flex items-center justify-center shrink-0">
                        <CheckCircle2 size={12} className="text-white" />
                      </div>
                    )}
                  </button>
                )
              })}
            </div>
          </div>

          {/* 버튼 */}
          <div className="flex gap-2 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2.5 text-sm font-semibold text-gray-500 bg-offwhite-100 hover:bg-offwhite-200 rounded-xl transition-colors"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 py-2.5 text-sm font-semibold text-white bg-orange hover:bg-orange-600 disabled:opacity-60 rounded-xl transition-colors flex items-center justify-center gap-2"
            >
              {loading ? (
                <span className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
              ) : (
                <><Mail size={14} />초대 발송</>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function MemberManagementPage() {
  const { addToast } = useAppData()
  const [members, setMembers] = useState(INITIAL_MEMBERS)
  const [showModal, setShowModal] = useState(false)
  const [pendingInvites, setPendingInvites] = useState([])

  function handleInvite({ email, role }) {
    const newInvite = {
      id: Date.now(),
      email,
      role,
      sentAt: new Date().toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' }),
    }
    setPendingInvites(prev => [newInvite, ...prev])
    addToast({ type: 'success', message: `${email}로 초대장을 발송했습니다 (데모)` })
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-navy">멤버 관리</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            {members.length}명 활성 · {pendingInvites.length > 0 && `${pendingInvites.length}건 초대 대기`}
          </p>
        </div>
        <Button icon={Plus} onClick={() => setShowModal(true)}>멤버 초대</Button>
      </div>

      {/* 현재 멤버 */}
      <div>
        <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 px-1">현재 멤버</p>
        <Card padding={false}>
          {members.map(m => (
            <div key={m.id} className="flex items-center gap-4 px-5 py-4 border-b border-offwhite-100 last:border-0">
              <Avatar initials={m.initials} size="md" />
              <div className="flex-1">
                <p className="font-semibold text-navy text-sm">{m.name}</p>
                <p className="text-xs text-gray-400">{m.email}</p>
              </div>
              <div className="flex items-center gap-3">
                <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${ROLE_COLOR[m.role] ?? 'text-gray-500 bg-gray-100'}`}>
                  {m.role}
                </span>
                <span className="text-xs text-gray-300">{m.joinedAt} 가입</span>
              </div>
            </div>
          ))}
        </Card>
      </div>

      {/* 초대 대기 */}
      {pendingInvites.length > 0 && (
        <div>
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 px-1">초대 대기 중</p>
          <Card padding={false}>
            {pendingInvites.map(inv => (
              <div key={inv.id} className="flex items-center gap-4 px-5 py-4 border-b border-offwhite-100 last:border-0">
                <div className="w-9 h-9 rounded-full bg-offwhite-200 flex items-center justify-center shrink-0">
                  <Mail size={15} className="text-gray-400" />
                </div>
                <div className="flex-1">
                  <p className="font-semibold text-navy text-sm">{inv.email}</p>
                  <p className="text-xs text-gray-400">{inv.sentAt} 발송</p>
                </div>
                <div className="flex items-center gap-3">
                  <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${ROLE_COLOR[inv.role] ?? 'text-gray-500 bg-gray-100'}`}>
                    {inv.role}
                  </span>
                  <span className="text-xs font-medium text-amber-500 bg-amber-50 border border-amber-200 px-2.5 py-1 rounded-full">
                    대기 중
                  </span>
                </div>
              </div>
            ))}
          </Card>
        </div>
      )}

      {showModal && (
        <InviteModal onClose={() => setShowModal(false)} onInvite={handleInvite} />
      )}
    </div>
  )
}
