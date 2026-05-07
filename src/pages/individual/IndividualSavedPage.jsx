import { Heart, MapPin, Clock, Bookmark } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { RECOMMENDED_JOBS } from '../../data/mockIndividual'

export default function IndividualSavedPage() {
  const navigate = useNavigate()
  const saved = RECOMMENDED_JOBS.filter(j => j.isSaved)

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">관심 공고</h1>
        <p className="text-sm text-gray-500 mt-1">저장한 공고 {saved.length}건</p>
      </div>

      {saved.length === 0 ? (
        <div className="text-center py-16 text-gray-400">
          <Heart size={32} className="mx-auto mb-3 opacity-30" />
          <p className="text-sm">저장한 공고가 없습니다.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {saved.map(job => (
            <div
              key={job.id}
              onClick={() => navigate(`/individual/jobs/${job.id}`)}
              className="bg-white rounded-2xl border border-offwhite-200 p-5 cursor-pointer hover:border-navy transition-colors"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1">
                  <p className="font-bold text-navy mb-1">{job.title}</p>
                  <p className="text-sm text-gray-500 mb-2">{job.company}</p>
                  <div className="flex gap-3 text-xs text-gray-400">
                    <span className="flex items-center gap-1"><MapPin size={11} />{job.location}</span>
                    <span className="flex items-center gap-1"><Clock size={11} />~{job.deadline}</span>
                  </div>
                </div>
                <div className="text-right shrink-0">
                  <p className="font-bold text-orange">{job.wage}</p>
                  <button
                    onClick={e => e.stopPropagation()}
                    className="mt-2 p-1.5 text-orange"
                  >
                    <Bookmark size={18} fill="currentColor" />
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
