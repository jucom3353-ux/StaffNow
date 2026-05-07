import { useState } from 'react'
import { Mail, Check, X, Clock, UserCheck, Send } from 'lucide-react'
import Card from '../../components/ui/Card'
import StatusBadge from '../../components/ui/StatusBadge'
import Button from '../../components/ui/Button'

const INITIAL = [
  { id: 1, staff: '홍길동', role: '행사 안내 스태프', shift: '주말 행사 스태프 · 5월 10일 09:00–18:00', wage: '시급 13,000원', status: 'pending', sentAt: '2026-05-06' },
  { id: 2, staff: '이영희', role: '부스 운영 보조', shift: '주말 행사 스태프 · 5월 11일 09:00–18:00', wage: '시급 13,000원', status: 'accepted', sentAt: '2026-05-05' },
  { id: 3, staff: '김철수', role: '행사 진행 보조', shift: '6월 박람회 안내 · 6월 1일 10:00–19:00', wage: '시급 12,500원', status: 'confirmed', sentAt: '2026-05-04' },
  { id: 4, staff: '박지수', role: '안내 데스크', shift: '6월 박람회 안내 · 6월 2일 10:00–19:00', wage: '시급 12,500원', status: 'pending', sentAt: '2026-05-06' },
  { id: 5, staff: '최민준', role: '행사 안내 스태프', shift: '주말 행사 스태프 · 5월 10일 09:00–18:00', wage: '시급 13,000원', status: 'rejected', sentAt: '2026-05-03' },
]

const TABS = [
  { key: 'all',       label: '전체' },
  { key: 'pending',   label: '응답 대기' },
  { key: 'accepted',  label: '수락됨' },
  { key: 'confirmed', label: '확정됨' },
  { key: 'rejected',  label: '거절됨' },
]

const STATUS_META = {
  pending:   { label: '응답 대기', color: 'text-amber-600 bg-amber-50 border-amber-200' },
  accepted:  { label: '수락됨',   color: 'text-blue-600 bg-blue-50 border-blue-200' },
  confirmed: { label: '확정됨',   color: 'text-green-600 bg-green-50 border-green-200' },
  rejected:  { label: '거절됨',   color: 'text-red-500 bg-red-50 border-red-200' },
}

function StatusPill({ status }) {
  const meta = STATUS_META[status] || { label: status, color: 'text-gray-500 bg-gray-50 border-gray-200' }
  return (
    <span className={`inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
      {meta.label}
    </span>
  )
}

export default function InvitationsPage() {
  const [invitations, setInvitations] = useState(INITIAL)
  const [tab, setTab] = useState('all')

  const filtered = invitations.filter(inv => tab === 'all' || inv.status === tab)

  function confirm(id) {
    setInvitations(prev => prev.map(inv => inv.id === id ? { ...inv, status: 'confirmed' } : inv))
  }
  function reject(id) {
    setInvitations(prev => prev.map(inv => inv.id === id ? { ...inv, status: 'rejected' } : inv))
  }
  function resend(id) {
    setInvitations(prev => prev.map(inv => inv.id === id ? { ...inv, status: 'pending', sentAt: '2026-05-07' } : inv))
  }

  const counts = {
    pending:   invitations.filter(i => i.status === 'pending').length,
    accepted:  invitations.filter(i => i.status === 'accepted').length,
    confirmed: invitations.filter(i => i.status === 'confirmed').length,
    rejected:  invitations.filter(i => i.status === 'rejected').length,
  }

  return (
    <div className="space-y-5">
      {/* 헤더 */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-navy">초대/확정 관리</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            스태프 초대 현황 — 대기 <strong className="text-amber-600">{counts.pending}건</strong>,
            수락 <strong className="text-blue-600">{counts.accepted}건</strong>,
            확정 <strong className="text-green-600">{counts.confirmed}건</strong>
          </p>
        </div>
        <Button icon={Send}>초대 발송</Button>
      </div>

      {/* 요약 카드 */}
      <div className="grid grid-cols-4 gap-3">
        {[
          { label: '응답 대기', value: counts.pending,   icon: Clock,     color: 'text-amber-500', bg: 'bg-amber-50' },
          { label: '수락됨',   value: counts.accepted,  icon: Mail,      color: 'text-blue-500',  bg: 'bg-blue-50' },
          { label: '확정됨',   value: counts.confirmed, icon: UserCheck, color: 'text-green-600', bg: 'bg-green-50' },
          { label: '거절됨',   value: counts.rejected,  icon: X,         color: 'text-red-400',   bg: 'bg-red-50' },
        ].map(({ label, value, icon: Icon, color, bg }) => (
          <Card key={label}>
            <div className="flex items-center gap-3">
              <div className={`w-9 h-9 rounded-lg ${bg} flex items-center justify-center shrink-0`}>
                <Icon size={16} className={color} />
              </div>
              <div>
                <p className="text-xl font-bold text-navy tabular-nums">{value}</p>
                <p className="text-xs text-gray-500">{label}</p>
              </div>
            </div>
          </Card>
        ))}
      </div>

      {/* 탭 */}
      <div className="flex gap-1 border-b border-offwhite-200">
        {TABS.map(t => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`px-4 py-2.5 text-sm font-semibold transition-colors border-b-2 -mb-px ${
              tab === t.key
                ? 'border-orange text-orange'
                : 'border-transparent text-gray-500 hover:text-navy'
            }`}
          >
            {t.label}
            {t.key !== 'all' && counts[t.key] > 0 && (
              <span className="ml-1.5 text-xs bg-offwhite-200 text-gray-600 px-1.5 py-0.5 rounded-full">
                {counts[t.key]}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* 목록 */}
      <Card padding={false}>
        {filtered.length === 0 ? (
          <div className="py-12 text-center text-sm text-gray-400">해당 상태의 초대가 없습니다.</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-offwhite-200">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">스태프</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider hidden md:table-cell">Shift · 급여</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">상태</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">발송일</th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody>
              {filtered.map(inv => (
                <tr key={inv.id} className="border-b border-offwhite-100 hover:bg-offwhite-100 transition-colors">
                  <td className="px-5 py-3.5">
                    <p className="font-semibold text-navy">{inv.staff}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{inv.role}</p>
                  </td>
                  <td className="px-5 py-3.5 hidden md:table-cell">
                    <p className="text-gray-700">{inv.shift}</p>
                    <p className="text-xs text-orange font-medium mt-0.5">{inv.wage}</p>
                  </td>
                  <td className="px-5 py-3.5">
                    <StatusPill status={inv.status} />
                  </td>
                  <td className="px-5 py-3.5 text-gray-500">{inv.sentAt}</td>
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-2 justify-end">
                      {inv.status === 'accepted' && (
                        <>
                          <button
                            onClick={() => confirm(inv.id)}
                            className="flex items-center gap-1 text-xs font-semibold text-green-600 bg-green-50 hover:bg-green-100 border border-green-200 px-2.5 py-1.5 rounded-lg transition-colors"
                          >
                            <Check size={12} />확정
                          </button>
                          <button
                            onClick={() => reject(inv.id)}
                            className="flex items-center gap-1 text-xs font-semibold text-red-500 bg-red-50 hover:bg-red-100 border border-red-200 px-2.5 py-1.5 rounded-lg transition-colors"
                          >
                            <X size={12} />거절
                          </button>
                        </>
                      )}
                      {inv.status === 'rejected' && (
                        <button
                          onClick={() => resend(inv.id)}
                          className="flex items-center gap-1 text-xs font-semibold text-navy bg-offwhite hover:bg-offwhite-200 border border-offwhite-200 px-2.5 py-1.5 rounded-lg transition-colors"
                        >
                          <Send size={12} />재발송
                        </button>
                      )}
                      {inv.status === 'pending' && (
                        <span className="text-xs text-gray-400 flex items-center gap-1">
                          <Clock size={11} />응답 대기 중
                        </span>
                      )}
                      {inv.status === 'confirmed' && (
                        <span className="text-xs text-green-600 flex items-center gap-1 font-medium">
                          <UserCheck size={11} />확정 완료
                        </span>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  )
}
