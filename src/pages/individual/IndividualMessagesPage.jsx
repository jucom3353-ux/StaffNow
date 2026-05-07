import { MessageSquare } from 'lucide-react'

const MESSAGES = [
  { id: 1, from: '(주)스태프나우', preview: '지원해 주셔서 감사합니다. 검토 후 연락드리겠습니다.', time: '10:32', unread: 1 },
  { id: 2, from: '킨텍스',        preview: '합격을 축하합니다! 근무 일정을 확인해 주세요.',       time: '어제',  unread: 0 },
  { id: 3, from: '브랜드X',       preview: '이번에는 함께하지 못하게 되어 아쉽습니다.',           time: '5/1',   unread: 0 },
]

export default function IndividualMessagesPage() {
  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">메시지</h1>
        <p className="text-sm text-gray-500 mt-1">기업과의 대화</p>
      </div>

      <div className="bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
        {MESSAGES.map((msg, i) => (
          <div
            key={msg.id}
            className={`flex items-center gap-4 px-5 py-4 cursor-pointer hover:bg-offwhite-100 transition-colors ${i < MESSAGES.length - 1 ? 'border-b border-offwhite-200' : ''}`}
          >
            <div className="w-10 h-10 rounded-full bg-navy flex items-center justify-center shrink-0">
              <span className="text-white text-sm font-bold">{msg.from[0]}</span>
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between mb-0.5">
                <p className="font-semibold text-navy text-sm">{msg.from}</p>
                <span className="text-xs text-gray-400">{msg.time}</span>
              </div>
              <p className="text-xs text-gray-500 truncate">{msg.preview}</p>
            </div>
            {msg.unread > 0 && (
              <span className="w-5 h-5 rounded-full bg-orange text-white text-[10px] font-bold flex items-center justify-center shrink-0">
                {msg.unread}
              </span>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
