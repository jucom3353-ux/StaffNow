import { Link } from 'react-router-dom'
import { Plus, CalendarPlus, UserCheck, Mail } from 'lucide-react'
import clsx from 'clsx'

const ACTIONS = [
  { icon: Plus,         label: '공고 생성',      path: '/jobs/create',  primary: true  },
  { icon: CalendarPlus, label: 'Shift 생성',      path: '/shifts/create', primary: true  },
  { icon: UserCheck,    label: '추천 인력 확인',   path: '/staff',        primary: false },
  { icon: Mail,         label: '초대/확정 여부',   path: '/invitations',  primary: false },
]

export default function QuickActions() {
  return (
    <div>
      <h2 className="text-sm font-semibold text-gray-500 mb-3 uppercase tracking-wider">빠른 액션</h2>
      <div className="flex flex-wrap gap-3">
        {ACTIONS.map(({ icon: Icon, label, path, primary }) => (
          <Link
            key={path}
            to={path}
            className={clsx(
              'flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors duration-150',
              primary
                ? 'bg-orange text-white hover:bg-orange-600 active:bg-orange-700'
                : 'bg-white text-navy border border-navy-100 hover:bg-offwhite active:bg-offwhite-200'
            )}
          >
            <Icon size={16} />
            {label}
          </Link>
        ))}
      </div>
    </div>
  )
}
