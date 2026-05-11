import { useState } from 'react'

export function useSidebar() {
  const [collapsed, setCollapsed] = useState(() => {
    return localStorage.getItem('staffnow_sidebar_collapsed') === 'true'
  })
  const [mobileOpen, setMobileOpen] = useState(false)

  const toggleSidebar = () => {
    setCollapsed(prev => {
      const next = !prev
      localStorage.setItem('staffnow_sidebar_collapsed', String(next))
      return next
    })
  }

  const openMobile  = () => setMobileOpen(true)
  const closeMobile = () => setMobileOpen(false)

  return { collapsed, toggleSidebar, mobileOpen, openMobile, closeMobile }
}
