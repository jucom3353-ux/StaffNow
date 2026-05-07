import { useRef, useState, useEffect, useCallback } from 'react'
import { Bell, Briefcase, UserCheck, Mail, X, PlusCircle } from 'lucide-react'

const STORAGE_KEY = 'staffnow_notif'

const BASE_NOTIFICATIONS = [
  { id: 1, type: 'job',    text: '공고 "주말 행사 스태프" 마감 3일 전입니다.',           time: '10분 전'  },
  { id: 2, type: 'invite', text: '홍길동님이 Shift 초대를 수락했습니다.',                  time: '32분 전'  },
  { id: 3, type: 'invite', text: '이영희님이 "5월 10일 오전 Shift" 초대를 수락했습니다.', time: '1시간 전' },
  { id: 4, type: 'staff',  text: '새로운 추천 인력 3명이 등록되었습니다.',                 time: '2시간 전' },
  { id: 5, type: 'job',    text: '공고 "6월 박람회 안내 스태프" 지원자가 8명입니다.',      time: '어제'     },
]

const TYPE_ICON = { job: Briefcase, invite: Mail, staff: UserCheck }

function loadSaved() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
  } catch {
    return {}
  }
}

function buildInitialNotifications() {
  const saved = loadSaved()
  const readIds      = new Set(saved.readIds      || [])
  const dismissedIds = new Set(saved.dismissedIds || [])
  return BASE_NOTIFICATIONS
    .filter(n => !dismissedIds.has(n.id))
    .map(n => ({ ...n, isRead: readIds.has(n.id) }))
}

export default function NotificationBell() {
  const [open, setOpen] = useState(false)
  const [notifications, setNotifications] = useState(buildInitialNotifications)
  const ref = useRef(null)

  const unread = notifications.filter(n => !n.isRead).length

  // Persist read/dismissed state whenever notifications change
  useEffect(() => {
    const readIds      = notifications.filter(n => n.isRead).map(n => n.id)
    // Merge dismissed IDs with what's already saved (dismissed = no longer in state)
    const saved        = loadSaved()
    const prevDismissed = new Set(saved.dismissedIds || [])
    const currentIds   = new Set(notifications.map(n => n.id))
    // IDs that were in BASE_NOTIFICATIONS but are no longer in state = dismissed
    BASE_NOTIFICATIONS.forEach(n => {
      if (!currentIds.has(n.id)) prevDismissed.add(n.id)
    })
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({
        readIds,
        dismissedIds: [...prevDismissed],
      }))
    } catch {}
  }, [notifications])

  // Outside click closes dropdown
  useEffect(() => {
    function handleClickOutside(e) {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false)
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  function handleOpen() {
    setOpen(v => {
      if (!v) {
        // Bell click marks all current notifications as read
        setNotifications(ns => ns.map(n => ({ ...n, isRead: true })))
      }
      return !v
    })
  }

  function markOneRead(id) {
    setNotifications(ns => ns.map(n => n.id === id ? { ...n, isRead: true } : n))
  }

  function dismiss(id) {
    setNotifications(ns => ns.filter(n => n.id !== id))
  }

  const addDemoNotification = useCallback(() => {
    const demoTexts = [
      '김서연님이 "5월 10일 Shift" 초대를 수락했습니다.',
      '새로운 지원자 5명이 shift-demo에 지원했습니다.',
      '공고 "주말 행사 스태프" 정원이 80% 채워졌습니다.',
      '박준호님이 Shift 초대를 거절했습니다.',
      '이번 주 Shift 2건이 시작 예정입니다.',
    ]
    const id = Date.now()
    setNotifications(prev => [{
      id,
      type: ['job', 'invite', 'staff'][Math.floor(Math.random() * 3)],
      text: demoTexts[Math.floor(Math.random() * demoTexts.length)],
      time: '방금',
      isRead: false,
    }, ...prev])
  }, [])

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
        <div className="absolute right-0 top-full mt-1.5 w-80 bg-white rounded-xl border border-offwhite-200 shadow-lg z-50 overflow-hidden">
          <div className="flex items-center justify-between px-4 py-3 border-b border-offwhite-200">
            <div className="flex items-center gap-2">
              <span className="font-bold text-navy text-sm">알림</span>
              {unread > 0 && (
                <span className="text-xs bg-orange text-white font-bold px-1.5 py-0.5 rounded-full">{unread}</span>
              )}
            </div>
          </div>

          <div className="max-h-72 overflow-y-auto divide-y divide-offwhite-200">
            {notifications.length === 0 ? (
              <p className="text-sm text-gray-400 text-center py-8">알림이 없습니다.</p>
            ) : (
              notifications.map(n => {
                const Icon = TYPE_ICON[n.type] ?? Bell
                return (
                  <div
                    key={n.id}
                    onClick={() => markOneRead(n.id)}
                    className={`flex items-start gap-3 px-4 py-3 hover:bg-offwhite-100 transition-colors cursor-pointer ${!n.isRead ? 'bg-orange-50/40' : ''}`}
                  >
                    <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 mt-0.5 ${!n.isRead ? 'bg-orange-100 text-orange' : 'bg-offwhite text-gray-400'}`}>
                      <Icon size={14} />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className={`text-xs leading-relaxed ${!n.isRead ? 'text-navy font-medium' : 'text-gray-600'}`}>{n.text}</p>
                      <p className="text-[11px] text-gray-400 mt-0.5">{n.time}</p>
                    </div>
                    <button
                      onClick={e => { e.stopPropagation(); dismiss(n.id) }}
                      className="p-0.5 rounded hover:bg-offwhite text-gray-300 hover:text-gray-500 shrink-0 mt-0.5"
                    >
                      <X size={13} />
                    </button>
                  </div>
                )
              })
            )}
          </div>

          {/* 데모용: 새 알림 생성 버튼 */}
          <div className="px-4 py-2.5 border-t border-offwhite-200">
            <button
              onClick={addDemoNotification}
              className="w-full flex items-center justify-center gap-1.5 py-2 rounded-lg text-xs font-semibold text-gray-500 hover:bg-offwhite-100 hover:text-navy border border-dashed border-offwhite-200 transition-colors"
            >
              <PlusCircle size={13} />테스트 알림 추가
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
