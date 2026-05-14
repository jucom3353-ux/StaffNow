import { useRef, useState, useEffect } from 'react'
import { Bell, Briefcase, UserCheck, Mail, X } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

const TYPE_ICON = { job: Briefcase, invite: Mail, staff: UserCheck }

function getStorageKey(email) {
  return `staffnow_notif_${email?.replace(/[^a-zA-Z0-9]/g, '_') || 'guest'}`
}

function loadSaved(key) {
  try { return JSON.parse(localStorage.getItem(key) || '[]') } catch { return [] }
}

export default function NotificationBell() {
  const { user } = useAuth()
  const storageKey = getStorageKey(user?.email)

  const [open, setOpen] = useState(false)
  const [notifications, setNotifications] = useState(() => loadSaved(storageKey))
  const ref = useRef(null)

  const unread = notifications.filter(n => !n.isRead).length

  useEffect(() => {
    try { localStorage.setItem(storageKey, JSON.stringify(notifications)) } catch {}
  }, [notifications, storageKey])

  useEffect(() => {
    function handleClickOutside(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  function handleOpen() {
    setOpen(v => {
      if (!v) setNotifications(ns => ns.map(n => ({ ...n, isRead: true })))
      return !v
    })
  }

  function markOneRead(id) {
    setNotifications(ns => ns.map(n => n.id === id ? { ...n, isRead: true } : n))
  }

  function dismiss(id) {
    setNotifications(ns => ns.filter(n => n.id !== id))
  }

  return (
    <div ref={ref} className="relative">
      <button
        onClick={handleOpen}
        className="relative p-2 rounded-lg text-gray-500 hover:bg-offwhite-100 hover:text-navy transition-colors duration-150"
      >
        <Bell size={20} />
        {unread > 0 && (
          <span className="absolute top-1 right-1 w-4 h-4 bg-orange text-white text-xs font-bold rounded-full flex items-center justify-center leading-none">
            {unread > 9 ? '9+' : unread}
          </span>
        )}
      </button>

      {open && (
        <>
          <div className="fixed inset-0 z-[149] sm:hidden" onClick={() => setOpen(false)} />
          <div className="
            fixed left-2 right-2 top-[4.25rem]
            sm:absolute sm:left-auto sm:right-0 sm:top-full sm:mt-1.5 sm:w-80
            bg-white rounded-xl border border-offwhite-200 shadow-xl z-[150] overflow-hidden
          ">
            <div className="flex items-center justify-between px-4 py-3 border-b border-offwhite-200">
              <div className="flex items-center gap-2">
                <span className="font-bold text-navy text-sm">알림</span>
                {unread > 0 && (
                  <span className="text-xs bg-orange text-white font-bold px-1.5 py-0.5 rounded-full">{unread}</span>
                )}
              </div>
              <button
                onClick={() => setOpen(false)}
                className="sm:hidden p-1 rounded-lg text-gray-400 hover:bg-offwhite hover:text-gray-600 transition-colors"
              >
                <X size={15} />
              </button>
            </div>

            <div className="max-h-[60vh] sm:max-h-72 overflow-y-auto divide-y divide-offwhite-200">
              {notifications.length === 0 ? (
                <p className="text-sm text-gray-400 text-center py-8">알림이 없습니다.</p>
              ) : (
                notifications.map(n => {
                  const Icon = TYPE_ICON[n.type] ?? Bell
                  return (
                    <div
                      key={n.id}
                      onClick={() => markOneRead(n.id)}
                      className={`flex items-start gap-3 px-4 py-3.5 hover:bg-offwhite-100 transition-colors cursor-pointer ${!n.isRead ? 'bg-orange-50/40' : ''}`}
                    >
                      <div className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 mt-0.5 ${!n.isRead ? 'bg-orange-100 text-orange' : 'bg-offwhite text-gray-400'}`}>
                        <Icon size={15} />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className={`text-sm leading-snug ${!n.isRead ? 'text-navy font-medium' : 'text-gray-600'}`}>{n.text}</p>
                        <p className="text-xs text-gray-400 mt-1">{n.time}</p>
                      </div>
                      <button
                        onClick={e => { e.stopPropagation(); dismiss(n.id) }}
                        className="p-1 rounded-lg hover:bg-offwhite text-gray-300 hover:text-gray-500 shrink-0 mt-0.5"
                      >
                        <X size={14} />
                      </button>
                    </div>
                  )
                })
              )}
            </div>
          </div>
        </>
      )}
    </div>
  )
}
