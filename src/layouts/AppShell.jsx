import { Outlet } from 'react-router-dom'
import { useSidebar } from '../hooks/useSidebar'
import { useSwipeGesture } from '../hooks/useSwipeGesture'
import Sidebar from '../components/sidebar/Sidebar'
import TopBar from '../components/topbar/TopBar'
import PageContainer from './PageContainer'
import ToastContainer from '../components/ui/ToastContainer'

export default function AppShell() {
  const { collapsed, toggleSidebar, mobileOpen, openMobile, closeMobile } = useSidebar()
  const { handleTouchStart, handleTouchEnd } = useSwipeGesture({
    isOpen: mobileOpen,
    onOpen: openMobile,
    onClose: closeMobile,
  })

  return (
    <div
      className="flex h-screen bg-offwhite overflow-hidden"
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      {/* 모바일 백드롭 */}
      {mobileOpen && (
        <div
          className="fixed inset-0 z-[1100] bg-black/50 md:hidden"
          onClick={closeMobile}
        />
      )}

      <Sidebar collapsed={collapsed} onToggle={toggleSidebar} mobileOpen={mobileOpen} onClose={closeMobile} />
      <div className="flex flex-col flex-1 min-w-0 overflow-hidden">
        <TopBar onOpenMobile={openMobile} />
        <main className="flex-1 overflow-y-auto">
          <PageContainer>
            <Outlet />
          </PageContainer>
        </main>
      </div>
      <ToastContainer />
    </div>
  )
}
