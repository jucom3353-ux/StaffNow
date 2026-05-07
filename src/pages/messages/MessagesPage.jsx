import { useState, useRef, useEffect, useMemo } from 'react'
import { Search, Send, MoreVertical, ChevronLeft } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

// ── 목업 대화 데이터 ──────────────────────────────────────
const INITIAL_CONVERSATIONS = [
  {
    id: 'conv-01',
    staffId: 'ap-01',
    staffName: '김서연',
    role: '부스 운영 보조',
    avatar: '김',
    online: true,
    messages: [
      { id: 'm1', from: 'staff', text: '안녕하세요, 6월 1일 박람회 Shift 관련해서 문의드립니다.', time: '2025-06-01T09:10:00' },
      { id: 'm2', from: 'biz',   text: '네, 안녕하세요! 어떤 부분이 궁금하신가요?', time: '2025-06-01T09:12:00' },
      { id: 'm3', from: 'staff', text: '현장 도착 시간이 10시인데, 준비가 있어서 9시 30분에 와도 될까요?', time: '2025-06-01T09:13:00' },
      { id: 'm4', from: 'biz',   text: '물론이죠! 일찍 오시면 더 좋습니다. 입장 시 1층 안내데스크에서 스태프 배지 수령해 주세요.', time: '2025-06-01T09:15:00' },
      { id: 'm5', from: 'staff', text: '감사합니다. 당일 잘 부탁드립니다!', time: '2025-06-01T09:16:00' },
    ],
  },
  {
    id: 'conv-02',
    staffId: 'ap-03',
    staffName: '이지은',
    role: '행사 안내 스태프',
    avatar: '이',
    online: true,
    messages: [
      { id: 'm1', from: 'biz',   text: '이지은님, 이번 Shift 확정되셨습니다. 수고해 주세요!', time: '2025-05-28T14:00:00' },
      { id: 'm2', from: 'staff', text: '감사합니다! 열심히 하겠습니다 😊', time: '2025-05-28T14:05:00' },
      { id: 'm3', from: 'staff', text: '혹시 유니폼이 따로 있나요?', time: '2025-05-28T14:06:00' },
      { id: 'm4', from: 'biz',   text: '흰 셔츠 + 검정 바지로 통일해 주시면 됩니다.', time: '2025-05-28T14:10:00' },
    ],
  },
  {
    id: 'conv-03',
    staffId: 'ap-05',
    staffName: '정다영',
    role: '고객 응대',
    avatar: '정',
    online: false,
    messages: [
      { id: 'm1', from: 'biz',   text: '정다영님, 초대 수락해 주셔서 감사합니다!', time: '2025-05-25T10:00:00' },
      { id: 'm2', from: 'staff', text: '네! 잘 부탁드립니다. 근무지 위치가 어디인가요?', time: '2025-05-25T10:30:00' },
      { id: 'm3', from: 'biz',   text: '서울 강남구 코엑스 B홀입니다. 지하철 2호선 삼성역 5번 출구 도보 3분이에요.', time: '2025-05-25T10:35:00' },
    ],
  },
  {
    id: 'conv-04',
    staffId: 'ap-06',
    staffName: '황민석',
    role: '안내 데스크',
    avatar: '황',
    online: false,
    messages: [
      { id: 'm1', from: 'staff', text: '안녕하세요, 내일 Shift 교통편 때문에 30분 늦을 것 같습니다. 괜찮을까요?', time: '2025-05-31T20:00:00' },
      { id: 'm2', from: 'biz',   text: '확인했습니다. 알려주셔서 감사해요. 최대한 서둘러 주시면 됩니다.', time: '2025-05-31T20:15:00' },
    ],
  },
  {
    id: 'conv-05',
    staffId: 'ap-09',
    staffName: '임수진',
    role: '행사 안내 스태프',
    avatar: '임',
    online: false,
    messages: [
      { id: 'm1', from: 'biz',   text: '임수진님, 이번 6월 박람회 Shift에 관심 있으신가요?', time: '2025-05-20T11:00:00' },
      { id: 'm2', from: 'staff', text: '네, 관심 있습니다! 자세한 내용 알 수 있을까요?', time: '2025-05-20T11:30:00' },
      { id: 'm3', from: 'biz',   text: '6월 1일 오전 10시 ~ 오후 7시, 코엑스 행사입니다. 시급 13,000원이에요.', time: '2025-05-20T11:32:00' },
      { id: 'm4', from: 'staff', text: '좋네요! 참여하겠습니다.', time: '2025-05-20T11:45:00' },
    ],
  },
  {
    id: 'conv-06',
    staffId: 'ap-10',
    staffName: '손태민',
    role: '부스 운영 보조',
    avatar: '손',
    online: true,
    messages: [
      { id: 'm1', from: 'biz',   text: '손태민님, 초대 발송했습니다. 확인 부탁드립니다!', time: '2025-05-22T09:00:00' },
    ],
  },
]

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

function Avatar({ name, online, size = 'md' }) {
  const sz = size === 'lg' ? 'w-11 h-11 text-base' : 'w-9 h-9 text-sm'
  return (
    <div className="relative shrink-0">
      <div className={`${sz} rounded-full bg-navy/10 text-navy font-bold flex items-center justify-center`}>
        {name[0]}
      </div>
      {online && (
        <span className="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-green-400 border-2 border-white" />
      )}
    </div>
  )
}

export default function MessagesPage() {
  const { user } = useAuth()
  const [conversations, setConversations] = useState(INITIAL_CONVERSATIONS)
  const [selectedId, setSelectedId] = useState(INITIAL_CONVERSATIONS[0].id)
  const [input, setInput] = useState('')
  const [search, setSearch] = useState('')
  const [mobileView, setMobileView] = useState('list') // 'list' | 'chat'
  const bottomRef = useRef(null)

  const selected = conversations.find(c => c.id === selectedId)

  const filteredConvs = useMemo(() =>
    conversations.filter(c =>
      c.staffName.includes(search) || c.role.includes(search)
    )
  , [conversations, search])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [selectedId, selected?.messages.length])

  function sendMessage() {
    const text = input.trim()
    if (!text) return
    const newMsg = {
      id: `m-${Date.now()}`,
      from: 'biz',
      text,
      time: new Date().toISOString(),
    }
    setConversations(prev =>
      prev.map(c => c.id === selectedId ? { ...c, messages: [...c.messages, newMsg] } : c)
    )
    setInput('')
  }

  function handleKeyDown(e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  function selectConv(id) {
    setSelectedId(id)
    setMobileView('chat')
  }

  // ── 대화 목록 패널 ──────────────────────────────────────
  const ConvList = (
    <div className="flex flex-col h-full">
      {/* 헤더 */}
      <div className="px-4 pt-4 pb-3 border-b border-offwhite-200">
        <h2 className="text-base font-bold text-navy mb-3">메시지</h2>
        <div className="relative">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            type="text"
            placeholder="스태프 검색..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-8 pr-3 py-2 text-sm bg-offwhite-100 rounded-lg border border-offwhite-200 focus:outline-none focus:border-orange focus:ring-1 focus:ring-orange/30"
          />
        </div>
      </div>

      {/* 목록 */}
      <div className="flex-1 overflow-y-auto">
        {filteredConvs.length === 0 ? (
          <p className="text-center text-sm text-gray-400 mt-8">검색 결과가 없습니다</p>
        ) : filteredConvs.map(c => {
          const last = c.messages.at(-1)
          const isSelected = c.id === selectedId
          return (
            <button
              key={c.id}
              onClick={() => selectConv(c.id)}
              className={`w-full flex items-center gap-3 px-4 py-3.5 text-left transition-colors border-b border-offwhite-100 last:border-0
                ${isSelected ? 'bg-orange/5 border-l-2 border-l-orange' : 'hover:bg-offwhite-100 border-l-2 border-l-transparent'}`}
            >
              <Avatar name={c.staffName} online={c.online} />
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between">
                  <span className={`text-sm font-semibold ${isSelected ? 'text-orange' : 'text-navy'}`}>
                    {c.staffName}
                  </span>
                  {last && <span className="text-xs text-gray-400 shrink-0 ml-1">{formatTime(last.time)}</span>}
                </div>
                <p className="text-xs text-gray-400 truncate mt-0.5">{c.role}</p>
                {last && (
                  <p className="text-xs text-gray-500 truncate mt-0.5">
                    {last.from === 'biz' ? '나: ' : ''}{last.text}
                  </p>
                )}
              </div>
            </button>
          )
        })}
      </div>
    </div>
  )

  // ── 채팅 패널 ───────────────────────────────────────────
  const ChatPanel = selected ? (
    <div className="flex flex-col h-full">
      {/* 채팅 헤더 */}
      <div className="flex items-center gap-3 px-5 py-3.5 border-b border-offwhite-200 bg-white shrink-0">
        <button
          onClick={() => setMobileView('list')}
          className="lg:hidden w-8 h-8 flex items-center justify-center rounded-lg hover:bg-offwhite-100 text-gray-500 mr-1"
        >
          <ChevronLeft size={18} />
        </button>
        <Avatar name={selected.staffName} online={selected.online} size="lg" />
        <div className="flex-1">
          <p className="font-bold text-navy text-sm">{selected.staffName}</p>
          <p className="text-xs text-gray-400">
            {selected.online
              ? <span className="text-green-500 font-medium">온라인</span>
              : selected.role}
          </p>
        </div>
        <button className="w-8 h-8 flex items-center justify-center rounded-lg hover:bg-offwhite-100 text-gray-400 hover:text-navy transition-colors">
          <MoreVertical size={16} />
        </button>
      </div>

      {/* 메시지 목록 */}
      <div className="flex-1 overflow-y-auto px-5 py-4 space-y-3">
        {selected.messages.map((msg, i) => {
          const isBiz = msg.from === 'biz'
          const prevSame = i > 0 && selected.messages[i - 1].from === msg.from
          return (
            <div key={msg.id} className={`flex items-end gap-2 ${isBiz ? 'flex-row-reverse' : 'flex-row'} ${prevSame ? 'mt-1' : 'mt-3'}`}>
              {!isBiz && !prevSame && <Avatar name={selected.staffName} online={false} />}
              {!isBiz && prevSame && <div className="w-9 shrink-0" />}
              <div className={`max-w-[65%] ${isBiz ? 'items-end' : 'items-start'} flex flex-col gap-1`}>
                <div className={`px-4 py-2.5 rounded-2xl text-sm leading-relaxed
                  ${isBiz
                    ? 'bg-navy text-white rounded-br-sm'
                    : 'bg-offwhite-100 text-navy rounded-bl-sm border border-offwhite-200'}`}
                >
                  {msg.text}
                </div>
                <span className="text-[11px] text-gray-400 px-1">{formatFull(msg.time)}</span>
              </div>
            </div>
          )
        })}
        <div ref={bottomRef} />
      </div>

      {/* 입력창 */}
      <div className="px-5 py-4 border-t border-offwhite-200 bg-white shrink-0">
        <div className="flex items-end gap-2">
          <textarea
            rows={1}
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="메시지를 입력하세요… (Enter로 전송)"
            className="flex-1 resize-none px-4 py-2.5 text-sm border border-offwhite-200 rounded-xl focus:outline-none focus:border-orange focus:ring-1 focus:ring-orange/30 bg-offwhite-100 leading-relaxed max-h-32 overflow-y-auto"
            style={{ height: '42px' }}
            onInput={e => {
              e.target.style.height = '42px'
              e.target.style.height = Math.min(e.target.scrollHeight, 128) + 'px'
            }}
          />
          <button
            onClick={sendMessage}
            disabled={!input.trim()}
            className="w-10 h-10 rounded-xl bg-orange hover:bg-orange-600 disabled:bg-offwhite-200 disabled:text-gray-400 text-white flex items-center justify-center transition-colors shrink-0"
          >
            <Send size={16} />
          </button>
        </div>
      </div>
    </div>
  ) : (
    <div className="flex-1 flex items-center justify-center text-gray-400 text-sm">
      대화를 선택하세요
    </div>
  )

  // ── 레이아웃 ─────────────────────────────────────────────
  return (
    <div className="h-[calc(100vh-4rem)] -m-6 flex overflow-hidden">
      {/* 좌측: 대화 목록 */}
      <div className={`w-80 shrink-0 border-r border-offwhite-200 bg-white
        ${mobileView === 'chat' ? 'hidden lg:flex' : 'flex'} flex-col`}>
        {ConvList}
      </div>

      {/* 우측: 채팅 */}
      <div className={`flex-1 min-w-0 bg-white
        ${mobileView === 'list' ? 'hidden lg:flex' : 'flex'} flex-col`}>
        {ChatPanel}
      </div>
    </div>
  )
}
