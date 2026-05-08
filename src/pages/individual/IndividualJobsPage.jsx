import { Search, MapPin, Clock, Bookmark, SlidersHorizontal, X } from 'lucide-react'
import { useState, useRef, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { RECOMMENDED_JOBS } from '../../data/mockIndividual'
import { useIndividualData } from '../../hooks/useIndividualData'

const TAGS = ['전체', '단기', '장기', '주말', '야간', '행사/이벤트', '카페', '편의점']

const SORT_OPTIONS = [
  { value: 'newest',   label: '최신순' },
  { value: 'wage',     label: '급여 높은순' },
  { value: 'deadline', label: '마감 임박순' },
]

function parseWage(wage) {
  const m = wage?.match(/[\d,]+/)
  return m ? parseInt(m[0].replace(/,/g, ''), 10) : 0
}

export default function IndividualJobsPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { isSaved, toggleSave } = useIndividualData()
  const [query,     setQuery]     = useState(() => searchParams.get('q') ?? '')
  const [activeTag, setActiveTag] = useState('전체')
  const [sortBy,    setSortBy]    = useState('newest')
  const [filterOpen, setFilterOpen] = useState(false)
  const filterRef = useRef(null)

  useEffect(() => {
    function handler(e) {
      if (filterRef.current && !filterRef.current.contains(e.target)) setFilterOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const filtered = RECOMMENDED_JOBS
    .filter(j =>
      (query === '' || j.title.includes(query) || j.company.includes(query)) &&
      (activeTag === '전체' || j.tags.includes(activeTag))
    )
    .slice()
    .sort((a, b) => {
      if (sortBy === 'wage')     return parseWage(b.wage) - parseWage(a.wage)
      if (sortBy === 'deadline') return new Date(a.deadline) - new Date(b.deadline)
      return (b.isNew ? 1 : 0) - (a.isNew ? 1 : 0)
    })

  const activeSortLabel = SORT_OPTIONS.find(o => o.value === sortBy)?.label

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">공고 검색</h1>
        <p className="text-sm text-gray-500 mt-1">나에게 맞는 공고를 찾아보세요.</p>
      </div>

      <div className="flex gap-3">
        <div className="flex items-center gap-2 bg-white border border-offwhite-200 rounded-xl px-4 py-2.5 flex-1">
          <Search size={16} className="text-gray-400 shrink-0" />
          <input
            type="text"
            placeholder="공고명, 회사명 검색..."
            value={query}
            onChange={e => setQuery(e.target.value)}
            className="bg-transparent text-sm text-navy placeholder-gray-400 outline-none w-full"
          />
          {query && (
            <button onClick={() => setQuery('')} className="text-gray-300 hover:text-gray-500 transition-colors">
              <X size={14} />
            </button>
          )}
        </div>

        {/* 필터 드롭다운 */}
        <div className="relative" ref={filterRef}>
          <button
            onClick={() => setFilterOpen(v => !v)}
            className={`flex items-center gap-2 border rounded-xl px-4 py-2.5 text-sm font-medium transition-colors ${
              filterOpen || sortBy !== 'newest'
                ? 'bg-orange text-white border-orange'
                : 'bg-white text-navy border-offwhite-200 hover:border-navy'
            }`}
          >
            <SlidersHorizontal size={15} />
            {sortBy !== 'newest' ? activeSortLabel : '정렬'}
          </button>

          {filterOpen && (
            <div className="absolute right-0 top-12 w-44 bg-white rounded-xl shadow-lg border border-offwhite-200 z-20 py-1.5 overflow-hidden">
              <p className="px-4 py-1.5 text-[11px] font-semibold text-gray-400 uppercase tracking-wide">정렬 기준</p>
              {SORT_OPTIONS.map(opt => (
                <button
                  key={opt.value}
                  onClick={() => { setSortBy(opt.value); setFilterOpen(false) }}
                  className={`w-full text-left px-4 py-2.5 text-sm transition-colors flex items-center justify-between
                    ${sortBy === opt.value ? 'text-orange font-semibold bg-orange/5' : 'text-gray-600 hover:bg-offwhite-100'}`}
                >
                  {opt.label}
                  {sortBy === opt.value && <span className="w-1.5 h-1.5 rounded-full bg-orange" />}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-hide">
        {TAGS.map(tag => (
          <button
            key={tag}
            onClick={() => setActiveTag(tag)}
            className={`shrink-0 text-xs font-semibold px-3.5 py-1.5 rounded-full border transition-colors ${
              activeTag === tag
                ? 'bg-orange text-white border-orange'
                : 'bg-white text-gray-500 border-offwhite-200 hover:border-navy'
            }`}
          >
            {tag}
          </button>
        ))}
      </div>

      <div className="space-y-3">
        {filtered.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <Search size={32} className="mx-auto mb-3 opacity-30" />
            <p className="text-sm">검색 결과가 없습니다.</p>
          </div>
        ) : (
          filtered.map(job => (
            <div
              key={job.id}
              onClick={() => navigate(`/individual/jobs/${job.id}`)}
              className="bg-white rounded-2xl border border-offwhite-200 p-5 cursor-pointer hover:border-navy transition-colors"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    {job.isNew && (
                      <span className="text-[10px] font-bold bg-orange text-white px-1.5 py-0.5 rounded-full">NEW</span>
                    )}
                    <span className="text-base font-bold text-navy">{job.title}</span>
                  </div>
                  <p className="text-sm text-gray-500 mb-3">{job.company}</p>
                  <div className="flex flex-wrap items-center gap-3 text-xs text-gray-400 mb-3">
                    <span className="flex items-center gap-1"><MapPin size={12} />{job.location}</span>
                    <span className="flex items-center gap-1"><Clock size={12} />마감 {job.deadline}</span>
                  </div>
                  <div className="flex flex-wrap gap-1.5">
                    {job.tags.map(t => (
                      <span key={t} className="text-[11px] bg-offwhite px-2.5 py-1 rounded-full text-gray-500">{t}</span>
                    ))}
                  </div>
                </div>
                <div className="text-right shrink-0">
                  <p className="text-base font-bold text-orange">{job.wage}</p>
                  <button
                    onClick={e => { e.stopPropagation(); toggleSave(job.id) }}
                    className={`mt-2 p-1.5 rounded-lg transition-colors ${isSaved(job.id) ? 'text-orange' : 'text-gray-300 hover:text-orange'}`}
                  >
                    <Bookmark size={18} fill={isSaved(job.id) ? 'currentColor' : 'none'} />
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
