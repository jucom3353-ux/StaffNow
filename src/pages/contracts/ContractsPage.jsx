import Card from '../../components/ui/Card'
import StatusBadge from '../../components/ui/StatusBadge'
import EmptyState from '../../components/ui/EmptyState'
import { FileCheck } from 'lucide-react'

const MOCK_CONTRACTS = [
  { id: 1, staff: '이영희', shift: '주말 행사 스태프 · 5월 10일', status: 'completed' },
  { id: 2, staff: '홍길동', shift: '주말 행사 스태프 · 5월 10일', status: 'pending' },
]

export default function ContractsPage() {
  return (
    <div className="space-y-5">
      <h1 className="text-xl font-bold text-navy">계약 상태</h1>
      <Card padding={false}>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-offwhite-200">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">스태프</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">Shift</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">계약 상태</th>
            </tr>
          </thead>
          <tbody>
            {MOCK_CONTRACTS.map(c => (
              <tr key={c.id} className="border-b border-offwhite-100 hover:bg-offwhite-100 transition-colors">
                <td className="px-5 py-3 font-medium text-navy">{c.staff}</td>
                <td className="px-5 py-3 text-gray-600">{c.shift}</td>
                <td className="px-5 py-3"><StatusBadge status={c.status} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
