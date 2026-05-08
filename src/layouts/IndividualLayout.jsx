import { useState } from 'react'
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { Home, Search, ClipboardList, Heart, MessageSquare, User, ChevronLeft } from 'lucide-react'
import clsx from 'clsx'
import { useAuth } from '../context/AuthContext'
import NotificationBell from '../components/topbar/NotificationBell'
import UserMenu from '../components/topbar/UserMenu'

const NAV = [
  { to: '/individual',              icon: Home,          label: '홈',        end: true },
  { to: '/individual/jobs',         icon: Search,        label: '공고 검색' },
  { to: '/individual/applications', icon: ClipboardList, label: '지원 현황' },
  { to: '/individual/saved',        icon: Heart,         label: '관심 공고' },
  { to: '/individual/messages',     icon: MessageSquare, label: '메시지' },
  { to: '/individual/profile',      icon: User,          label: '내 프로필' },
]

export default function IndividualLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const { user } = useAuth()
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState('')

  function handleSearch(e) {
    e.preventDefault()
    navigate(`/individual/jobs${searchQuery.trim() ? `?q=${encodeURIComponent(searchQuery.trim())}` : ''}`)
    setSearchQuery('')
  }

  return (
    <div className="flex h-screen bg-offwhite overflow-hidden">
      {/* 사이드바 */}
      <aside className={clsx(
        'flex flex-col bg-navy shrink-0 transition-all duration-200',
        collapsed ? 'w-16' : 'w-64'
      )}>
        {/* 로고 */}
        <button
          onClick={() => navigate('/individual')}
          className={clsx('flex items-center h-16 shrink-0 px-4 hover:opacity-80 transition-opacity', collapsed ? 'justify-center' : 'gap-2.5')}
        >
          <img src="/logo.png" alt="StaffNow" className="w-8 h-8 object-contain shrink-0" />
          {!collapsed && (
            <div className="overflow-hidden">
              <span className="text-white font-bold text-base leading-tight block whitespace-nowrap">StaffNow</span>
              <span className="text-navy-200 text-xs leading-tight block whitespace-nowrap">구직자 포털</span>
            </div>
          )}
        </button>

        {/* 유저 정보 */}
        {!collapsed && (
          <div className="mx-3 mb-3 p-3 bg-navy-700 rounded-xl">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-full bg-orange/20 border border-orange/30 flex items-center justify-center shrink-0">
                <span className="text-orange text-sm font-bold">{user?.avatar}</span>
              </div>
              <div className="min-w-0">
                <p className="text-white text-sm font-semibold truncate">{user?.name}</p>
                <p className="text-navy-200 text-xs truncate">{user?.roleLabel}</p>
              </div>
            </div>
          </div>
        )}

        {/* 네비게이션 */}
        <nav className="flex-1 px-2 space-y-0.5 overflow-y-auto">
          {NAV.map(item => {
            const Icon = item.icon
            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) => clsx(
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all',
                  isActive
                    ? 'bg-navy-700 text-white border-l-2 border-orange'
                    : 'text-navy-200 hover:bg-navy-700 hover:text-white',
                  collapsed && 'justify-center'
                )}
              >
                <Icon size={18} className="shrink-0" />
                {!collapsed && <span>{item.label}</span>}
              </NavLink>
            )
          })}
        </nav>

        {/* 하단: 접기 버튼만 */}
        <div className="p-2 border-t border-navy-700">
          <button
            onClick={() => setCollapsed(v => !v)}
            className={clsx(
              'w-full flex items-center gap-3 px-3 py-2 rounded-lg text-navy-200 hover:text-white text-xs transition-all',
              collapsed ? 'justify-center' : ''
            )}
          >
            <ChevronLeft size={16} className={clsx('transition-transform', collapsed && 'rotate-180')} />
            {!collapsed && <span>접기</span>}
          </button>
        </div>
      </aside>

      {/* 메인 영역 */}
      <div className="flex flex-col flex-1 min-w-0 overflow-hidden">
        {/* 탑바 */}
        <header className="h-16 flex items-center px-6 gap-4 bg-white border-b border-offwhite-200 shrink-0">
          <form onSubmit={handleSearch} className="flex items-center gap-2 bg-offwhite rounded-lg px-3 py-1.5 w-56">
            <Search size={15} className="text-gray-400 shrink-0" />
            <input
              type="text"
              placeholder="공고 검색..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="bg-transparent text-sm text-navy placeholder-gray-400 outline-none w-full"
            />
          </form>
          <div className="flex-1" />
          <NotificationBell />
          <div className="w-px h-6 bg-offwhite-200" />
          <UserMenu />
        </header>
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
