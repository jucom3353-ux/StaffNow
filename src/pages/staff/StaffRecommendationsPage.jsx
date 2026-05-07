import { useState } from 'react'
import Card from '../../components/ui/Card'
import Button from '../../components/ui/Button'
import { MOCK_APPLICANTS } from '../../data/mockApplicants'
import { Star } from 'lucide-react'

const AVATAR_COLORS = [
  'bg-navy', 'bg-orange', 'bg-emerald-500', 'bg-violet-500',
  'bg-sky-500', 'bg-rose-500', 'bg-amber-500', 'bg-teal-500',
]

const TIER_FILTERS = [
  { key: 'all',  label: '전체' },
  { key: 'top',  label: '고수 (4.5+)' },
  { key: 'mid',  label: '실력자 (3.8–4.4)' },
  { key: 'new',  label: '신규' },
]

function getTier(rating) {
  if (rating === null) return 'new'
  if (rating >= 4.5) return 'top'
  if (rating >= 3.8) return 'mid'
  return 'low'
}

function StarRow({ rating }) {
  if (rating === null) return <span className="text-xs text-gray-400">신규</span>
  const full = Math.floor(rating)
  return (
    <span className="flex items-center gap-0.5">
      {Array.from({ length: 5 }).map((_, i) => (
        <Star
          key={i}
          size={11}
          className={i < full ? 'fill-yellow-400 text-yellow-400' : 'text-gray-200 fill-gray-200'}
        />
      ))}
      <span className="text-xs text-yellow-600 font-semibold ml-1">{rating}</span>
    </span>
  )
}

export default function StaffRecommendationsPage() {
  const [filter, setFilter] = useState('all')
  const [search, setSearch] = useState('')

  const visible = MOCK_APPLICANTS.filter(p => {
    const tierMatch =
      filter === 'all' ||
      (filter === 'top' && getTier(p.rating) === 'top') ||
      (filter === 'mid' && getTier(p.rating) === 'mid') ||
      (filter === 'new' && getTier(p.rating) === 'new')
    const searchMatch = !search || p.name.includes(search) || p.region.includes(search)
    return tierMatch && searchMatch
  })

  return (
    <div className="space-y-5">
      <div className="flex items-start justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-xl font-bold text-navy">추천 인력</h1>
          <p className="text-sm text-gray-500 mt-0.5">총 {MOCK_APPLICANTS.length}명의 인력 풀</p>
        </div>
        <input
          type="text"
          placeholder="이름, 지역 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border border-offwhite-200 rounded-lg px-3 py-1.5 text-sm text-navy placeholder-gray-400 outline-none focus:border-navy w-44"
        />
      </div>

      {/* 티어 필터 탭 */}
      <div className="flex gap-1">
        {TIER_FILTERS.map(t => {
          const count = t.key === 'all'
            ? MOCK_APPLICANTS.length
            : MOCK_APPLICANTS.filter(p =>
                t.key === 'top' ? getTier(p.rating) === 'top' :
                t.key === 'mid' ? getTier(p.rating) === 'mid' :
                getTier(p.rating) === 'new'
              ).length
          return (
            <button
              key={t.key}
              onClick={() => setFilter(t.key)}
              className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-all flex items-center gap-1.5
                ${filter === t.key ? 'bg-navy text-white' : 'text-gray-500 hover:bg-offwhite-100 hover:text-navy'}`}
            >
              {t.label}
              <span className={`text-xs tabular-nums px-1.5 rounded-md
                ${filter === t.key ? 'bg-white/20 text-white' : 'bg-offwhite-200 text-gray-500'}`}>
                {count}
              </span>
            </button>
          )
        })}
      </div>

      {visible.length === 0 ? (
        <Card>
          <p className="text-center text-sm text-gray-400 py-8">검색 결과가 없습니다</p>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {visible.map((person, idx) => (
            <Card key={person.id}>
              <div className="flex items-start gap-3">
                <div className={`w-10 h-10 rounded-full ${AVATAR_COLORS[idx % AVATAR_COLORS.length]} flex items-center justify-center shrink-0`}>
                  <span className="text-white text-sm font-bold">{person.name[0]}</span>
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between gap-1">
                    <p className="font-semibold text-navy text-sm">{person.name}</p>
                    {person.rating !== null && (
                      <span className="text-xs text-yellow-600 font-semibold shrink-0">★{person.rating}</span>
                    )}
                  </div>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {person.age}세 · {person.gender} · {person.region}
                  </p>
                  <div className="flex items-center gap-2 mt-1.5">
                    <StarRow rating={person.rating} />
                    {person.hireCount > 0 && (
                      <span className="text-xs text-orange font-semibold">고용 {person.hireCount}회</span>
                    )}
                  </div>
                </div>
              </div>
              <div className="mt-3 pt-3 border-t border-offwhite-200">
                <Button size="sm" className="w-full justify-center">초대 보내기</Button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
