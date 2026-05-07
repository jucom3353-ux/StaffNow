import { useState } from 'react'
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { Zap, BarChart3, Users, Building2, Briefcase, Flag, Settings, ChevronLeft, LogOut, Bell, ShieldCheck } from 'lucide-react'
import clsx from 'clsx'
import { useAuth } from '../context/AuthContext'

const NAV = [
  { to: '/admin',            icon: BarChart3,  label: '대시보드',   end: true },
  { to: '/admin/users',      icon: Users,      label: '유저 관리' },
  { to: '/admin/businesses', icon: Building2,  label: '기업 인증',  badge: 7 },
  { to: '/admin/jobs',       icon: Briefcase,  label: '공고 관리' },
  { to: '/admin/reports',    icon: Flag,       label: '신고/제재',  badge: 3 },
  { to: '/admin/settings',   icon: Settings,   label: '시스템 설정' },
]

export default function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false)
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex h-screen bg-offwhite overflow-hidden">
      {/* 사이드바 */}
      <aside className={clsx(
        'flex flex-col bg-navy shrink-0 transition-all duration-200',
        collapsed ? 'w-16' : 'w-64'
      )}>
        {/* 로고 */}
        <div className={clsx('flex items-center h-16 shrink-0 px-4', collapsed ? 'justify-center' : 'gap-2.5')}>
          <div className="w-8 h-8 rounded-lg bg-orange flex items-center justify-center shrink-0">
            <Zap size={18} className="text-white fill-white" />
          </div>
          {!collapsed && (
            <div className="overflow-hidden">
              <span className="text-white font-bold text-base leading-tight block whitespace-nowrap">StaffNow</span>
              <span className="text-navy-200 text-xs leading-tight block whitespace-nowrap">관리자 포털</span>
            </div>
          )}
        </div>

        {/* 어드민 배지 */}
        {!collapsed && (
          <div className="mx-3 mb-3 p-3 bg-orange/10 border border-orange/20 rounded-xl">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-full bg-orange/20 flex items-center justify-center shrink-0">
                <ShieldCheck size={15} className="text-orange" />
              </div>
              <div>
                <p className="text-white text-sm font-semibold">{user?.name}</p>
                <p className="text-orange text-xs font-medium">플랫폼 운영자</p>
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
                {!collapsed && (
                  <span className="flex-1">{item.label}</span>
                )}
                {!collapsed && item.badge && (
                  <span className="bg-orange text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full min-w-[18px] text-center">
                    {item.badge}
                  </span>
                )}
              </NavLink>
            )
          })}
        </nav>

        {/* 하단 */}
        <div className="p-2 border-t border-navy-700">
          <button
            onClick={handleLogout}
            className={clsx(
              'w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-navy-200 hover:bg-navy-700 hover:text-white text-sm transition-all',
              collapsed && 'justify-center'
            )}
          >
            <LogOut size={18} className="shrink-0" />
            {!collapsed && <span>로그아웃</span>}
          </button>
          <button
            onClick={() => setCollapsed(v => !v)}
            className={clsx(
              'w-full flex items-center gap-3 px-3 py-2 rounded-lg text-navy-200 hover:text-white text-xs transition-all mt-0.5',
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
        <header className="h-16 flex items-center px-6 gap-4 bg-white border-b border-offwhite-200 shrink-0">
          <div className="flex-1" />
          <button className="relative p-2 rounded-lg hover:bg-offwhite-100 transition-colors">
            <Bell size={18} className="text-gray-500" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-orange rounded-full" />
          </button>
          <div className="w-px h-6 bg-offwhite-200" />
          <div className="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-offwhite-100 cursor-pointer">
            <div className="w-7 h-7 rounded-full bg-orange flex items-center justify-center">
              <span className="text-white text-xs font-bold">{user?.avatar}</span>
            </div>
            <span className="text-sm font-semibold text-navy hidden sm:block">{user?.name}</span>
          </div>
        </header>
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
