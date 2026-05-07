import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Search } from 'lucide-react'
import PageBreadcrumb from './PageBreadcrumb'
import NotificationBell from './NotificationBell'
import UserMenu from './UserMenu'

export default function TopBar() {
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
    <header className="h-16 flex items-center px-6 gap-4 bg-white border-b border-offwhite-200 shrink-0">
      <PageBreadcrumb />
      <div className="flex-1" />
      <form onSubmit={handleSearch} className="hidden md:flex items-center gap-2 bg-offwhite rounded-lg px-3 py-1.5 w-56">
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
