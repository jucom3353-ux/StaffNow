import { Briefcase, Calendar, Mail, DollarSign } from 'lucide-react'
import StatsCard from '../../components/dashboard/StatsCard'
import QuickActions from '../../components/dashboard/QuickActions'
import RecentActivity from '../../components/dashboard/RecentActivity'
import ActiveJobsSummary from '../../components/dashboard/ActiveJobsSummary'
import UpcomingShifts from '../../components/dashboard/UpcomingShifts'
import { useAppData } from '../../context/AppDataContext'

const today = new Date().toLocaleDateString('ko-KR', {
  year: 'numeric', month: 'long', day: 'numeric', weekday: 'short',
})

export default function DashboardPage() {
  const { jobs, shifts, invitations } = useAppData()

  const activeJobsCount      = jobs.filter(j => j.status === 'active').length
  const scheduledShiftsCount = shifts.filter(s => s.status === 'scheduled' || s.status === 'in_progress').length
  const pendingInviteCount   = invitations.filter(i => i.status === 'pending').length
  const unpaidCount          = shifts.filter(s => s.status === 'completed' && !s.isPaid).length

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold text-navy">대시보드</h1>
        <p className="text-sm text-gray-500 mt-0.5">{today} 기준 운영 현황</p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard
          icon={Briefcase}
          label="진행 중인 공고"
          value={activeJobsCount}
          delta={{ value: activeJobsCount - 4, dir: activeJobsCount > 4 ? 'up' : activeJobsCount < 4 ? 'down' : 'neutral', label: '초기 대비' }}
          to="/jobs"
        />
        <StatsCard
          icon={Calendar}
          label="예정/진행 Shift"
          value={scheduledShiftsCount}
          delta={{ value: scheduledShiftsCount - 3, dir: scheduledShiftsCount > 3 ? 'up' : 'neutral', label: '초기 대비' }}
          to="/shifts"
        />
        <StatsCard
          icon={Mail}
          label="대기 중인 초대"
          value={pendingInviteCount}
          delta={{ value: pendingInviteCount, dir: pendingInviteCount > 0 ? 'up' : 'neutral', label: '응답 대기' }}
          to="/invitations"
        />
        <StatsCard
          icon={DollarSign}
          label="미정산 건수"
          value={unpaidCount}
          delta={{ value: unpaidCount, dir: unpaidCount > 0 ? 'up' : 'neutral', label: '정산 필요' }}
          accentColor="orange"
          to="/payroll"
        />
      </div>

      <QuickActions />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ActiveJobsSummary />
        <UpcomingShifts />
      </div>

      <RecentActivity />
    </div>
  )
}
