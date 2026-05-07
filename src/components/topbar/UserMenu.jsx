import { useRef, useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronDown, LogOut, Settings, User } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import Avatar from '../ui/Avatar'

const ROLE_LABEL = {
  BUSINESS:   '기업 회원',
  INDIVIDUAL: '개인 회원',
  ADMIN:      '플랫폼 운영자',
}

export default function UserMenu() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const ref = useRef(null)

  const name = user?.name ?? '사용자'
  const roleLabel = ROLE_LABEL[user?.role] ?? user?.roleLabel ?? ''

  useEffect(() => {
    function handleClickOutside(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  function handleLogout() {
    logout()
    navigate('/login')
  }

  const settingsPath = user?.role === 'BUSINESS' ? '/settings/company'
    : user?.role === 'ADMIN' ? '/admin/settings'
    : '/individual/profile'

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen(v => !v)}
        className="flex items-center gap-2 px-2 py-1.5 rounded-lg hover:bg-offwhite-100 transition-colors duration-150 group"
      >
        <Avatar initials={name.slice(0, 1)} size="sm" />
        <div className="text-left hidden sm:block">
          <p className="text-sm font-semibold text-navy leading-tight">{name}</p>
          <p className="text-xs text-gray-400 leading-tight">{roleLabel}</p>
        </div>
        <ChevronDown size={14} className={`text-gray-400 transition-transform ${open ? 'rotate-180' : ''}`} />
      </button>

      {open && (
        <div className="absolute right-0 top-full mt-1.5 w-52 bg-white rounded-xl border border-offwhite-200 shadow-lg py-1.5 z-50">
          <div className="px-4 py-2.5 border-b border-offwhite-200">
            <p className="text-sm font-semibold text-navy">{name}</p>
            <p className="text-xs text-gray-400">{roleLabel}</p>
          </div>
          <button
            onClick={() => { setOpen(false); navigate(settingsPath) }}
            className="w-full flex items-center gap-2.5 px-4 py-2 text-sm text-navy hover:bg-offwhite-100 transition-colors"
          >
            <Settings size={15} className="text-gray-400" />
            설정
          </button>
          <button
            onClick={() => { setOpen(false); handleLogout() }}
            className="w-full flex items-center gap-2.5 px-4 py-2 text-sm text-red-500 hover:bg-red-50 transition-colors"
          >
            <LogOut size={15} />
            로그아웃
          </button>
        </div>
      )}
    </div>
  )
}
