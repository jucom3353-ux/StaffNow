import Card from '../../components/ui/Card'
import Avatar from '../../components/ui/Avatar'
import Button from '../../components/ui/Button'
import { Plus } from 'lucide-react'

const MEMBERS = [
  { id: 1, name: '김운영', email: 'admin@jucompany.kr', role: '관리자', initials: '김' },
  { id: 2, name: '박담당', email: 'manager@jucompany.kr', role: '운영 담당', initials: '박' },
]

export default function MemberManagementPage() {
  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-navy">멤버 관리</h1>
        <Button icon={Plus} size="sm">멤버 초대</Button>
      </div>
      <Card padding={false}>
        {MEMBERS.map(m => (
          <div key={m.id} className="flex items-center gap-4 px-5 py-4 border-b border-offwhite-100 last:border-0">
            <Avatar initials={m.initials} size="md" />
            <div className="flex-1">
              <p className="font-semibold text-navy text-sm">{m.name}</p>
              <p className="text-xs text-gray-500">{m.email}</p>
            </div>
            <span className="text-xs font-medium text-navy-200 bg-navy-50 px-2.5 py-1 rounded-full">{m.role}</span>
          </div>
        ))}
      </Card>
    </div>
  )
}
