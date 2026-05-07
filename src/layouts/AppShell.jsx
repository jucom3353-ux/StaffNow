import { Outlet } from 'react-router-dom'
import { useSidebar } from '../hooks/useSidebar'
import Sidebar from '../components/sidebar/Sidebar'
import TopBar from '../components/topbar/TopBar'
import PageContainer from './PageContainer'
import ToastContainer from '../components/ui/ToastContainer'

export default function AppShell() {
  const { collapsed, toggleSidebar } = useSidebar()

  return (
    <div className="flex h-screen bg-offwhite overflow-hidden">
      <Sidebar collapsed={collapsed} onToggle={toggleSidebar} />
      <div className="flex flex-col flex-1 min-w-0 overflow-hidden">
        <TopBar />
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
