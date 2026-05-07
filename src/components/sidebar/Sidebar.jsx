import clsx from 'clsx'
import { NAV_GROUPS } from '../../constants/navigation'
import SidebarLogo from './SidebarLogo'
import SidebarNavGroup from './SidebarNavGroup'
import SidebarToggleButton from './SidebarToggleButton'

export default function Sidebar({ collapsed, onToggle }) {
  return (
    <aside
      className={clsx(
        'flex flex-col bg-navy h-screen shrink-0 transition-all duration-200 ease-in-out overflow-hidden',
        'shadow-[2px_0_8px_rgba(27,43,72,0.12)]',
        collapsed ? 'w-16' : 'w-64'
      )}
    >
      <SidebarLogo collapsed={collapsed} />
      <div className="border-t border-navy-700" />
      <nav className="flex-1 overflow-y-auto py-3 space-y-1 scrollbar-none">
        {NAV_GROUPS.map((group, i) => (
          <SidebarNavGroup key={i} {...group} collapsed={collapsed} />
        ))}
      </nav>
      <SidebarToggleButton collapsed={collapsed} onToggle={onToggle} />
    </aside>
  )
}
