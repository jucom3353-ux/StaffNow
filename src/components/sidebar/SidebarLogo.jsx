import clsx from 'clsx'
import { Zap } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function SidebarLogo({ collapsed, className }) {
  return (
    <Link
      to="/dashboard"
      className={clsx(
        'flex items-center h-16 px-4 shrink-0 rounded-lg',
        'hover:bg-navy-700 transition-colors duration-150',
        className
      )}
    >
      <div className="flex items-center gap-2.5">
        <div className="w-8 h-8 rounded-lg bg-orange flex items-center justify-center shrink-0">
          <Zap size={18} className="text-white fill-white" />
        </div>
        {!collapsed && (
          <div className="overflow-hidden">
            <span className="text-white font-bold text-base leading-tight block whitespace-nowrap">
              StaffNow
            </span>
            <span className="text-navy-200 text-xs leading-tight block whitespace-nowrap">
              기업 운영 대시보드
            </span>
          </div>
        )}
      </div>
    </Link>
  )
}
