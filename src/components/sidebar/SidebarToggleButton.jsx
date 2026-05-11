import { ChevronLeft, ChevronRight, X } from 'lucide-react'

export default function SidebarToggleButton({ collapsed, onToggle, onClose }) {
  return (
    <div className="border-t border-navy-700">
      {/* 데스크탑: 접기/펼치기 */}
      <button
        onClick={onToggle}
        className="hidden md:flex items-center justify-center h-12 text-navy-200 hover:text-white hover:bg-navy-700 transition-colors duration-150 w-full"
        aria-label={collapsed ? '사이드바 펼치기' : '사이드바 접기'}
      >
        {collapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
      </button>
      {/* 모바일: 닫기 */}
      <button
        onClick={onClose}
        className="flex md:hidden items-center justify-center gap-2 h-12 text-navy-200 hover:text-white hover:bg-navy-700 transition-colors duration-150 w-full text-sm"
      >
        <X size={16} />
        <span>닫기</span>
      </button>
    </div>
  )
}
