import { useState, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Plus, Search, ChevronRight, Trash2, ChevronUp, ChevronDown, ChevronsUpDown, Pencil } from 'lucide-react'
import Card from '../../components/ui/Card'
import StatusSelector from '../../components/ui/StatusSelector'
import Button from '../../components/ui/Button'
import EmptyState from '../../components/ui/EmptyState'
import ConfirmModal from '../../components/ui/ConfirmModal'
import { Briefcase } from 'lucide-react'
import { useAppData } from '../../context/AppDataContext'
import { useAuth } from '../../context/AuthContext'

const JOB_STATUS_OPTIONS = ['active', 'draft', 'closed', 'completed', 'cancelled']

const TABS = [
  { key: 'all',       label: '전체' },
  { key: 'active',    label: '모집 중' },
  { key: 'draft',     label: '초안' },
  { key: 'completed', label: '완료' },
]

const COLUMNS = [
  { key: 'title',      label: '공고명' },
  { key: 'status',     label: '상태' },
  { key: 'location',   label: '지역' },
  { key: 'headcount',  label: '인원' },
  { key: 'createdAt',  label: '생성일' },
]

const STATUS_ORDER = { active: 0, draft: 1, completed: 2 }

function sortJobs(jobs, key, dir) {
  if (!key) return jobs
  return [...jobs].sort((a, b) => {
    let av = a[key], bv = b[key]
    if (key === 'status') { av = STATUS_ORDER[av] ?? 9; bv = STATUS_ORDER[bv] ?? 9 }
    if (key === 'headcount') { av = Number(av); bv = Number(bv) }
    if (av < bv) return dir === 'asc' ? -1 : 1
    if (av > bv) return dir === 'asc' ? 1 : -1
    return 0
  })
}

function SortIcon({ colKey, sortKey, sortDir }) {
  if (sortKey !== colKey) return <ChevronsUpDown size={12} className="text-gray-300 ml-1 inline-block" />
  return sortDir === 'asc'
    ? <ChevronUp size={12} className="text-navy ml-1 inline-block" />
    : <ChevronDown size={12} className="text-navy ml-1 inline-block" />
}

export default function JobListPage() {
  const { jobs, shifts, deleteJob, updateJobStatus } = useAppData()
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'
  const myJobs = isAdmin ? jobs : jobs.filter(j => j.createdBy === user?.name)
  const [searchParams] = useSearchParams()
  const [tab, setTab] = useState('all')
  const [search, setSearch] = useState(searchParams.get('q') || '')

  useEffect(() => {
    const q = searchParams.get('q')
    if (q) setSearch(q)
  }, [searchParams])
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [sortKey, setSortKey] = useState('createdAt')
  const [sortDir, setSortDir] = useState('desc')

  function handleSort(key) {
    if (sortKey === key) {
      setSortDir(d => d === 'asc' ? 'desc' : 'asc')
    } else {
      setSortKey(key)
      setSortDir('asc')
    }
  }

  const filtered = sortJobs(
    myJobs
      .filter(j => tab === 'all' || j.status === tab)
      .filter(j => !search || j.title.includes(search) || j.location.includes(search)),
    sortKey,
    sortDir
  )

  return (
    <div className="space-y-5">
      {/* 페이지 헤더 */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-navy">공고 관리</h1>
          <p className="text-sm text-gray-500 mt-0.5">총 {myJobs.length}건의 공고</p>
        </div>
        <Button icon={Plus} as={Link} to="/jobs/create">공고 생성</Button>
      </div>

      {/* 탭 + 검색 */}
      <div className="flex items-center justify-between gap-4">
        <div className="flex gap-1">
          {TABS.map(t => {
            const count = t.key === 'all' ? myJobs.length : myJobs.filter(j => j.status === t.key).length
            return (
              <button
                key={t.key}
                onClick={() => setTab(t.key)}
                className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-all flex items-center gap-1.5
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
        <div className="flex items-center gap-2 bg-white border border-offwhite-200 rounded-lg px-3 py-1.5 w-52">
          <Search size={14} className="text-gray-400 shrink-0" />
          <input
            type="text"
            placeholder="공고명, 지역 검색"
            className="bg-transparent text-sm text-navy placeholder-gray-400 outline-none w-full"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
      </div>

      {/* 테이블 */}
      {filtered.length === 0 ? (
        <Card>
          <EmptyState
            icon={Briefcase}
            title={search ? `"${search}" 검색 결과가 없습니다` : '공고가 없습니다'}
            description={!search ? '첫 공고를 생성해 스태프를 모집하세요' : undefined}
            action={!search ? { label: '+ 공고 생성', to: '/jobs/create' } : undefined}
          />
        </Card>
      ) : (
        <Card padding={false}>
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-offwhite-200 bg-offwhite-100">
                {COLUMNS.map(col => (
                  <th
                    key={col.key}
                    onClick={() => handleSort(col.key)}
                    className="text-left px-5 py-3 text-xs font-semibold text-gray-500 uppercase tracking-wide cursor-pointer select-none hover:text-navy transition-colors whitespace-nowrap"
                  >
                    {col.label}
                    <SortIcon colKey={col.key} sortKey={sortKey} sortDir={sortDir} />
                  </th>
                ))}
                <th className="w-8" />
              </tr>
            </thead>
            <tbody>
              {filtered.map(job => (
                <tr key={job.id} className="border-b border-offwhite-100 last:border-0 hover:bg-offwhite-100 transition-colors group">
                  <td className="px-5 py-3.5">
                    <Link to={`/jobs/${job.id}`} className="font-semibold text-navy group-hover:text-orange transition-colors">
                      {job.title}
                    </Link>
                  </td>
                  <td className="px-5 py-3.5">
                    <StatusSelector
                      status={job.status}
                      options={JOB_STATUS_OPTIONS}
                      onChange={val => updateJobStatus(job.id, val)}
                    />
                  </td>
                  <td className="px-5 py-3.5 text-gray-600">{job.location}</td>
                  <td className="px-5 py-3.5">
                    <span className="font-semibold text-navy tabular-nums">{shifts.filter(s => s.jobId === job.id).reduce((sum, s) => sum + (s.confirmedStaff || 0), 0)}</span>
                    <span className="text-gray-400">/{job.headcount}명</span>
                  </td>
                  <td className="px-5 py-3.5 text-gray-400 text-xs">{job.createdAt}</td>
                  <td className="px-3 py-3.5">
                    <div className="flex items-center gap-1">
                      <Link
                        to={`/jobs/${job.id}/edit`}
                        onClick={e => e.stopPropagation()}
                        className="p-1 rounded text-gray-300 hover:text-orange hover:bg-orange/10 transition-colors opacity-0 group-hover:opacity-100"
                        title="공고 수정"
                      >
                        <Pencil size={14} />
                      </Link>
                      <button
                        onClick={e => { e.preventDefault(); e.stopPropagation(); setDeleteTarget(job) }}
                        className="p-1 rounded text-gray-300 hover:text-red-500 hover:bg-red-50 transition-colors opacity-0 group-hover:opacity-100"
                        title="공고 삭제"
                      >
                        <Trash2 size={14} />
                      </button>
                      <Link to={`/jobs/${job.id}`} className="p-1 text-gray-300 group-hover:text-navy transition-colors">
                        <ChevronRight size={16} />
                      </Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}

      <ConfirmModal
        open={!!deleteTarget}
        title="공고를 삭제하시겠습니까?"
        description={`"${deleteTarget?.title}" 공고와 연결된 모든 데이터가 삭제됩니다. 이 작업은 되돌릴 수 없습니다.`}
        onConfirm={() => { deleteJob(deleteTarget.id); setDeleteTarget(null) }}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}
