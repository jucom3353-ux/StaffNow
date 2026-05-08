import { useParams, Link, useLocation, useNavigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import Card from '../../components/ui/Card'
import StatusBadge from '../../components/ui/StatusBadge'
import EmptyState from '../../components/ui/EmptyState'
import Button from '../../components/ui/Button'
import ConfirmModal from '../../components/ui/ConfirmModal'
import StatusSelector from '../../components/ui/StatusSelector'
import { Briefcase, CalendarPlus, Users, ChevronLeft, Trash2, Pencil, ShieldCheck } from 'lucide-react'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'

const JOB_STATUS_OPTIONS = ['active', 'draft', 'closed', 'completed', 'cancelled']

export default function JobDetailPage() {
  const { id } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const { user } = useAuth()
  const { jobs, shifts, addToast, deleteJob, updateJobStatus } = useAppData()
  const [showDeleteModal, setShowDeleteModal] = useState(false)

  const job = jobs.find(j => j.id === id)
  const jobShifts = shifts.filter(s => s.jobId === id)
  const isAuthor = job?.createdBy === user?.name
  const isAdmin = user?.role === 'ADMIN'
  const canManage = isAuthor || isAdmin

  useEffect(() => {
    if (location.state?.created) {
      addToast({ type: 'success', message: '공고가 성공적으로 등록되었습니다 🎉' })
    } else if (location.state?.updated) {
      addToast({ type: 'success', message: '공고가 수정되었습니다' })
    }
  }, [])

  if (!job) return (
    <EmptyState
      icon={Briefcase}
      title="공고를 찾을 수 없습니다"
      action={{ label: '목록으로', to: '/jobs' }}
    />
  )

  return (
    <div className="max-w-3xl space-y-5">
      {/* 헤더 */}
      <div className="flex items-start gap-3">
        <Link
          to="/jobs"
          className="w-8 h-8 rounded-lg border border-offwhite-200 flex items-center justify-center text-gray-500 hover:bg-offwhite-100 transition-colors shrink-0 mt-0.5"
        >
          <ChevronLeft size={18} />
        </Link>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <h1 className="text-xl font-bold text-navy">{job.title}</h1>
            <StatusSelector
                      status={job.status}
                      options={JOB_STATUS_OPTIONS}
                      onChange={val => updateJobStatus(id, val)}
                    />
          </div>
          <p className="text-sm text-gray-500 mt-0.5">{job.location} · {job.createdAt} 생성</p>
        </div>
        <div className="flex items-center gap-2">
          {isAdmin && !isAuthor && (
            <span className="flex items-center gap-1 text-xs font-bold text-purple-600 bg-purple-50 border border-purple-200 px-2 py-1 rounded-full">
              <ShieldCheck size={11} />관리자 권한
            </span>
          )}
          {canManage && (
            <Button icon={Pencil} size="sm" variant="secondary" as={Link} to={`/jobs/${id}/edit`}>
              수정
            </Button>
          )}
          {canManage && (
            <Button icon={Trash2} size="sm" variant="danger-ghost" onClick={() => setShowDeleteModal(true)}>
              삭제
            </Button>
          )}
          <Button icon={CalendarPlus} size="sm" as={Link} to={`/shifts/create?jobId=${id}`} variant="secondary">
            Shift 추가
          </Button>
        </div>
      </div>

      {/* 기본 정보 */}
      <Card header={<span className="text-sm font-bold text-navy">공고 정보</span>}>
        <dl className="grid grid-cols-2 gap-x-8 gap-y-3 text-sm">
          <div>
            <dt className="text-xs font-medium text-gray-400 uppercase tracking-wide mb-0.5">근무 지역</dt>
            <dd className="font-semibold text-navy">{job.location}</dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-gray-400 uppercase tracking-wide mb-0.5">모집 인원</dt>
            <dd className="font-semibold text-navy">
              <span className="text-orange tabular-nums">{jobShifts.reduce((sum, s) => sum + (s.confirmedStaff || 0), 0)}</span>
              <span className="text-gray-400 font-normal">/{job.headcount}명</span>
            </dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-gray-400 uppercase tracking-wide mb-0.5">Shift 수</dt>
            <dd className="font-semibold text-navy">{jobShifts.length}개</dd>
          </div>
          <div>
            <dt className="text-xs font-medium text-gray-400 uppercase tracking-wide mb-0.5">상태</dt>
            <dd>
              <StatusSelector
                status={job.status}
                options={JOB_STATUS_OPTIONS}
                onChange={val => updateJobStatus(id, val)}
              />
            </dd>
          </div>
          {job.createdBy && (
            <div>
              <dt className="text-xs font-medium text-gray-400 uppercase tracking-wide mb-0.5">작성자</dt>
              <dd className="font-semibold text-navy">{job.createdBy}</dd>
            </div>
          )}
        </dl>
      </Card>

      {/* 연결된 Shift */}
      <Card
        padding={false}
        header={
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-sm font-bold text-navy">연결된 Shift</span>
              <span className="bg-navy-50 text-navy text-xs font-bold px-1.5 py-0.5 rounded-md">{jobShifts.length}</span>
            </div>
            <Button icon={CalendarPlus} size="sm" variant="ghost" as={Link} to={`/shifts/create?jobId=${id}`}>
              Shift 추가
            </Button>
          </div>
        }
      >
        {jobShifts.length === 0 ? (
          <EmptyState
            icon={CalendarPlus}
            title="연결된 Shift가 없습니다"
            description="이 공고에 근무 일정을 추가하세요"
            action={{ label: '+ Shift 생성', to: '/shifts/create' }}
          />
        ) : (
          <div>
            {jobShifts.map((shift, i) => (
              <Link
                key={shift.id}
                to={`/shifts/${shift.id}`}
                className={`flex items-center gap-4 px-5 py-3 hover:bg-offwhite-100 transition-colors group ${i < jobShifts.length - 1 ? 'border-b border-offwhite-100' : ''}`}
              >
                <div className="w-9 h-9 rounded-lg bg-navy-50 flex flex-col items-center justify-center shrink-0 border border-navy-100">
                  <span className="text-xs font-extrabold text-navy leading-none tabular-nums">
                    {new Date(shift.date + 'T00:00:00').getDate()}
                  </span>
                  <span className="text-[10px] text-navy-200 font-medium">
                    {new Date(shift.date + 'T00:00:00').toLocaleString('ko', { month: 'short' })}
                  </span>
                </div>
                <div className="flex-1">
                  <p className="text-sm text-navy font-medium group-hover:text-orange transition-colors">
                    {shift.startTime}–{shift.endTime}
                  </p>
                  <p className="text-xs text-gray-400 mt-0.5 tabular-nums">
                    확정 {shift.confirmedStaff}/{shift.requiredStaff}명
                  </p>
                </div>
                <StatusBadge status={shift.status} size="sm" />
              </Link>
            ))}
          </div>
        )}
      </Card>

      <ConfirmModal
        open={showDeleteModal}
        title="공고를 삭제하시겠습니까?"
        description={`"${job.title}" 공고와 연결된 모든 데이터가 삭제됩니다. 이 작업은 되돌릴 수 없습니다.`}
        onConfirm={() => { deleteJob(id); navigate('/jobs') }}
        onCancel={() => setShowDeleteModal(false)}
      />

      {/* 추천 인력 바로가기 */}
      <Card className="border-orange-100 bg-orange-50">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-orange flex items-center justify-center shrink-0">
              <Users size={16} className="text-white" />
            </div>
            <div>
              <p className="text-sm font-semibold text-navy">추천 인력 확인</p>
              <p className="text-xs text-gray-500 mt-0.5">이 공고에 적합한 스태프를 확인하세요</p>
            </div>
          </div>
          <Button size="sm" as={Link} to="/staff">
            인력 확인
          </Button>
        </div>
      </Card>
    </div>
  )
}
