import Card from '../../components/ui/Card'
import StatusBadge from '../../components/ui/StatusBadge'
import EmptyState from '../../components/ui/EmptyState'
import { FileCheck } from 'lucide-react'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'
import { MOCK_APPLICANTS } from '../../data/mockApplicants'

const applicantMap = Object.fromEntries(MOCK_APPLICANTS.map(a => [a.id, a]))

export default function ContractsPage() {
  const { shifts, jobs } = useAppData()
  const { user } = useAuth()

  const isAdmin = user?.role === 'ADMIN'
  const myJobIds = isAdmin ? null : new Set(jobs.filter(j => j.createdBy === user?.name).map(j => j.id))
  const myShifts = isAdmin ? shifts : shifts.filter(s => myJobIds.has(s.jobId))

  // 완료된 Shift에서 채용 확정 인원 추출
  const contracts = myShifts
    .filter(s => s.status === 'completed')
    .flatMap(s => {
      const hired = s.applicantStates
        ? s.applicantStates.filter(a => a.status === 'hired')
        : (s.applicantIds ?? []).slice(0, s.confirmedStaff).map(id => ({ id }))

      const d = new Date(s.date + 'T00:00:00')
      const label = `${s.jobTitle} · ${d.getMonth() + 1}월 ${d.getDate()}일`

      return hired.map(a => ({
        id: `${s.id}-${a.id}`,
        staffName: applicantMap[a.id]?.name ?? a.id,
        shift: label,
        status: 'completed',
      }))
    })

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-bold text-navy">계약 상태</h1>
        <p className="text-sm text-gray-500 mt-0.5">총 {contracts.length}건의 계약</p>
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
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Shift</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">계약 상태</th>
              </tr>
            </thead>
            <tbody>
              {contracts.map(c => (
                <tr key={c.id} className="border-b border-offwhite-100 last:border-0 hover:bg-offwhite-100 transition-colors">
                  <td className="px-5 py-3 font-medium text-navy">{c.staffName}</td>
                  <td className="px-5 py-3 text-gray-600">{c.shift}</td>
                  <td className="px-5 py-3"><StatusBadge status={c.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  )
}
