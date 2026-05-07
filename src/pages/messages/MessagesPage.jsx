import EmptyState from '../../components/ui/EmptyState'
import { MessageSquare } from 'lucide-react'

export default function MessagesPage() {
  return (
    <div className="space-y-5">
      <h1 className="text-xl font-bold text-navy">메시지</h1>
      <EmptyState icon={MessageSquare} title="메시지 기능 준비 중" description="스태프와의 메시지 기능은 곧 지원될 예정입니다." />
    </div>
  )
}
