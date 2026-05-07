import { ChevronLeft, ChevronRight } from 'lucide-react'

export default function SidebarToggleButton({ collapsed, onToggle }) {
  return (
    <button
      onClick={onToggle}
      className="flex items-center justify-center h-12 border-t border-navy-700 text-navy-200 hover:text-white hover:bg-navy-700 transition-colors duration-150 w-full"
      aria-label={collapsed ? '사이드바 펼치기' : '사이드바 접기'}
    >
      {collapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
    </button>
  )
}
