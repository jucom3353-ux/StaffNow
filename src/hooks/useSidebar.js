import { useState } from 'react'

export function useSidebar() {
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)

  const toggleSidebar = () => setCollapsed(prev => !prev)
  const openMobile  = () => setMobileOpen(true)
  const closeMobile = () => setMobileOpen(false)

  return { collapsed, toggleSidebar, mobileOpen, openMobile, closeMobile }
}
