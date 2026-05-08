import { useState } from 'react'
import { Link } from 'react-router-dom'
import { FileCheck, X, Download, CheckCircle2, Building2, User, ClipboardList } from 'lucide-react'
import Card from '../../components/ui/Card'
import EmptyState from '../../components/ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'
import { MOCK_APPLICANTS } from '../../data/mockApplicants'

const applicantMap = Object.fromEntries(MOCK_APPLICANTS.map(a => [a.id, a]))
const HOURLY_RATE = 13000

const STATUS_META = {
  completed: { label: '계약 완료', color: 'text-green-600 bg-green-50 border-green-200' },
  pending:   { label: '서명 대기', color: 'text-amber-600 bg-amber-50 border-amber-200' },
}

function ContractModal({ contract, onClose }) {
  const staff = applicantMap[contract.staffId]
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={onClose}>
      <div
        className="bg-white rounded-2xl shadow-xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto"
        onClick={e => e.stopPropagation()}
      >
        {/* 헤더 */}
        <div className="flex items-center justify-between px-6 pt-6 pb-4 border-b border-offwhite-200">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-green-50 flex items-center justify-center">
              <FileCheck size={17} className="text-green-600" />
            </div>
            <div>
              <h2 className="text-base font-bold text-navy">근로 계약서</h2>
              <p className="text-xs text-gray-400">{contract.shift}</p>
            </div>
          </div>
          <button onClick={onClose} className="w-8 h-8 rounded-lg hover:bg-offwhite-100 flex items-center justify-center text-gray-400">
            <X size={16} />
          </button>
        </div>

        {/* 계약서 본문 */}
        <div className="px-6 py-5 space-y-5 text-sm">

          {/* 당사자 정보 */}
          <div className="grid grid-cols-2 gap-3">
            <div className="bg-offwhite-100 rounded-xl p-4">
              <div className="flex items-center gap-2 mb-2">
                <Building2 size={13} className="text-navy" />
                <span className="text-xs font-bold text-navy uppercase tracking-wider">사업주</span>
              </div>
              <p className="font-semibold text-navy text-sm">(주)스태프나우</p>
              <p className="text-xs text-gray-400 mt-0.5">서울특별시 강남구 테헤란로 123</p>
              <p className="text-xs text-gray-400">대표: 김대표</p>
            </div>
            <div className="bg-offwhite-100 rounded-xl p-4">
              <div className="flex items-center gap-2 mb-2">
                <User size={13} className="text-navy" />
                <span className="text-xs font-bold text-navy uppercase tracking-wider">근로자</span>
              </div>
              <p className="font-semibold text-navy text-sm">{contract.staffName}</p>
              {staff && (
                <>
                  <p className="text-xs text-gray-400 mt-0.5">{staff.region}</p>
                  <p className="text-xs text-gray-400">만 {staff.age}세</p>
                </>
              )}
            </div>
          </div>

          {/* 계약 조건 */}
          <div>
            <p className="text-xs font-bold text-navy uppercase tracking-wider mb-3">계약 조건</p>
            <div className="space-y-2">
              {[
                { label: '근무 Shift', value: contract.shift },
                { label: '근무 시간', value: contract.workHours ?? `${contract.hours ?? 9}시간` },
                { label: '시급', value: `₩${HOURLY_RATE.toLocaleString('ko-KR')}` },
                { label: '총 급여', value: contract.amount ? `₩${contract.amount.toLocaleString('ko-KR')}` : '정산 완료 후 확정' },
                { label: '담당 역할', value: contract.role },
                { label: '계약 형태', value: '단기 근로 (일일)' },
              ].map(row => (
                <div key={row.label} className="flex justify-between py-2 border-b border-offwhite-100 last:border-0">
                  <span className="text-gray-400 text-xs">{row.label}</span>
                  <span className="font-semibold text-navy text-xs">{row.value}</span>
                </div>
              ))}
            </div>
          </div>

          {/* 근로 조건 고지 */}
          <div className="bg-offwhite-100 rounded-xl px-4 py-3 text-xs text-gray-500 leading-relaxed space-y-1">
            <p>• 본 계약은 「근로기준법」에 따른 단기 근로 계약입니다.</p>
            <p>• 임금은 근로일 기준 익월 10일 이내 지급됩니다.</p>
            <p>• 근무 중 발생한 사고는 산업재해보상보험법에 따릅니다.</p>
            <p>• 무단 결근 시 해당 근무 임금은 지급되지 않습니다.</p>
          </div>

          {/* 서명 상태 */}
          <div className="flex items-center justify-between py-3 px-4 rounded-xl border border-green-200 bg-green-50">
            <div className="flex items-center gap-2">
              <CheckCircle2 size={16} className="text-green-600" />
              <span className="text-sm font-semibold text-green-700">양측 서명 완료</span>
            </div>
            <span className="text-xs text-green-500">{contract.signedAt ?? '2025-06-01'}</span>
          </div>
        </div>

        {/* 푸터 */}
        <div className="px-6 pb-6 flex gap-2">
          <button
            onClick={onClose}
            className="flex-1 py-2.5 text-sm font-semibold text-gray-500 bg-offwhite-100 hover:bg-offwhite-200 rounded-xl transition-colors"
          >
            닫기
          </button>
          <button
            onClick={() => alert('데모 버전에서는 다운로드가 지원되지 않습니다.')}
            className="flex-1 py-2.5 text-sm font-semibold text-white bg-navy hover:bg-navy-700 rounded-xl transition-colors flex items-center justify-center gap-2"
          >
            <Download size={14} />PDF 다운로드
          </button>
        </div>
      </div>
    </div>
  )
}

export default function ContractsPage() {
  const { shifts, jobs } = useAppData()
  const { user } = useAuth()
  const [selectedContract, setSelectedContract] = useState(null)

  const isAdmin = user?.role === 'ADMIN'
  const myJobIds = isAdmin ? null : new Set(jobs.filter(j => j.createdBy === user?.name).map(j => j.id))
  const myShifts = isAdmin ? shifts : shifts.filter(s => myJobIds.has(s.jobId))

  const contracts = myShifts
    .filter(s => s.status === 'completed')
    .flatMap(s => {
      const hired = s.applicantStates
        ? s.applicantStates.filter(a => a.status === 'hired')
        : (s.applicantIds ?? []).slice(0, s.confirmedStaff).map(id => ({ id }))

      const d = new Date(s.date + 'T00:00:00')
      const label = `${s.jobTitle} · ${d.getMonth() + 1}월 ${d.getDate()}일`

      return hired.map(a => {
        const att = s.attendance?.find(x => x.id === a.id)
        const calcH = att?.checkIn && att?.checkOut
          ? (() => {
              const [h1, m1] = att.checkIn.split(':').map(Number)
              const [h2, m2] = att.checkOut.split(':').map(Number)
              const mins = (h2 * 60 + m2) - (h1 * 60 + m1)
              return mins > 0 ? mins / 60 : 0
            })()
          : null
        return {
          id: `${s.id}-${a.id}`,
          staffId: a.id,
          staffName: applicantMap[a.id]?.name ?? a.id,
          role: a.role ?? '스태프',
          shift: label,
          shiftDate: s.date,
          hours: calcH ? Math.round(calcH * 10) / 10 : null,
          workHours: calcH
            ? (() => { const hh = Math.floor(calcH); const mm = Math.round((calcH - hh) * 60); return mm > 0 ? `${hh}h ${mm}m` : `${hh}h` })()
            : null,
          amount: calcH ? Math.round(calcH * HOURLY_RATE) : null,
          status: 'completed',
          signedAt: s.date,
        }
      })
    })

  return (
    <div className="space-y-5">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-navy">계약 상태</h1>
          <p className="text-sm text-gray-500 mt-0.5">총 {contracts.length}건의 계약</p>
        </div>
        <Link
          to="/attendance"
          className="flex items-center gap-2 px-4 py-2 rounded-xl border border-offwhite-200 text-navy text-sm font-semibold hover:bg-offwhite-100 transition-colors"
        >
          <ClipboardList size={14} />근태 관리
        </Link>
      </div>

      {contracts.length === 0 ? (
        <Card>
          <EmptyState
            icon={FileCheck}
            title="계약 내역이 없습니다"
            description="Shift를 완료·확정하면 계약 내역이 표시됩니다"
          />
        </Card>
      ) : (
        <Card padding={false}>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-offwhite-200 bg-offwhite-100">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">스태프</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider hidden md:table-cell">Shift</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider hidden md:table-cell">근무 시간</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">금액</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">상태</th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody>
              {contracts.map(c => {
                const meta = STATUS_META[c.status] ?? STATUS_META.completed
                return (
                  <tr key={c.id} className="border-b border-offwhite-100 last:border-0 hover:bg-offwhite-100 transition-colors">
                    <td className="px-5 py-3.5">
                      <p className="font-semibold text-navy">{c.staffName}</p>
                      <p className="text-xs text-gray-400 mt-0.5">{c.role}</p>
                    </td>
                    <td className="px-5 py-3.5 text-gray-600 hidden md:table-cell">{c.shift}</td>
                    <td className="px-5 py-3.5 text-gray-600 hidden md:table-cell tabular-nums">
                      {c.workHours ?? '—'}
                    </td>
                    <td className="px-5 py-3.5 font-semibold text-navy tabular-nums">
                      {c.amount ? `₩${c.amount.toLocaleString('ko-KR')}` : '—'}
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`inline-flex text-xs font-semibold px-2.5 py-1 rounded-full border ${meta.color}`}>
                        {meta.label}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <button
                        onClick={() => setSelectedContract(c)}
                        className="text-xs font-semibold text-navy bg-offwhite-100 hover:bg-navy hover:text-white border border-offwhite-200 hover:border-navy px-3 py-1.5 rounded-lg transition-colors"
                      >
                        계약서 보기
                      </button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </Card>
      )}

      {selectedContract && (
        <ContractModal contract={selectedContract} onClose={() => setSelectedContract(null)} />
      )}
    </div>
  )
}
