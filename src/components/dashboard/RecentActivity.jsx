import {
  Briefcase, Mail, Calendar, FileCheck, DollarSign, Play,
} from 'lucide-react'
import Card from '../ui/Card'
import EmptyState from '../ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'

const TYPE_CONFIG = {
  job_created:     { icon: Briefcase, iconBg: 'bg-navy-50',    iconColor: 'text-navy'        },
  invitation_sent: { icon: Mail,      iconBg: 'bg-orange-50',  iconColor: 'text-orange'      },
  shift_confirmed: { icon: Calendar,  iconBg: 'bg-green-50',   iconColor: 'text-green-600'   },
  contract_signed: { icon: FileCheck, iconBg: 'bg-blue-50',    iconColor: 'text-blue-600'    },
  payroll_approved:{ icon: DollarSign,iconBg: 'bg-emerald-50', iconColor: 'text-emerald-600' },
  shift_started:   { icon: Play,      iconBg: 'bg-orange-50',  iconColor: 'text-orange'      },
}

function ActivityItem({ type, text, time }) {
  const cfg = TYPE_CONFIG[type] ?? TYPE_CONFIG.job_created
  const Icon = cfg.icon
  return (
    <div className="flex items-center gap-3 py-3 border-b border-offwhite-100 last:border-0">
      <div className={`w-7 h-7 rounded-lg flex items-center justify-center shrink-0 ${cfg.iconBg}`}>
        <Icon size={13} className={cfg.iconColor} />
      </div>
      <p className="text-sm text-navy leading-snug flex-1 min-w-0">{text}</p>
      <span className="text-xs text-gray-400 whitespace-nowrap shrink-0">{time}</span>
    </div>
  )
}

export default function RecentActivity() {
  const { activities } = useAppData()
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'
  const myActivities = isAdmin
    ? activities
    : activities.filter(a => a.actor === user?.name)

  return (
    <Card
      padding={false}
      header={
        <div className="flex items-center gap-2">
          <span className="text-sm font-bold text-navy">최근 활동</span>
          {myActivities.length > 0 && (
            <span className="bg-offwhite-100 text-gray-500 text-xs font-bold px-1.5 py-0.5 rounded-md tabular-nums">
              {myActivities.length}
            </span>
          )}
        </div>
      }
    >
      {myActivities.length === 0 ? (
        <EmptyState icon={Calendar} title="최근 활동이 없습니다" description="공고를 생성하거나 Shift를 운영하면 활동 내역이 표시됩니다" />
      ) : (
        <div className="px-5">
          {myActivities.slice(0, 8).map(item => (
            <ActivityItem key={item.id} {...item} />
          ))}
        </div>
      )}
    </Card>
  )
}
