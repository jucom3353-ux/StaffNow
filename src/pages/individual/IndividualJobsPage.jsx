import { Search, MapPin, Clock, Bookmark, SlidersHorizontal } from 'lucide-react'
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { RECOMMENDED_JOBS } from '../../data/mockIndividual'

const TAGS = ['전체', '단기', '장기', '주말', '야간', '행사/이벤트', '카페', '편의점']

export default function IndividualJobsPage() {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [activeTag, setActiveTag] = useState('전체')

  const filtered = RECOMMENDED_JOBS.filter(j =>
    (query === '' || j.title.includes(query) || j.company.includes(query)) &&
    (activeTag === '전체' || j.tags.includes(activeTag))
  )

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">공고 검색</h1>
        <p className="text-sm text-gray-500 mt-1">나에게 맞는 공고를 찾아보세요.</p>
      </div>

      {/* 검색 */}
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
        </div>
        <button className="flex items-center gap-2 bg-white border border-offwhite-200 rounded-xl px-4 py-2.5 text-sm text-navy hover:border-navy transition-colors">
          <SlidersHorizontal size={15} />
          필터
        </button>
      </div>

      {/* 태그 필터 */}
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

      {/* 결과 */}
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
                    onClick={e => e.stopPropagation()}
                    className={`mt-2 p-1.5 rounded-lg transition-colors ${job.isSaved ? 'text-orange' : 'text-gray-300 hover:text-orange'}`}
                  >
                    <Bookmark size={18} fill={job.isSaved ? 'currentColor' : 'none'} />
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
