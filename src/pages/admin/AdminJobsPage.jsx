import { useState, useMemo } from 'react'
import { Briefcase, Search, Eye, Trash2, X, AlertTriangle } from 'lucide-react'
import { useAppData } from '../../context/AppDataContext'

const STATUS = {
  active:   { label: '진행 중',  color: 'bg-green-100 text-green-700' },
  reported: { label: '신고됨',   color: 'bg-red-100 text-red-700' },
  closed:   { label: '마감',     color: 'bg-gray-100 text-gray-600' },
  paused:   { label: '일시정지', color: 'bg-yellow-100 text-yellow-700' },
}

export default function AdminJobsPage() {
  const { jobs, deleteJob } = useAppData()
  const [search, setSearch] = useState('')
  const [viewJob, setViewJob] = useState(null)
  const [deleteConfirm, setDeleteConfirm] = useState(null)

  const filtered = useMemo(() =>
    jobs.filter(j =>
      j.title.toLowerCase().includes(search.toLowerCase()) ||
      (j.createdBy ?? '').toLowerCase().includes(search.toLowerCase())
    )
  , [jobs, search])

  function confirmDelete() {
    if (!deleteConfirm) return
    deleteJob(deleteConfirm.id)
    setDeleteConfirm(null)
  }

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">공고 관리</h1>
        <p className="text-sm text-gray-500 mt-1">플랫폼 전체 공고 현황 · {jobs.length}건</p>
      </div>

      <div className="flex items-center gap-2 bg-white border border-offwhite-200 rounded-xl px-4 py-2.5 max-w-sm">
        <Search size={15} className="text-gray-400" />
        <input
          type="text"
          placeholder="공고명 / 회사 검색..."
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="bg-transparent text-sm outline-none w-full placeholder-gray-400 text-navy"
        />
      </div>

      <div className="bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
        {filtered.length === 0 ? (
          <div className="py-16 text-center text-sm text-gray-400">
            <Briefcase size={32} className="mx-auto mb-3 text-gray-300" />
            검색 결과가 없습니다
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-offwhite-200 bg-offwhite">
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">공고명</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 hidden md:table-cell">회사</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 hidden lg:table-cell">채용 인원</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500 hidden md:table-cell">게시일</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">상태</th>
                <th className="px-5 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-offwhite-200">
              {filtered.map(job => {
                const s = STATUS[job.status] ?? STATUS.active
                return (
                  <tr key={job.id} className="hover:bg-offwhite-100 transition-colors">
                    <td className="px-5 py-3.5 font-semibold text-navy">{job.title}</td>
                    <td className="px-5 py-3.5 text-gray-500 hidden md:table-cell">{job.createdBy ?? '—'}</td>
                    <td className="px-5 py-3.5 text-gray-500 hidden lg:table-cell">{job.headcount}명</td>
                    <td className="px-5 py-3.5 text-gray-500 hidden md:table-cell">{job.createdAt}</td>
                    <td className="px-5 py-3.5">
                      <span className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${s.color}`}>{s.label}</span>
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="flex gap-1">
                        <button
                          onClick={() => setViewJob(job)}
                          className="p-1.5 rounded hover:bg-offwhite text-gray-400 hover:text-navy transition-colors"
                          title="상세 보기"
                        >
                          <Eye size={14} />
                        </button>
                        <button
                          onClick={() => setDeleteConfirm(job)}
                          className="p-1.5 rounded hover:bg-red-50 text-gray-400 hover:text-red-500 transition-colors"
                          title="삭제"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* 상세 보기 모달 */}
      {viewJob && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setViewJob(null)}
        >
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-md mx-4 overflow-hidden">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
              <h3 className="font-bold text-navy">공고 상세</h3>
              <button onClick={() => setViewJob(null)} className="text-gray-400 hover:text-navy transition-colors">
                <X size={18} />
              </button>
            </div>
            <div className="p-5 space-y-3">
              {[
                { label: '공고명',      value: viewJob.title },
                { label: '작성자 / 회사', value: viewJob.createdBy ?? '—' },
                { label: '근무 위치',   value: viewJob.location ?? '—' },
                { label: '채용 인원',   value: `${viewJob.headcount}명` },
                { label: '모집 완료',   value: `${viewJob.filledCount ?? 0}명` },
                { label: '게시일',      value: viewJob.createdAt },
                { label: '상태',        value: STATUS[viewJob.status]?.label ?? viewJob.status },
                ...(viewJob.wage ? [{ label: '급여', value: `${viewJob.wage} (${viewJob.wageType ?? ''})` }] : []),
                ...(viewJob.description ? [{ label: '상세 내용', value: viewJob.description }] : []),
              ].map(({ label, value }) => (
                <div key={label} className="flex gap-3">
                  <span className="text-xs font-semibold text-gray-400 w-28 shrink-0 mt-0.5">{label}</span>
                  <span className="text-sm text-navy flex-1 break-words">{value}</span>
                </div>
              ))}
            </div>
            <div className="px-5 pb-5">
              <button
                onClick={() => setViewJob(null)}
                className="w-full py-2.5 text-sm font-semibold text-gray-500 border border-offwhite-200 rounded-xl hover:bg-offwhite-100 transition-colors"
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 삭제 확인 모달 */}
      {deleteConfirm && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          onClick={e => e.target === e.currentTarget && setDeleteConfirm(null)}
        >
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm mx-4 p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 rounded-full bg-red-50 flex items-center justify-center shrink-0">
                <AlertTriangle size={18} className="text-red-500" />
              </div>
              <div>
                <p className="font-bold text-navy">공고 삭제</p>
                <p className="text-xs text-gray-400 mt-0.5">이 작업은 되돌릴 수 없습니다</p>
              </div>
            </div>
            <p className="text-sm text-gray-600 mb-5">
              <strong className="text-navy">"{deleteConfirm.title}"</strong> 공고를 삭제하시겠습니까?
            </p>
            <div className="flex gap-2">
              <button
                onClick={() => setDeleteConfirm(null)}
                className="flex-1 py-2.5 text-sm font-semibold text-gray-500 border border-offwhite-200 rounded-xl hover:bg-offwhite-100 transition-colors"
              >
                취소
              </button>
              <button
                onClick={confirmDelete}
                className="flex-1 py-2.5 text-sm font-semibold text-white bg-red-500 hover:bg-red-600 rounded-xl transition-colors"
              >
                삭제
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
