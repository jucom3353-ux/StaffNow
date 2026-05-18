import { useState } from 'react'
import { Outlet, NavLink } from 'react-router-dom'
import { Zap, BarChart3, Users, Building2, Briefcase, Flag, Settings, ChevronLeft, ShieldCheck, Menu, X } from 'lucide-react'
import { useSwipeGesture } from '../hooks/useSwipeGesture'
import clsx from 'clsx'
import { useAuth } from '../context/AuthContext'
import NotificationBell from '../components/topbar/NotificationBell'
import UserMenu from '../components/topbar/UserMenu'
import { PENDING_BUSINESSES } from '../data/mockAdmin'
import { ADMIN_REPORTS } from '../data/mockAdmin'

export default function AdminLayout() {
  const [collapsed,  setCollapsed]  = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const { user } = useAuth()

  const { handleTouchStart, handleTouchEnd } = useSwipeGesture({
    isOpen:  mobileOpen,
    onOpen:  () => setMobileOpen(true),
    onClose: () => setMobileOpen(false),
  })

  const bizPending     = PENDING_BUSINESSES.length
  const reportsPending = ADMIN_REPORTS.filter(r => r.status === 'pending').length

  const NAV = [
    { to: '/admin',            icon: BarChart3,  label: '대시보드',   end: true },
    { to: '/admin/users',      icon: Users,      label: '유저 관리' },
    { to: '/admin/businesses', icon: Building2,  label: '기업 인증',  badge: bizPending || null },
    { to: '/admin/jobs',       icon: Briefcase,  label: '공고 관리' },
    { to: '/admin/reports',    icon: Flag,       label: '신고/제재',  badge: reportsPending || null },
    { to: '/admin/settings',   icon: Settings,   label: '시스템 설정' },
  ]

  return (
    <div
      className="flex h-screen bg-offwhite overflow-hidden"
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >

      {/* ── 모바일 백드롭 ── */}
      {mobileOpen && (
        <div
          className="fixed inset-0 z-[1100] bg-black/50 md:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}

      {/* ── 사이드바 ── */}
      <aside className={clsx(
        'flex flex-col bg-navy transition-all duration-200',
        // 모바일: fixed overlay 드로어
        'fixed inset-y-0 left-0 z-[1200] w-64',
        mobileOpen ? 'translate-x-0' : '-translate-x-full',
        // 데스크탑: flex 흐름, translate 리셋
        'md:relative md:inset-auto md:z-auto md:translate-x-0 md:shrink-0',
        collapsed ? 'md:w-16' : 'md:w-64',
      )}>
        {/* 로고 */}
        <div className={clsx('flex items-center h-16 shrink-0 px-4', collapsed ? 'md:justify-center' : 'gap-2.5')}>
          <div className="w-8 h-8 rounded-lg bg-orange flex items-center justify-center shrink-0">
            <Zap size={18} className="text-white fill-white" />
          </div>
          <div className={clsx('overflow-hidden', collapsed && 'md:hidden')}>
            <span className="text-white font-bold text-base leading-tight block whitespace-nowrap">StaffNow</span>
            <span className="text-navy-200 text-xs leading-tight block whitespace-nowrap">관리자 포털</span>
          </div>
        </div>

        {/* 어드민 배지 */}
        <div className={clsx('mx-3 mb-3 p-3 bg-orange/10 border border-orange/20 rounded-xl', collapsed && 'md:hidden')}>
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

        {/* 네비게이션 */}
        <nav className="flex-1 px-2 space-y-0.5 overflow-y-auto">
          {NAV.map(item => {
            const Icon = item.icon
            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                onClick={() => setMobileOpen(false)}
                className={({ isActive }) => clsx(
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all',
                  isActive
                    ? 'bg-navy-700 text-white border-l-2 border-orange'
                    : 'text-navy-200 hover:bg-navy-700 hover:text-white',
                  collapsed && 'md:justify-center'
                )}
              >
                <Icon size={18} className="shrink-0" />
                <span className={clsx('flex-1', collapsed && 'md:hidden')}>{item.label}</span>
                {item.badge && (
                  <span className={clsx('bg-orange text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full min-w-[18px] text-center', collapsed && 'md:hidden')}>
                    {item.badge}
                  </span>
                )}
              </NavLink>
            )
          })}
        </nav>

        {/* 하단 버튼 */}
        <div className="p-2 border-t border-navy-700">
          {/* 데스크탑: 접기/펼치기 */}
          <button
            onClick={() => setCollapsed(v => !v)}
            className={clsx(
              'w-full hidden md:flex items-center gap-3 px-3 py-2 rounded-lg text-navy-200 hover:text-white text-xs transition-all',
              collapsed ? 'justify-center' : ''
            )}
          >
            <ChevronLeft size={16} className={clsx('transition-transform', collapsed && 'rotate-180')} />
            {!collapsed && <span>접기</span>}
          </button>
          {/* 모바일: 닫기 버튼 */}
          <button
            onClick={() => setMobileOpen(false)}
            className="w-full flex md:hidden items-center gap-3 px-3 py-2 rounded-lg text-navy-200 hover:text-white text-xs transition-all"
          >
            <X size={16} />
            <span>닫기</span>
          </button>
        </div>
      </aside>

      {/* ── 메인 영역 ── */}
      <div className="flex flex-col flex-1 min-w-0 overflow-hidden">
        <header className="h-16 flex items-center px-4 md:px-6 gap-3 bg-white border-b border-offwhite-200 shrink-0">
          {/* 햄버거 버튼 — 모바일 전용 */}
          <button
            onClick={() => setMobileOpen(true)}
            className="md:hidden p-1.5 rounded-lg text-gray-500 hover:bg-offwhite transition-colors shrink-0"
          >
            <Menu size={20} />
          </button>
          <div className="flex-1" />
          <NotificationBell />
          <div className="w-px h-6 bg-offwhite-200" />
          <UserMenu />
        </header>
        <main className="flex-1 overflow-y-auto p-4 md:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
