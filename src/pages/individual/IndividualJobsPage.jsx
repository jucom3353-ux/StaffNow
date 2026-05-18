import { Search, MapPin, Clock, Bookmark, SlidersHorizontal, X, List, Map } from 'lucide-react'
import { useState, useRef, useEffect, useCallback } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { jobSearchApi } from '../../services/api'
import { useIndividualData } from '../../hooks/useIndividualData'
import MapView from '../../components/jobs/MapView'

const TAGS = ['전체', '단기', '장기', '주말', '야간', '행사/이벤트', '카페', '편의점']

const SORT_OPTIONS = [
  { value: 'newest',   label: '최신순' },
  { value: 'wage',     label: '급여 높은순' },
  { value: 'deadline', label: '마감 임박순' },
]

const TAG_TO_CATEGORY = {
  '단기': 'SHORT_TERM',
  '장기': 'LONG_TERM',
  '주말': 'WEEKEND',
  '행사/이벤트': 'EVENT',
}

const SORT_TO_API = {
  newest: 'latest',
  wage: 'wage',
  deadline: 'deadline',
}

const WAGE_LABEL = {
  HOURLY: '시급',
  DAILY: '일급',
  MONTHLY: '월급',
  FIXED: '고정급',
}

const CATEGORY_TO_TAG = {
  SHORT_TERM: '단기',
  LONG_TERM: '장기',
  WEEKEND: '주말',
  EVENT: '행사/이벤트',
}

function transformJob(job) {
  return {
    id: job.id,
    title: job.title,
    company: job.companyName,
    location: job.workLocation,
    wage: `${WAGE_LABEL[job.wageType] || '시급'} ${(job.wageAmount || 0).toLocaleString()}원`,
    deadline: job.deadline ? job.deadline.substring(0, 10) : '',
    tags: [CATEGORY_TO_TAG[job.category]].filter(Boolean),
    isNew: job.createdAt && (Date.now() - new Date(job.createdAt).getTime() < 48 * 60 * 60 * 1000),
  }
}

export default function IndividualJobsPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { isSaved, toggleSave } = useIndividualData()

  const [query,      setQuery]      = useState(() => searchParams.get('q') ?? '')
  const [activeTag,  setActiveTag]  = useState('전체')
  const [sortBy,     setSortBy]     = useState('newest')
  const [filterOpen, setFilterOpen] = useState(false)
  const [viewMode,   setViewMode]   = useState('list')
  const [jobs,       setJobs]       = useState([])
  const [loading,    setLoading]    = useState(false)
  const [error,      setError]      = useState(null)

  const filterRef = useRef(null)

  useEffect(() => {
    function handler(e) {
      if (filterRef.current && !filterRef.current.contains(e.target)) setFilterOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const fetchJobs = useCallback(async (q, tag, sort) => {
    setLoading(true)
    setError(null)
    try {
      const res = await jobSearchApi.search({
        title: q || undefined,
        category: TAG_TO_CATEGORY[tag] || undefined,
        sort: SORT_TO_API[sort],
      })
      if (!res.ok) throw new Error('공고를 불러오지 못했습니다.')
      const data = await res.json()
      const list = Array.isArray(data) ? data : (data.content ?? [])
      setJobs(list.map(transformJob))
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchJobs(query, activeTag, sortBy)
    }, 300)
    return () => clearTimeout(timer)
  }, [query, activeTag, sortBy, fetchJobs])

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

        <div className="flex bg-offwhite-100 border border-offwhite-200 rounded-xl p-1 gap-0.5">
          <button
            onClick={() => setViewMode('list')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-semibold transition-colors ${
              viewMode === 'list' ? 'bg-white text-navy shadow-sm' : 'text-gray-400 hover:text-navy'
            }`}
          >
            <List size={15} />
          </button>
          <button
            onClick={() => setViewMode('map')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-semibold transition-colors ${
              viewMode === 'map' ? 'bg-white text-navy shadow-sm' : 'text-gray-400 hover:text-navy'
            }`}
          >
            <Map size={15} />
          </button>
        </div>

        {viewMode === 'list' && (
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
        )}
      </div>

      <div className="flex gap-2 overflow-x-auto pb-3 scrollbar-hide">
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

      {viewMode === 'map' ? (
        <MapView
          jobs={jobs}
          onJobClick={id => navigate(`/individual/jobs/${id}`)}
        />
      ) : (
        <div className="space-y-3">
          {loading && (
            <div className="text-center py-16 text-gray-400">
              <p className="text-sm">공고를 불러오는 중...</p>
            </div>
          )}

          {!loading && error && (
            <div className="text-center py-16 text-gray-400">
              <p className="text-sm text-red-400">{error}</p>
            </div>
          )}

          {!loading && !error && jobs.length === 0 && (
            <div className="text-center py-16 text-gray-400">
              <Search size={32} className="mx-auto mb-3 opacity-30" />
              <p className="text-sm">검색 결과가 없습니다.</p>
            </div>
          )}

          {!loading && !error && jobs.map(job => (
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
                    onClick={e => { e.stopPropagation(); toggleSave(String(job.id)) }}
                    className={`mt-2 p-1.5 rounded-lg transition-colors ${isSaved(String(job.id)) ? 'text-orange' : 'text-gray-300 hover:text-orange'}`}
                  >
                    <Bookmark size={18} fill={isSaved(job.id) ? 'currentColor' : 'none'} />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
