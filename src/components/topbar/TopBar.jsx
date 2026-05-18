import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Search, Menu } from 'lucide-react'
import PageBreadcrumb from './PageBreadcrumb'
import NotificationBell from './NotificationBell'
import UserMenu from './UserMenu'

export default function TopBar({ onOpenMobile }) {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')

  function handleSearch(e) {
    e.preventDefault()
    if (query.trim()) {
      navigate(`/jobs?q=${encodeURIComponent(query.trim())}`)
      setQuery('')
    }
  }

  return (
    <header className="h-16 flex items-center px-4 md:px-6 gap-3 bg-white border-b border-offwhite-200 shrink-0">
      {/* 햄버거 버튼 — 모바일 전용 */}
      {onOpenMobile && (
        <button
          onClick={onOpenMobile}
          className="md:hidden p-1.5 rounded-lg text-gray-500 hover:bg-offwhite transition-colors shrink-0"
        >
          <Menu size={20} />
        </button>
      )}
      <PageBreadcrumb />
      <div className="flex-1 hidden md:block" />
      <form onSubmit={handleSearch} className="flex flex-1 md:flex-none md:w-80 items-center gap-2 bg-offwhite rounded-lg px-3 py-1.5">
        <Search size={15} className="text-gray-400 shrink-0" />
        <input
          type="text"
          value={query}
          onChange={e => setQuery(e.target.value)}
          placeholder="공고 검색..."
          className="bg-transparent text-sm text-navy placeholder-gray-400 outline-none w-full"
        />
      </form>
      <NotificationBell />
      <div className="w-px h-6 bg-offwhite-200" />
      <UserMenu />
    </header>
  )
}
