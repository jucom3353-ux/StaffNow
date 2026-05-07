import { Link } from 'react-router-dom'
import { ArrowRight, Briefcase, Plus } from 'lucide-react'
import Card from '../ui/Card'
import StatusBadge from '../ui/StatusBadge'
import EmptyState from '../ui/EmptyState'
import { useAppData } from '../../context/AppDataContext'

export default function ActiveJobsSummary() {
  const { jobs } = useAppData()
  const active = jobs.filter(j => j.status === 'active').slice(0, 3)

  return (
    <Card
      padding={false}
      header={
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-sm font-bold text-navy">진행 중인 공고</span>
            <span className="bg-orange-50 text-orange text-xs font-bold px-1.5 py-0.5 rounded-md tabular-nums">
              {active.length}
            </span>
          </div>
          <Link
            to="/jobs"
            className="flex items-center gap-0.5 text-xs font-medium text-gray-400 hover:text-orange transition-colors"
          >
            전체 보기
            <ArrowRight size={11} strokeWidth={2.5} />
          </Link>
        </div>
      }
    >
      {active.length === 0 ? (
        <EmptyState
          icon={Briefcase}
          title="진행 중인 공고가 없습니다"
          description="첫 공고를 생성해 스태프를 모집하세요"
          action={{ label: '+ 공고 생성', to: '/jobs/create' }}
        />
      ) : (
        <div>
          {active.map((job, i) => (
            <Link
              key={job.id}
              to={`/jobs/${job.id}`}
              className={`flex items-center gap-4 px-5 py-3.5 hover:bg-offwhite-100 transition-colors group ${i < active.length - 1 ? 'border-b border-offwhite-100' : ''}`}
            >
              <div className="w-8 h-8 rounded-lg bg-navy-50 flex items-center justify-center shrink-0">
                <Briefcase size={14} className="text-navy" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-navy truncate group-hover:text-orange transition-colors">
                  {job.title}
                </p>
                <p className="text-xs text-gray-400 mt-0.5">{job.location}</p>
              </div>
              <div className="flex items-center gap-2 shrink-0">
                <div className="text-right">
                  <p className="text-xs font-semibold text-navy tabular-nums">{job.filledCount}<span className="text-gray-400 font-normal">/{job.headcount}명</span></p>
                </div>
                <StatusBadge status={job.status} size="sm" />
              </div>
            </Link>
          ))}
          {jobs.filter(j => j.status === 'active').length > 3 && (
            <Link to="/jobs" className="flex items-center justify-center py-2.5 text-xs text-gray-400 hover:text-orange transition-colors border-t border-offwhite-100">
              +{jobs.filter(j => j.status === 'active').length - 3}건 더 보기
            </Link>
          )}
        </div>
      )}
    </Card>
  )
}
