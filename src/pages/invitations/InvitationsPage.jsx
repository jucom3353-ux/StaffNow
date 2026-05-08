import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Mail, Check, X, Clock, UserCheck, Send, Search, ExternalLink } from 'lucide-react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'
import { MOCK_APPLICANTS } from '../../data/mockApplicants'

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
  const meta = STATUS_META[status] ?? { label: status, color: 'text-gray-500 bg-gray-50 border-gray-200' }
  return (
    <span className={`inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
      {meta.label}
    </span>
  )
}

// ── 초대 발송 모달 ────────────────────────────────────────
function SendModal({ myShifts, onSend, onClose }) {
  const [staffSearch, setStaffSearch] = useState('')
  const [selectedStaff, setSelectedStaff] = useState(null)
  const [shiftId, setShiftId] = useState('')
  const [role, setRole] = useState('')
  const [wage, setWage] = useState('시급 13,000원')

  const staffList = MOCK_APPLICANTS.filter(a =>
    !staffSearch || a.name.includes(staffSearch) || a.region.includes(staffSearch)
  ).slice(0, 8)

  const selectedShift = myShifts.find(s => s.id === shiftId)

  function handleSend() {
    if (!selectedStaff || !shiftId || !role) return
    const d = new Date(selectedShift.date + 'T00:00:00')
    onSend({
      staffId: selectedStaff.id,
      staffName: selectedStaff.name,
      role,
      shiftId,
      shiftLabel: `${selectedShift.jobTitle} · ${d.getMonth() + 1}월 ${d.getDate()}일 ${selectedShift.startTime}–${selectedShift.endTime}`,
      jobId: selectedShift.jobId,
      wage,
    })
  }

  const canSend = selectedStaff && shiftId && role.trim()

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md mx-4 p-6 space-y-5" onClick={e => e.stopPropagation()}>
        <h2 className="text-base font-bold text-navy">초대 발송</h2>

        {/* 스태프 선택 */}
        <div className="space-y-2">
          <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">스태프 선택 *</label>
          <div className="flex items-center gap-2 border border-offwhite-200 rounded-lg px-3 py-2">
            <Search size={13} className="text-gray-400 shrink-0" />
            <input
              type="text"
              placeholder="이름, 지역 검색"
              value={staffSearch}
              onChange={e => { setStaffSearch(e.target.value); setSelectedStaff(null) }}
              className="flex-1 text-sm outline-none text-navy placeholder-gray-400"
            />
          </div>
          {selectedStaff ? (
            <div className="flex items-center justify-between bg-navy-50 border border-navy-100 rounded-lg px-3 py-2">
              <span className="text-sm font-semibold text-navy">
                {selectedStaff.name} <span className="font-normal text-gray-500">· {selectedStaff.region}</span>
              </span>
              <button onClick={() => setSelectedStaff(null)} className="text-gray-400 hover:text-navy">
                <X size={14} />
              </button>
            </div>
          ) : (
            <div className="border border-offwhite-200 rounded-lg overflow-hidden max-h-40 overflow-y-auto">
              {staffList.map(a => (
                <button
                  key={a.id}
                  onClick={() => { setSelectedStaff(a); setStaffSearch('') }}
                  className="w-full text-left px-3 py-2 text-sm hover:bg-offwhite-100 transition-colors flex items-center justify-between"
                >
                  <span className="font-medium text-navy">{a.name}</span>
                  <span className="text-xs text-gray-400">{a.age}세 · {a.region}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Shift 선택 */}
        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Shift 선택 *</label>
          <select
            value={shiftId}
            onChange={e => setShiftId(e.target.value)}
            className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm text-navy outline-none focus:border-navy bg-white"
          >
            <option value="">Shift를 선택하세요</option>
            {myShifts.map(s => {
              const d = new Date(s.date + 'T00:00:00')
              return (
                <option key={s.id} value={s.id}>
                  {s.jobTitle} · {d.getMonth() + 1}월 {d.getDate()}일 {s.startTime}–{s.endTime}
                </option>
              )
            })}
          </select>
        </div>

        {/* 역할 */}
        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">역할 *</label>
          <input
            type="text"
            placeholder="예: 행사 안내 스태프"
            value={role}
            onChange={e => setRole(e.target.value)}
            className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm text-navy outline-none focus:border-navy"
          />
        </div>

        {/* 급여 */}
        <div className="space-y-1.5">
          <label className="text-xs font-semibold text-gray-500 uppercase tracking-wide">급여</label>
          <input
            type="text"
            value={wage}
            onChange={e => setWage(e.target.value)}
            className="w-full border border-offwhite-200 rounded-lg px-3 py-2 text-sm text-navy outline-none focus:border-navy"
          />
        </div>

        <div className="flex gap-2 pt-1">
          <button
            onClick={onClose}
            className="flex-1 py-2.5 rounded-xl border border-offwhite-200 text-sm font-semibold text-gray-500 hover:bg-offwhite transition-colors"
          >
            취소
          </button>
          <button
            onClick={handleSend}
            disabled={!canSend}
            className="flex-1 py-2.5 rounded-xl bg-orange text-white text-sm font-bold hover:bg-orange-600 transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <Send size={14} />발송
          </button>
        </div>
      </div>
    </div>
  )
}

// ── 메인 페이지 ───────────────────────────────────────────
export default function InvitationsPage() {
  const { invitations, addInvitation, updateInvitationStatus, jobs, shifts } = useAppData()
  const { user } = useAuth()
  const [tab, setTab] = useState('all')
  const [showModal, setShowModal] = useState(false)

  const isAdmin = user?.role === 'ADMIN'
  const myJobIds = isAdmin ? null : new Set(jobs.filter(j => j.createdBy === user?.name).map(j => j.id))
  const myShifts = isAdmin ? shifts : shifts.filter(s => myJobIds.has(s.jobId))
  const myInvitations = isAdmin ? invitations : invitations.filter(i => myJobIds.has(i.jobId))

  const filtered = myInvitations.filter(i => tab === 'all' || i.status === tab)

  const counts = {
    pending:   myInvitations.filter(i => i.status === 'pending').length,
    accepted:  myInvitations.filter(i => i.status === 'accepted').length,
    confirmed: myInvitations.filter(i => i.status === 'confirmed').length,
    rejected:  myInvitations.filter(i => i.status === 'rejected').length,
  }

  function handleSend(data) {
    addInvitation(data)
    setShowModal(false)
  }

  return (
    <div className="space-y-5">
      {showModal && (
        <SendModal
          myShifts={myShifts}
          onSend={handleSend}
          onClose={() => setShowModal(false)}
        />
      )}

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
        <Button icon={Send} onClick={() => setShowModal(true)}>초대 발송</Button>
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
        {TABS.map(t => {
          const cnt = t.key === 'all' ? myInvitations.length : (counts[t.key] ?? 0)
          return (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`px-4 py-2.5 text-sm font-semibold transition-colors border-b-2 -mb-px flex items-center gap-1.5 ${
                tab === t.key ? 'border-orange text-orange' : 'border-transparent text-gray-500 hover:text-navy'
              }`}
            >
              {t.label}
              {cnt > 0 && (
                <span className={`text-xs tabular-nums px-1.5 rounded-md ${tab === t.key ? 'bg-orange/10 text-orange' : 'bg-offwhite-200 text-gray-500'}`}>
                  {cnt}
                </span>
              )}
            </button>
          )
        })}
      </div>

      {/* 목록 */}
      <Card padding={false}>
        {filtered.length === 0 ? (
          <div className="py-12 text-center text-sm text-gray-400">해당 상태의 초대가 없습니다.</div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-offwhite-200 bg-offwhite-100">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">스태프</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider hidden md:table-cell">Shift · 급여</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">상태</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">발송일</th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody>
              {filtered.map(inv => (
                <tr key={inv.id} className="border-b border-offwhite-100 last:border-0 hover:bg-offwhite-100 transition-colors">
                  <td className="px-5 py-3.5">
                    <p className="font-semibold text-navy">{inv.staffName}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{inv.role}</p>
                  </td>
                  <td className="px-5 py-3.5 hidden md:table-cell">
                    <p className="text-gray-700">{inv.shiftLabel}</p>
                    <p className="text-xs text-orange font-medium mt-0.5">{inv.wage}</p>
                  </td>
                  <td className="px-5 py-3.5"><StatusPill status={inv.status} /></td>
                  <td className="px-5 py-3.5 text-gray-500 tabular-nums">{inv.sentAt}</td>
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-2 justify-end">
                      {inv.status === 'accepted' && (
                        <>
                          <button
                            onClick={() => updateInvitationStatus(inv.id, 'confirmed')}
                            className="flex items-center gap-1 text-xs font-semibold text-green-600 bg-green-50 hover:bg-green-100 border border-green-200 px-2.5 py-1.5 rounded-lg transition-colors"
                          >
                            <Check size={12} />확정
                          </button>
                          <button
                            onClick={() => updateInvitationStatus(inv.id, 'rejected')}
                            className="flex items-center gap-1 text-xs font-semibold text-red-500 bg-red-50 hover:bg-red-100 border border-red-200 px-2.5 py-1.5 rounded-lg transition-colors"
                          >
                            <X size={12} />거절
                          </button>
                        </>
                      )}
                      {inv.status === 'rejected' && (
                        <button
                          onClick={() => updateInvitationStatus(inv.id, 'pending')}
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
                        <div className="flex items-center gap-2">
                          <span className="text-xs text-green-600 flex items-center gap-1 font-medium">
                            <UserCheck size={11} />확정 완료
                          </span>
                          {inv.shiftId && (
                            <Link
                              to={`/shifts/${inv.shiftId}`}
                              className="flex items-center gap-1 text-xs font-semibold text-navy bg-offwhite-100 hover:bg-navy hover:text-white border border-offwhite-200 hover:border-navy px-2.5 py-1.5 rounded-lg transition-colors"
                            >
                              <ExternalLink size={10} />Shift 보기
                            </Link>
                          )}
                        </div>
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
