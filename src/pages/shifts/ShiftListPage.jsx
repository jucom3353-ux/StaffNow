import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Plus, Calendar, ChevronRight, ClipboardList, FileCheck } from 'lucide-react'
import Card from '../../components/ui/Card'
import StatusBadge from '../../components/ui/StatusBadge'
import Button from '../../components/ui/Button'
import EmptyState from '../../components/ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'

const TABS = [
  { key: 'all',        label: '전체' },
  { key: 'scheduled',  label: '예정' },
  { key: 'in_progress',label: '진행 중' },
  { key: 'completed',  label: '완료' },
]

export default function ShiftListPage() {
  const { shifts, jobs } = useAppData()
  const { user } = useAuth()
  const [searchParams] = useSearchParams()
  const [tab, setTab] = useState(searchParams.get('tab') || 'all')

  const isAdmin = user?.role === 'ADMIN'
  const myJobIds = isAdmin ? null : new Set(jobs.filter(j => j.createdBy === user?.name).map(j => j.id))
  const myShifts = isAdmin ? shifts : shifts.filter(s => myJobIds.has(s.jobId))

  const filtered = myShifts.filter(s => tab === 'all' || s.status === tab)

  return (
    <div className="space-y-5">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-navy">Shift 관리</h1>
          <p className="text-sm text-gray-500 mt-0.5">총 {myShifts.length}건의 Shift</p>
        </div>
        <Button icon={Plus} as={Link} to="/shifts/create">Shift 생성</Button>
      </div>

      {/* 탭 */}
      <div className="flex gap-1 overflow-x-auto scrollbar-hide">
        {TABS.map(t => {
          const count = t.key === 'all' ? myShifts.length : myShifts.filter(s => s.status === t.key).length
          return (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`shrink-0 px-3 py-1.5 rounded-lg text-sm font-medium transition-all flex items-center gap-1.5
                ${tab === t.key
                  ? 'bg-navy text-white'
                  : 'text-gray-500 hover:bg-offwhite-100 hover:text-navy'}`}
            >
              {t.label}
              {count > 0 && (
                <span className={`text-xs tabular-nums px-1.5 rounded-md
                  ${tab === t.key ? 'bg-white/20 text-white' : 'bg-offwhite-200 text-gray-500'}`}>
                  {count}
                </span>
              )}
            </button>
          )
        })}
      </div>

      {filtered.length === 0 ? (
        <Card>
          <EmptyState
            icon={Calendar}
            title="Shift가 없습니다"
            description="공고를 선택해 Shift를 생성하세요"
            action={{ label: '+ Shift 생성', to: '/shifts/create' }}
          />
        </Card>
      ) : (
        <div className="space-y-2">
          {filtered.map(shift => {
            const d = new Date(shift.date + 'T00:00:00')
            const displayCount = shift.applicantCount ?? shift.confirmedStaff
            const isOver = displayCount > shift.requiredStaff
            const isCompleted = shift.status === 'completed'
            const fillRatio = shift.requiredStaff > 0
              ? Math.min(shift.confirmedStaff / shift.requiredStaff, 1)
              : 0
            return (
              <div key={shift.id} className="space-y-0">
                <Link to={`/shifts/${shift.id}`}>
                <Card className="hover:border-navy-100 hover:shadow-[0_2px_8px_rgba(27,43,72,0.1)] transition-all cursor-pointer group">
                  <div className="flex items-center gap-4">
                    {/* 날짜 뱃지 */}
                    <div className="w-12 h-12 rounded-xl bg-navy-50 flex flex-col items-center justify-center shrink-0 border border-navy-100">
                      <span className="text-base font-extrabold text-navy leading-none tabular-nums">
                        {d.getDate()}
                      </span>
                      <span className="text-xs text-navy-200 leading-none mt-0.5 font-medium">
                        {d.toLocaleString('ko', { month: 'short' })}
                      </span>
                    </div>

                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 min-w-0">
                        <p className="font-semibold text-navy text-sm group-hover:text-orange transition-colors truncate min-w-0">
                          {shift.jobTitle}
                        </p>
                        <div className="flex items-center gap-1.5 shrink-0">
                          <StatusBadge status={shift.status} size="sm" />
                          {isOver && (
                            <span className="whitespace-nowrap text-xs font-bold text-orange bg-orange-50 border border-orange/20 px-1.5 py-0.5 rounded-full">
                              마감 초과
                            </span>
                          )}
                        </div>
                      </div>
                      <p className="text-xs text-gray-400 mt-2 truncate">
                        {shift.startTime}–{shift.endTime}
                        {shift.location && <><span className="mx-1.5">·</span>{shift.location}</>}
                      </p>
                      {/* 진행률 바 */}
                      <div className="flex items-center gap-2 mt-2">
                        <div className="flex-1 h-1.5 bg-offwhite-200 rounded-full overflow-hidden">
                          <div
                            className={`h-full rounded-full transition-all ${isOver ? 'bg-orange' : 'bg-navy'}`}
                            style={{ width: `${isOver ? 100 : fillRatio * 100}%` }}
                          />
                        </div>
                        <div className="flex items-center gap-1 shrink-0">
                          <span className={`text-xs font-bold px-1.5 py-0.5 rounded-md leading-none
                            ${isOver ? 'bg-orange text-white' : 'bg-navy/10 text-navy'}`}>
                            지원
                          </span>
                          <span className={`text-xs tabular-nums font-semibold ${isOver ? 'text-orange' : 'text-gray-500'}`}>
                            {displayCount} / {shift.requiredStaff}명
                          </span>
                        </div>
                      </div>
                    </div>

                    <ChevronRight size={16} className="text-gray-300 group-hover:text-navy transition-colors shrink-0" />
                  </div>
                </Card>
                </Link>
                {isCompleted && (
                  <div className="flex gap-2 px-1 pb-1 -mt-1">
                    <Link
                      to="/attendance"
                      onClick={e => e.stopPropagation()}
                      className="flex items-center gap-1.5 text-xs font-semibold text-blue-600 bg-blue-50 hover:bg-blue-100 border border-blue-200 px-3 py-1.5 rounded-b-lg transition-colors"
                    >
                      <ClipboardList size={11} />근태 관리
                    </Link>
                    <Link
                      to="/contracts"
                      onClick={e => e.stopPropagation()}
                      className="flex items-center gap-1.5 text-xs font-semibold text-green-600 bg-green-50 hover:bg-green-100 border border-green-200 px-3 py-1.5 rounded-b-lg transition-colors"
                    >
                      <FileCheck size={11} />계약 보기
                    </Link>
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
