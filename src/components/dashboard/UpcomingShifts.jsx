import { Link } from 'react-router-dom'
import { ArrowRight, Calendar } from 'lucide-react'
import Card from '../ui/Card'
import StatusBadge from '../ui/StatusBadge'
import EmptyState from '../ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'

export default function UpcomingShifts() {
  const { shifts } = useAppData()
  const upcoming = shifts.filter(s => s.status === 'scheduled').slice(0, 3)

  return (
    <Card
      padding={false}
      header={
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-sm font-bold text-navy">예정 Shift</span>
            <span className="bg-navy-50 text-navy text-xs font-bold px-1.5 py-0.5 rounded-md tabular-nums">
              {upcoming.length}
            </span>
          </div>
          <Link
            to="/shifts"
            className="flex items-center gap-0.5 text-xs font-medium text-gray-400 hover:text-orange transition-colors"
          >
            전체 보기
            <ArrowRight size={11} strokeWidth={2.5} />
          </Link>
        </div>
      }
    >
      {upcoming.length === 0 ? (
        <EmptyState
          icon={Calendar}
          title="예정된 Shift가 없습니다"
          description="공고에 Shift를 추가해 일정을 관리하세요"
          action={{ label: '+ Shift 생성', to: '/shifts/create' }}
        />
      ) : (
        <div>
          {upcoming.map((shift, i) => {
            const d = new Date(shift.date + 'T00:00:00')
            return (
              <Link
                key={shift.id}
                to={`/shifts/${shift.id}`}
                className={`flex items-center gap-4 px-5 py-3.5 hover:bg-offwhite-100 transition-colors group ${i < upcoming.length - 1 ? 'border-b border-offwhite-100' : ''}`}
              >
                <div className="w-9 h-9 rounded-lg bg-navy-50 flex flex-col items-center justify-center shrink-0 border border-navy-100">
                  <span className="text-xs font-extrabold text-navy leading-none tabular-nums">
                    {d.getDate()}
                  </span>
                  <span className="text-[10px] text-navy-200 leading-none mt-0.5 font-medium">
                    {d.toLocaleString('ko', { month: 'short' })}
                  </span>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-navy truncate group-hover:text-orange transition-colors">
                    {shift.jobTitle}
                  </p>
                  <p className="text-xs text-gray-400 mt-0.5">
                    {shift.startTime}–{shift.endTime}
                    <span className="mx-1.5 text-gray-300">·</span>
                    <span className="tabular-nums">{shift.confirmedStaff}/{shift.requiredStaff}명 확정</span>
                  </p>
                </div>
                <StatusBadge status={shift.status} size="sm" />
              </Link>
            )
          })}
        </div>
      )}
    </Card>
  )
}
