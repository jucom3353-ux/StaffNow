import { useState, useEffect } from 'react'
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { Home, Search, ClipboardList, Heart, MessageSquare, User, ChevronLeft, Clock, Menu, X, Banknote, Sparkles, FileCheck } from 'lucide-react'
import clsx from 'clsx'
import { useAuth } from '../context/AuthContext'
import NotificationBell from '../components/topbar/NotificationBell'
import UserMenu from '../components/topbar/UserMenu'
import { useSwipeGesture } from '../hooks/useSwipeGesture'
import AppFooter from '../components/ui/AppFooter'

const NAV = [
  { to: '/individual',              icon: Home,          label: '홈',        end: true },
  { to: '/individual/jobs',         icon: Search,        label: '공고 검색' },
  { to: '/individual/applications', icon: ClipboardList, label: '지원 현황' },
  { to: '/individual/saved',        icon: Heart,         label: '관심 공고' },
  { to: '/individual/attendance',   icon: Clock,         label: '출퇴근 관리' },
  { to: '/individual/messages',     icon: MessageSquare, label: '메시지' },
  { to: '/individual/profile',      icon: User,          label: '내 프로필' },
  { to: '/individual/payroll',      icon: Banknote,      label: '급여 명세' },
  { to: '/individual/contracts',   icon: FileCheck,     label: '근로계약서' },
]

export default function IndividualLayout() {
  const [collapsed,   setCollapsed]   = useState(false)
  const [mobileOpen,  setMobileOpen]  = useState(false)
  const [showWelcome, setShowWelcome] = useState(false)
  const { user } = useAuth()
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState('')

  useEffect(() => {
    if (sessionStorage.getItem('staffnow_new_signup') === 'true') {
      sessionStorage.removeItem('staffnow_new_signup')
      setShowWelcome(true)
    }
  }, [])

  const { handleTouchStart, handleTouchEnd } = useSwipeGesture({
    isOpen: mobileOpen,
    onOpen:  () => setMobileOpen(true),
    onClose: () => setMobileOpen(false),
  })

  function handleSearch(e) {
    e.preventDefault()
    navigate(`/individual/jobs${searchQuery.trim() ? `?q=${encodeURIComponent(searchQuery.trim())}` : ''}`)
    setSearchQuery('')
    setMobileOpen(false)
  }

  function handleNavClick() {
    // 모바일에서 메뉴 선택 시 드로어 닫기
    setMobileOpen(false)
  }

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

      {/* ── 사이드바 ──
          모바일: fixed 드로어 (콘텐츠 위에 overlay)
          데스크탑: flex 레이아웃 안에 포함 (콘텐츠 밀기)
      */}
      <aside className={clsx(
        'flex flex-col bg-navy transition-all duration-200',
        // 모바일: fixed overlay — translate로 슬라이드 인/아웃
        'fixed inset-y-0 left-0 z-[1200] w-64',
        mobileOpen ? 'translate-x-0' : '-translate-x-full',
        // 데스크탑: flex 흐름 복귀, translate 리셋
        'md:relative md:inset-auto md:z-auto md:translate-x-0 md:shrink-0',
        collapsed ? 'md:w-16' : 'md:w-64',
      )}>

        {/* 로고 */}
        <button
          onClick={() => { navigate('/individual'); handleNavClick() }}
          className={clsx('flex items-center h-16 shrink-0 px-4 hover:opacity-80 transition-opacity', collapsed ? 'md:justify-center' : 'gap-2.5')}
        >
          <img src="/logo.png" alt="StaffNow" className="w-8 h-8 object-contain shrink-0" />
          <div className={clsx('overflow-hidden', collapsed && 'md:hidden')}>
            <span className="text-white font-bold text-base leading-tight block whitespace-nowrap">StaffNow</span>
            <span className="text-navy-200 text-xs leading-tight block whitespace-nowrap">구직자 포털</span>
          </div>
        </button>

        {/* 유저 정보 */}
        <div className={clsx('mx-3 mb-3 p-3 bg-navy-700 rounded-xl', collapsed && 'md:hidden')}>
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

        {/* 네비게이션 */}
        <nav className="flex-1 px-2 space-y-0.5 overflow-y-auto">
          {NAV.map(item => {
            const Icon = item.icon
            return (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                onClick={handleNavClick}
                className={({ isActive }) => clsx(
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all',
                  isActive
                    ? 'bg-navy-700 text-white border-l-2 border-orange'
                    : 'text-navy-200 hover:bg-navy-700 hover:text-white',
                  collapsed && 'md:justify-center'
                )}
              >
                <Icon size={18} className="shrink-0" />
                <span className={clsx(collapsed && 'md:hidden')}>{item.label}</span>
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

      {/* ── 신규 가입 프로필 완성 팝업 ── */}
      {showWelcome && (
        <div className="fixed inset-0 z-[2000] flex items-center justify-center bg-black/50 px-4">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 text-center">
            <div className="w-14 h-14 rounded-full bg-orange/10 flex items-center justify-center mx-auto mb-4">
              <Sparkles size={28} className="text-orange" />
            </div>
            <h3 className="text-lg font-extrabold text-navy mb-1">환영합니다, {user?.name}님!</h3>
            <p className="text-sm text-gray-500 mb-1">StaffNow 가입을 축하드려요.</p>
            <p className="text-sm text-gray-500 mb-5">
              프로필을 완성하면 기업 담당자에게<br />더 잘 노출될 수 있어요.
            </p>
            <div className="space-y-2">
              <button
                onClick={() => { setShowWelcome(false); navigate('/individual/profile') }}
                className="w-full py-3 bg-orange text-white font-bold rounded-xl hover:bg-orange-600 transition-colors text-sm"
              >
                지금 프로필 작성하기
              </button>
              <button
                onClick={() => setShowWelcome(false)}
                className="w-full py-2.5 text-sm text-gray-400 hover:text-navy transition-colors"
              >
                나중에 하기
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── 메인 영역 ── */}
      <div className="flex flex-col flex-1 min-w-0 overflow-hidden">
        {/* 탑바 */}
        <header className="h-16 flex items-center px-4 md:px-6 gap-3 bg-white border-b border-offwhite-200 shrink-0">
          {/* 햄버거 버튼 — 모바일 전용 */}
          <button
            onClick={() => setMobileOpen(true)}
            className="md:hidden p-1.5 rounded-lg text-gray-500 hover:bg-offwhite transition-colors shrink-0"
          >
            <Menu size={20} />
          </button>

          <form onSubmit={handleSearch} className="flex items-center gap-2 bg-offwhite rounded-lg px-3 py-1.5 flex-1 max-w-xs md:w-56 md:flex-none">
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
        <main className="flex-1 overflow-y-auto p-4 md:p-6 flex flex-col">
          <div className="flex-1"><Outlet /></div>
          <AppFooter />
        </main>
      </div>
    </div>
  )
}
