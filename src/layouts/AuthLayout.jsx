import { Outlet } from 'react-router-dom'
import SidebarLogo from '../components/sidebar/SidebarLogo'

export default function AuthLayout() {
  return (
    <div className="min-h-screen bg-navy flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="flex justify-center mb-8">
          <SidebarLogo collapsed={false} />
        </div>
        <div className="bg-white rounded-2xl shadow-xl p-8">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
