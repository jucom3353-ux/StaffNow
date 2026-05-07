import { useState } from 'react'

export function useSidebar() {
  const [collapsed, setCollapsed] = useState(() => {
    return localStorage.getItem('staffnow_sidebar_collapsed') === 'true'
  })

  const toggleSidebar = () => {
    setCollapsed(prev => {
      const next = !prev
      localStorage.setItem('staffnow_sidebar_collapsed', String(next))
      return next
    })
  }

  return { collapsed, toggleSidebar }
}
