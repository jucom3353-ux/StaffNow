import { useState, useRef, useEffect, useMemo, useCallback } from 'react'
import {
  Search, Send, MoreVertical, ChevronLeft,
  Paperclip, FileText, ImageIcon, Pencil, Trash2, X, Check, ChevronRight,
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { useNavigate } from 'react-router-dom'

// ── 유틸 ────────────────────────────────────────────────────
function formatTime(iso) {
  const d = new Date(iso)
  const now = new Date()
  const diff = now - d
  const day = 86400000
  if (diff < day && d.getDate() === now.getDate())
    return d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false })
  if (diff < 2 * day) return '어제'
  return d.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })
}
function formatFull(iso) {
  return new Date(iso).toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false })
}
function formatFileSize(bytes) {
  if (bytes < 1024) return `${bytes}B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)}MB`
}

const MOCK_CONVS = [
  {
    id: 'ic-001',
    companyName: '(주)스태프나우',
    online: true,
    blocked: false,
    left: false,
    messages: [
      { id: 'm1', from: 'company', text: '안녕하세요! 지원해 주셔서 감사합니다.', time: new Date(Date.now() - 3600000 * 2).toISOString(), read: true, deleted: false, file: null },
      { id: 'm2', from: 'me',      text: '안녕하세요, 관심 있게 봐주셔서 감사합니다.', time: new Date(Date.now() - 3600000).toISOString(), read: true, deleted: false, file: null },
      { id: 'm3', from: 'company', text: '검토 후 연락드리겠습니다. 잘 부탁드립니다!', time: new Date(Date.now() - 1800000).toISOString(), read: false, deleted: false, file: null },
    ],
  },
  {
    id: 'ic-002',
    companyName: '킨텍스',
    online: false,
    blocked: false,
    left: false,
    messages: [
      { id: 'm4', from: 'company', text: '합격을 축하합니다! 근무 일정을 확인해 주세요.', time: new Date(Date.now() - 86400000).toISOString(), read: true, deleted: false, file: null },
    ],
  },
  {
    id: 'ic-003',
    companyName: '브랜드X',
    online: false,
    blocked: false,
    left: false,
    messages: [
      { id: 'm5', from: 'company', text: '이번에는 함께하지 못하게 되어 아쉽습니다.', time: new Date(Date.now() - 86400000 * 7).toISOString(), read: true, deleted: false, file: null },
    ],
  },
]

function getKey(email) {
  const safe = email?.replace(/[^a-zA-Z0-9]/g, '_') || 'anon'
  return `staffnow_ind_messages_${safe}`
}

function loadConvs(email) {
  try {
    const raw = localStorage.getItem(getKey(email))
    if (raw) return JSON.parse(raw)
  } catch {}
  return email === 'user@staffnow.kr' ? MOCK_CONVS : []
}

function saveConvs(email, convs) {
  try { localStorage.setItem(getKey(email), JSON.stringify(convs)) } catch {}
}

// ── 서브 컴포넌트 ───────────────────────────────────────────
function Avatar({ name, online, size = 'md' }) {
  const sz = size === 'lg' ? 'w-11 h-11 text-base' : 'w-9 h-9 text-sm'
  return (
    <div className="relative shrink-0">
      <div className={`${sz} rounded-full bg-navy/10 text-navy font-bold flex items-center justify-center select-none`}>
        {name[0]}
      </div>
      {online && (
        <span className="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-green-400 border-2 border-white" />
      )}
    </div>
  )
}

function FileBubble({ file, isMe }) {
  const isImage = file.type?.startsWith('image/')
  const Icon = isImage ? ImageIcon : FileText
  return (
    <div className={`flex items-center gap-2.5 px-3.5 py-2.5 rounded-2xl border max-w-[240px]
      ${isMe ? 'bg-navy/5 border-navy/15' : 'bg-offwhite-100 border-offwhite-200'}`}>
      <div className="w-8 h-8 rounded-lg bg-orange/10 flex items-center justify-center shrink-0">
        <Icon size={15} className="text-orange" />
      </div>
      <div className="min-w-0">
        <p className="text-xs font-semibold text-navy truncate">{file.name}</p>
        <p className="text-[11px] text-gray-400">{formatFileSize(file.size)}</p>
      </div>
    </div>
  )
}

function TypingIndicator({ name }) {
  return (
    <div className="flex items-end gap-2">
      <div className="w-9 h-9 rounded-full bg-navy/10 text-navy font-bold text-sm flex items-center justify-center select-none shrink-0">
        {name[0]}
      </div>
      <div className="bg-offwhite-100 border border-offwhite-200 rounded-2xl rounded-bl-sm px-4 py-3 flex items-center gap-1">
        {[0, 1, 2].map(i => (
          <span key={i} className="w-1.5 h-1.5 rounded-full bg-gray-400 animate-bounce"
            style={{ animationDelay: `${i * 0.15}s`, animationDuration: '0.8s' }} />
        ))}
      </div>
    </div>
  )
}

// ── 메인 ────────────────────────────────────────────────────
export default function IndividualMessagesPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [conversations, setConversations] = useState(() => loadConvs(user?.email))

  const [selectedId, setSelectedId] = useState(() =>
    conversations.find(c => !c.left && !c.blocked)?.id ?? null
  )
  const [input,      setInput]      = useState('')
  const [search,     setSearch]     = useState('')
  const [mobileView, setMobileView] = useState('list')
  const [convTab,    setConvTab]    = useState('all')
  const [menuOpen,   setMenuOpen]   = useState(false)
  const [editingId,  setEditingId]  = useState(null)
  const [editText,   setEditText]   = useState('')
  const [typing,     setTyping]     = useState(false)

  const menuRef    = useRef(null)
  const fileRef    = useRef(null)
  const bottomRef  = useRef(null)
  const typingTimer = useRef(null)

  // persist
  useEffect(() => { saveConvs(user?.email, conversations) }, [conversations, user?.email])

  // scroll to bottom
  const selected = conversations.find(c => c.id === selectedId)
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [selectedId, selected?.messages?.length, typing])

  // close menu on outside click
  useEffect(() => {
    function h(e) { if (menuRef.current && !menuRef.current.contains(e.target)) setMenuOpen(false) }
    document.addEventListener('mousedown', h)
    return () => document.removeEventListener('mousedown', h)
  }, [])

  // mark as read on select
  useEffect(() => {
    if (!selectedId) return
    setConversations(prev => prev.map(c =>
      c.id === selectedId ? { ...c, messages: c.messages.map(m => ({ ...m, read: true })) } : c
    ))
  }, [selectedId])

  const visibleConvs = useMemo(() =>
    conversations
      .filter(c => !c.left && !c.blocked)
      .slice()
      .sort((a, b) => {
        const aT = a.messages.at(-1)?.time ? new Date(a.messages.at(-1).time).getTime() : 0
        const bT = b.messages.at(-1)?.time ? new Date(b.messages.at(-1).time).getTime() : 0
        return bT - aT
      })
  , [conversations])

  const totalUnread = useMemo(() =>
    visibleConvs.reduce((sum, c) => sum + c.messages.filter(m => !m.read && m.from === 'company').length, 0)
  , [visibleConvs])

  const filtered = useMemo(() => {
    let list = visibleConvs.filter(c => c.companyName.includes(search))
    if (convTab === 'unread') list = list.filter(c => c.messages.some(m => !m.read && m.from === 'company'))
    return list
  }, [visibleConvs, search, convTab])

  function getUnread(conv) {
    return conv.messages.filter(m => !m.read && m.from === 'company').length
  }

  const sendMsg = useCallback((convId, { text, file = null }) => {
    const newMsg = { id: `m-${Date.now()}`, from: 'me', text: text ?? '', time: new Date().toISOString(), read: true, deleted: false, file }
    setConversations(prev => prev.map(c => c.id === convId ? { ...c, messages: [...c.messages, newMsg] } : c))
  }, [])

  const editMsg = useCallback((convId, msgId, newText) => {
    setConversations(prev => prev.map(c => c.id === convId
      ? { ...c, messages: c.messages.map(m => m.id === msgId ? { ...m, text: newText, edited: true } : m) }
      : c
    ))
  }, [])

  const deleteMsg = useCallback((convId, msgId) => {
    setConversations(prev => prev.map(c => c.id === convId
      ? { ...c, messages: c.messages.map(m => m.id === msgId ? { ...m, deleted: true } : m) }
      : c
    ))
  }, [])

  function handleSend() {
    const text = input.trim()
    if (!text || !selectedId) return
    sendMsg(selectedId, { text })
    setInput('')
    clearTimeout(typingTimer.current)
    setTyping(true)
    typingTimer.current = setTimeout(() => setTyping(false), 2500)
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend() }
  }

  function handleFileChange(e) {
    const file = e.target.files?.[0]
    if (!file || !selectedId) return
    sendMsg(selectedId, { text: '', file: { name: file.name, size: file.size, type: file.type } })
    e.target.value = ''
    clearTimeout(typingTimer.current)
    setTyping(true)
    typingTimer.current = setTimeout(() => setTyping(false), 2000)
  }

  function startEdit(msg) { setEditingId(msg.id); setEditText(msg.text) }
  function confirmEdit() {
    if (!editText.trim() || !selectedId) return
    editMsg(selectedId, editingId, editText.trim())
    setEditingId(null); setEditText('')
  }
  function cancelEdit() { setEditingId(null); setEditText('') }

  function selectConv(id) { setSelectedId(id); setMobileView('chat'); setMenuOpen(false) }

  // ── 대화 목록 패널 ────────────────────────────────────────
  const ConvList = (
    <div className="flex flex-col h-full">
      <div className="px-4 pt-4 pb-3 border-b border-offwhite-200">
        <h2 className="text-base font-bold text-navy mb-3">메시지</h2>
        <div className="flex gap-1.5 mb-3">
          {['all', 'unread'].map(tab => (
            <button key={tab} onClick={() => setConvTab(tab)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold transition-colors
                ${convTab === tab ? 'bg-navy text-white' : 'bg-offwhite-100 text-gray-500 hover:bg-offwhite-200'}`}
            >
              {tab === 'all' ? '전체' : '안읽음'}
              {tab === 'unread' && totalUnread > 0 && (
                <span className="inline-flex items-center justify-center w-4 h-4 rounded-full bg-orange text-white text-[10px] font-bold">
                  {totalUnread > 9 ? '9+' : totalUnread}
                </span>
              )}
            </button>
          ))}
        </div>
        <div className="relative">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text" placeholder="기업 검색..." value={search} onChange={e => setSearch(e.target.value)}
            className="w-full pl-8 pr-3 py-2 text-sm bg-offwhite-100 rounded-lg border border-offwhite-200 focus:outline-none focus:border-orange focus:ring-1 focus:ring-orange/30"
          />
        </div>
      </div>

      <div className="flex-1 overflow-y-auto">
        {filtered.length === 0 ? (
          <p className="text-center text-sm text-gray-400 mt-8">검색 결과가 없습니다</p>
        ) : filtered.map(c => {
          const last = c.messages.filter(m => !m.deleted).at(-1)
          const unread = getUnread(c)
          const isSelected = c.id === selectedId
          return (
            <button key={c.id} onClick={() => selectConv(c.id)}
              className={`w-full flex items-center gap-3 px-4 py-3.5 text-left transition-colors border-b border-offwhite-100 last:border-0
                ${isSelected ? 'bg-orange/5 border-l-2 border-l-orange' : 'hover:bg-offwhite-100 border-l-2 border-l-transparent'}`}
            >
              <Avatar name={c.companyName} online={c.online} />
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between">
                  <span className={`text-sm font-semibold ${isSelected ? 'text-orange' : 'text-navy'}`}>
                    {c.companyName}
                  </span>
                  <div className="flex items-center gap-1.5 shrink-0 ml-1">
                    {last && <span className="text-xs text-gray-400">{formatTime(last.time)}</span>}
                    {unread > 0 && !isSelected && (
                      <span className="w-4 h-4 rounded-full bg-orange text-white text-[10px] font-bold flex items-center justify-center">
                        {unread > 9 ? '9+' : unread}
                      </span>
                    )}
                  </div>
                </div>
                {last && (
                  <p className={`text-xs truncate mt-0.5 ${unread > 0 && !isSelected ? 'font-semibold text-navy' : 'text-gray-500'}`}>
                    {last.deleted
                      ? <span className="italic text-gray-400">삭제된 메시지</span>
                      : last.file ? `📎 ${last.file.name}`
                      : <>{last.from === 'me' ? '나: ' : ''}{last.text}</>}
                  </p>
                )}
              </div>
            </button>
          )
        })}
      </div>
    </div>
  )

  // ── 채팅 패널 ─────────────────────────────────────────────
  const ChatPanel = selected ? (
    <div className="flex flex-col h-full">
      <div className="flex items-center gap-3 px-5 py-3.5 border-b border-offwhite-200 bg-white shrink-0">
        <button onClick={() => setMobileView('list')}
          className="lg:hidden w-8 h-8 flex items-center justify-center rounded-lg hover:bg-offwhite-100 text-gray-500 mr-1">
          <ChevronLeft size={18} />
        </button>
        <button
          onClick={() => navigate(`/individual/company/${encodeURIComponent(selected.companyName)}`)}
          className="flex items-center gap-3 flex-1 min-w-0 hover:opacity-80 transition-opacity text-left"
        >
          <Avatar name={selected.companyName} online={selected.online} size="lg" />
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-1">
              <p className="font-bold text-navy text-sm">{selected.companyName}</p>
              <ChevronRight size={13} className="text-gray-400 shrink-0" />
            </div>
            <p className="text-xs text-gray-400">
              {selected.online ? <span className="text-green-500 font-medium">온라인</span> : '기업'}
            </p>
          </div>
        </button>
        <div className="relative" ref={menuRef}>
          <button onClick={() => setMenuOpen(v => !v)}
            className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-offwhite-100 text-gray-400 hover:text-navy transition-colors">
            <MoreVertical size={16} />
          </button>
          {menuOpen && (
            <div className="absolute right-0 top-10 w-44 bg-white rounded-xl shadow-lg border border-offwhite-200 z-50 py-1">
              <button onClick={() => setMenuOpen(false)}
                className="w-full text-left px-4 py-2.5 text-sm text-gray-600 hover:bg-offwhite-100 transition-colors">
                신고하기
              </button>
              <button onClick={() => {
                setMenuOpen(false)
                setConversations(prev => prev.map(c => c.id === selectedId ? { ...c, blocked: true } : c))
                setSelectedId(visibleConvs.find(c => c.id !== selectedId)?.id ?? null)
              }} className="w-full text-left px-4 py-2.5 text-sm text-gray-600 hover:bg-offwhite-100 transition-colors">
                차단하기
              </button>
              <div className="border-t border-offwhite-200 my-1" />
              <button onClick={() => {
                setMenuOpen(false)
                setConversations(prev => prev.map(c => c.id === selectedId ? { ...c, left: true } : c))
                setSelectedId(visibleConvs.find(c => c.id !== selectedId)?.id ?? null)
              }} className="w-full text-left px-4 py-2.5 text-sm text-red-500 hover:bg-red-50 transition-colors">
                대화방 나가기
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="flex-1 overflow-y-auto px-5 py-4 space-y-2">
        {selected.messages.map((msg, i) => {
          const isMe = msg.from === 'me'
          const prevSame = i > 0 && selected.messages[i - 1].from === msg.from
          const isEditing = editingId === msg.id

          if (msg.deleted) return (
            <div key={msg.id} className={`flex ${isMe ? 'justify-end' : 'justify-start'} ${prevSame ? 'mt-0.5' : 'mt-3'}`}>
              {!isMe && <div className="w-9 shrink-0 mr-2" />}
              <p className="text-xs italic text-gray-400 px-4 py-2 bg-gray-50 rounded-2xl border border-gray-100">삭제된 메시지입니다</p>
            </div>
          )

          return (
            <div key={msg.id}
              className={`flex items-end gap-2 group ${isMe ? 'flex-row-reverse' : 'flex-row'} ${prevSame ? 'mt-0.5' : 'mt-3'}`}
            >
              {!isMe && !prevSame && <Avatar name={selected.companyName} online={false} />}
              {!isMe && prevSame && <div className="w-9 shrink-0" />}

              <div className={`max-w-[65%] flex flex-col gap-0.5 ${isMe ? 'items-end' : 'items-start'}`}>
                {isEditing ? (
                  <div className="flex items-center gap-1.5">
                    <input autoFocus value={editText} onChange={e => setEditText(e.target.value)}
                      onKeyDown={e => { if (e.key === 'Enter') confirmEdit(); if (e.key === 'Escape') cancelEdit() }}
                      className="px-3 py-2 text-sm border border-orange rounded-xl focus:outline-none focus:ring-1 focus:ring-orange/30"
                    />
                    <button onClick={confirmEdit} className="w-7 h-7 rounded-lg bg-green-50 text-green-600 hover:bg-green-100 flex items-center justify-center"><Check size={13} /></button>
                    <button onClick={cancelEdit} className="w-7 h-7 rounded-lg bg-gray-50 text-gray-500 hover:bg-gray-100 flex items-center justify-center"><X size={13} /></button>
                  </div>
                ) : (
                  <div className="relative flex items-center gap-1.5">
                    {isMe && !msg.file && (
                      <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity order-first">
                        <button onClick={() => startEdit(msg)} className="w-6 h-6 rounded-md bg-gray-100 hover:bg-gray-200 text-gray-500 flex items-center justify-center"><Pencil size={11} /></button>
                        <button onClick={() => deleteMsg(selectedId, msg.id)} className="w-6 h-6 rounded-md bg-gray-100 hover:bg-red-100 text-gray-500 hover:text-red-500 flex items-center justify-center"><Trash2 size={11} /></button>
                      </div>
                    )}
                    {msg.file ? <FileBubble file={msg.file} isMe={isMe} /> : (
                      <div className={`px-4 py-2.5 rounded-2xl text-sm leading-relaxed
                        ${isMe ? 'bg-navy text-white rounded-br-sm' : 'bg-offwhite-100 text-navy rounded-bl-sm border border-offwhite-200'}`}>
                        {msg.text}
                        {msg.edited && <span className="text-[10px] opacity-60 ml-1.5">(수정됨)</span>}
                      </div>
                    )}
                  </div>
                )}
                <span className="text-[11px] text-gray-400 px-1">{formatFull(msg.time)}</span>
              </div>
            </div>
          )
        })}
        {typing && <TypingIndicator name={selected.companyName} />}
        <div ref={bottomRef} />
      </div>

      <div className="px-5 py-4 border-t border-offwhite-200 bg-white shrink-0">
        <input ref={fileRef} type="file" accept="image/*,.pdf,.doc,.docx,.xls,.xlsx" className="hidden" onChange={handleFileChange} />
        <div className="flex items-end gap-2">
          <button onClick={() => fileRef.current?.click()}
            className="w-10 h-10 rounded-xl border border-offwhite-200 hover:bg-offwhite-100 text-gray-400 hover:text-navy flex items-center justify-center shrink-0 transition-colors">
            <Paperclip size={16} />
          </button>
          <textarea rows={1} value={input} onChange={e => setInput(e.target.value)} onKeyDown={handleKeyDown}
            placeholder="메시지를 입력하세요…"
            className="flex-1 resize-none px-4 py-2.5 text-sm border border-offwhite-200 rounded-xl focus:outline-none focus:border-orange focus:ring-1 focus:ring-orange/30 bg-offwhite-100 leading-relaxed max-h-32 overflow-y-auto"
            style={{ height: '42px' }}
            onInput={e => { e.target.style.height = '42px'; e.target.style.height = Math.min(e.target.scrollHeight, 128) + 'px' }}
          />
          <button onClick={handleSend} disabled={!input.trim()}
            className="w-10 h-10 rounded-xl bg-orange hover:bg-orange-600 disabled:bg-offwhite-200 disabled:text-gray-400 text-white flex items-center justify-center transition-colors shrink-0">
            <Send size={16} />
          </button>
        </div>
      </div>
    </div>
  ) : (
    <div className="flex-1 flex items-center justify-center">
      <p className="text-sm text-gray-400">대화를 선택하세요</p>
    </div>
  )

  return (
    <div className="h-[calc(100vh-4rem)] -m-6 flex overflow-hidden">
      <div className={`w-80 shrink-0 border-r border-offwhite-200 bg-white ${mobileView === 'chat' ? 'hidden lg:flex' : 'flex'} flex-col`}>
        {ConvList}
      </div>
      <div className={`flex-1 min-w-0 bg-white ${mobileView === 'list' ? 'hidden lg:flex' : 'flex'} flex-col`}>
        {ChatPanel}
      </div>
    </div>
  )
}
