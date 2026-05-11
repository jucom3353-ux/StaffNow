import clsx from 'clsx'
import { useMemo } from 'react'
import { NAV_GROUPS } from '../../constants/navigation'
import { useAppData } from '../../context/AppDataContext'
import SidebarLogo from './SidebarLogo'
import SidebarNavGroup from './SidebarNavGroup'
import SidebarToggleButton from './SidebarToggleButton'

export default function Sidebar({ collapsed, onToggle, mobileOpen, onClose }) {
  const { conversations } = useAppData()

  const unreadTotal = useMemo(() =>
    conversations
      .filter(c => !c.left && !c.blocked)
      .reduce((sum, c) => sum + c.messages.filter(m => !m.read && m.from === 'staff').length, 0)
  , [conversations])

  const navGroups = useMemo(() =>
    NAV_GROUPS.map(group => ({
      ...group,
      items: group.items.map(item =>
        item.path === '/messages' ? { ...item, badge: unreadTotal } : item
      ),
    }))
  , [unreadTotal])

  return (
    <aside
      className={clsx(
        'flex flex-col bg-navy h-screen transition-all duration-200 ease-in-out overflow-hidden',
        'shadow-[2px_0_8px_rgba(27,43,72,0.12)]',
        // 모바일: fixed overlay 드로어 (Leaflet z-index 최대 1000 이상으로 설정)
        'fixed inset-y-0 left-0 z-[1200] w-64',
        mobileOpen ? 'translate-x-0' : '-translate-x-full',
        // 데스크탑: flex 흐름으로 복귀
        'md:relative md:inset-auto md:z-auto md:translate-x-0 md:shrink-0',
        collapsed ? 'md:w-16' : 'md:w-64',
      )}
    >
      <SidebarLogo collapsed={collapsed} />
      <div className="border-t border-navy-700" />
      <nav className="flex-1 overflow-y-auto py-3 space-y-1 scrollbar-none">
        {navGroups.map((group, i) => (
          <SidebarNavGroup key={i} {...group} collapsed={collapsed} onNavClick={onClose} />
        ))}
      </nav>
      <SidebarToggleButton collapsed={collapsed} onToggle={onToggle} onClose={onClose} />
    </aside>
  )
}
