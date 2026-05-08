import { ArrowLeft, MapPin, Clock, Bookmark, Send, Building2, Banknote, Calendar, CheckCircle2 } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { RECOMMENDED_JOBS } from '../../data/mockIndividual'
import { useIndividualData } from '../../hooks/useIndividualData'

export default function IndividualJobDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isSaved, toggleSave, isApplied, applyJob } = useIndividualData()

  const job = RECOMMENDED_JOBS.find(j => j.id === id) ?? {
    id,
    title: '공고 상세',
    company: '회사명',
    location: '서울',
    wage: '시급 12,000원',
    deadline: '2026-06-01',
    tags: ['단기'],
    isNew: false,
  }

  const applied = isApplied(id)
  const saved   = isSaved(id)

  function handleApply() {
    applyJob({ jobId: job.id, jobTitle: job.title, company: job.company, wage: job.wage, location: job.location })
  }

  return (
    <div className="max-w-2xl mx-auto space-y-5">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-navy transition-colors"
      >
        <ArrowLeft size={16} />
        목록으로
      </button>

      <div className="bg-white rounded-2xl border border-offwhite-200 p-6">
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              {job.isNew && (
                <span className="text-[10px] font-bold bg-orange text-white px-1.5 py-0.5 rounded-full">NEW</span>
              )}
              <div className="flex gap-1.5">
                {job.tags.map(t => (
                  <span key={t} className="text-xs bg-offwhite px-2.5 py-0.5 rounded-full text-gray-500">{t}</span>
                ))}
              </div>
            </div>
            <h1 className="text-xl font-bold text-navy mb-1">{job.title}</h1>
            <p className="text-gray-500 text-sm">{job.company}</p>
          </div>
          <button
            onClick={() => toggleSave(job.id)}
            className={`p-2 rounded-xl border-2 transition-colors ${saved ? 'border-orange text-orange bg-orange-50' : 'border-offwhite-200 text-gray-400 hover:border-orange hover:text-orange'}`}
          >
            <Bookmark size={20} fill={saved ? 'currentColor' : 'none'} />
          </button>
        </div>

        <div className="mt-5 grid grid-cols-2 gap-4">
          {[
            { icon: Banknote,  label: '급여',   value: job.wage },
            { icon: MapPin,    label: '위치',   value: job.location },
            { icon: Calendar,  label: '마감일', value: job.deadline },
            { icon: Building2, label: '회사',   value: job.company },
          ].map(item => (
            <div key={item.label} className="flex items-center gap-3 p-3 bg-offwhite rounded-xl">
              <item.icon size={16} className="text-orange shrink-0" />
              <div>
                <p className="text-[10px] text-gray-400">{item.label}</p>
                <p className="text-sm font-semibold text-navy">{item.value}</p>
              </div>
            </div>
          ))}
        </div>

        <div className="mt-5">
          {applied ? (
            <div className="w-full py-3 rounded-xl bg-green-50 border border-green-200 text-green-700 font-semibold text-sm text-center flex items-center justify-center gap-2">
              <CheckCircle2 size={16} />
              지원 완료! 결과를 기다려주세요.
            </div>
          ) : (
            <button
              onClick={handleApply}
              className="w-full py-3 rounded-xl bg-orange text-white font-bold text-sm hover:bg-orange-600 transition-colors flex items-center justify-center gap-2"
            >
              <Send size={15} />
              지원하기
            </button>
          )}
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-offwhite-200 p-6">
        <h2 className="font-bold text-navy mb-4">공고 내용</h2>
        <div className="space-y-3 text-sm text-gray-600 leading-relaxed">
          <p>안녕하세요! {job.company}입니다.</p>
          <p>이번에 <strong className="text-navy">{job.title}</strong> 포지션으로 열정 있는 스태프를 모집합니다.</p>
          <p className="font-semibold text-navy mt-4">업무 내용</p>
          <ul className="list-disc list-inside space-y-1 text-gray-500">
            <li>행사 진행 보조 및 안내</li>
            <li>현장 스태프 운영 지원</li>
            <li>기타 현장 관련 업무</li>
          </ul>
          <p className="font-semibold text-navy mt-4">지원 자격</p>
          <ul className="list-disc list-inside space-y-1 text-gray-500">
            <li>성실하고 책임감 있는 분</li>
            <li>팀워크를 중시하는 분</li>
            <li>경력 무관, 누구나 지원 가능</li>
          </ul>
        </div>
      </div>
    </div>
  )
}
