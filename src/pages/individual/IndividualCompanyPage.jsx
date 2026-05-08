import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, Building2, User, Phone, Mail, MapPin, Briefcase, Clock, Banknote } from 'lucide-react'
import { RECOMMENDED_JOBS } from '../../data/mockIndividual'
import { useIndividualData } from '../../hooks/useIndividualData'

// 기업별 목업 프로필 데이터
const COMPANY_PROFILES = {
  '(주)스태프나우': {
    name: '(주)스태프나우',
    bizNumber: '123-45-67890',
    address: '서울특별시 강남구 테헤란로 123',
    description: '스태프나우는 단기 인력 매칭 플랫폼을 운영하는 기업입니다. 행사, 전시, 판촉 등 다양한 분야에서 우수 인력을 연결합니다.',
    manager: { name: '김운영', title: '채용 담당자', phone: '010-1234-5678', email: 'biz@staffnow.kr' },
  },
  '코엑스 전시': {
    name: '코엑스 전시',
    bizNumber: '234-56-78901',
    address: '서울특별시 강남구 영동대로 513 코엑스',
    description: '코엑스 전시는 국내 최대 전시·컨벤션 센터로 연간 수백 건의 행사를 진행합니다.',
    manager: { name: '이담당', title: '운영 매니저', phone: '02-6000-0000', email: 'staff@coex.co.kr' },
  },
  '킨텍스': {
    name: '킨텍스',
    bizNumber: '345-67-89012',
    address: '경기도 고양시 일산서구 킨텍스로 217-60',
    description: '킨텍스(KINTEX)는 수도권 최대 규모의 국제 전시·컨벤션 센터입니다.',
    manager: { name: '박매니저', title: '인력 관리팀', phone: '031-995-0000', email: 'hr@kintex.com' },
  },
  '브랜드X': {
    name: '브랜드X',
    bizNumber: '456-78-90123',
    address: '서울특별시 마포구 홍대로 100',
    description: '브랜드X는 팝업스토어 기획·운영 전문 기업으로 전국 주요 상권에서 활발히 활동 중입니다.',
    manager: { name: '최팀장', title: '현장 운영팀장', phone: '010-9876-5432', email: 'ops@brandx.co.kr' },
  },
  'GS25 논현점': {
    name: 'GS25 논현점',
    bizNumber: '567-89-01234',
    address: '서울특별시 강남구 논현동 123-4',
    description: 'GS25 논현점은 24시간 운영하는 편의점으로 야간 아르바이트를 모집합니다.',
    manager: { name: '정점장', title: '점장', phone: '010-5555-6666', email: 'gs25nonhyeon@gs25.com' },
  },
  '스타벅스 역삼점': {
    name: '스타벅스 역삼점',
    bizNumber: '678-90-12345',
    address: '서울특별시 강남구 역삼동 456-7',
    description: '스타벅스 역삼점은 활기차고 팀워크를 중시하는 파트타임 파트너를 모집합니다.',
    manager: { name: '한매니저', title: '점포 매니저', phone: '02-3456-7890', email: 'yeoksam@starbucks.com' },
  },
}

function getProfile(name) {
  return COMPANY_PROFILES[name] ?? {
    name,
    bizNumber: '-',
    address: '-',
    description: '기업 정보가 등록되어 있지 않습니다.',
    manager: { name: '-', title: '-', phone: '-', email: '-' },
  }
}

export default function IndividualCompanyPage() {
  const { name } = useParams()
  const navigate = useNavigate()
  const { isApplied, applyJob } = useIndividualData()

  const companyName = decodeURIComponent(name)
  const profile = getProfile(companyName)

  const jobs = RECOMMENDED_JOBS.filter(j => j.company === companyName)

  return (
    <div className="max-w-2xl mx-auto space-y-5">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-500 hover:text-navy transition-colors"
      >
        <ArrowLeft size={16} />
        돌아가기
      </button>

      {/* 기업 헤더 */}
      <div className="bg-white rounded-2xl border border-offwhite-200 p-6">
        <div className="flex items-center gap-4 mb-5">
          <div className="w-14 h-14 rounded-2xl bg-navy/10 flex items-center justify-center shrink-0">
            <span className="text-navy text-2xl font-bold">{companyName[0]}</span>
          </div>
          <div>
            <h1 className="text-xl font-bold text-navy">{companyName}</h1>
            <p className="text-sm text-gray-400 mt-0.5">사업자 {profile.bizNumber}</p>
          </div>
        </div>

        <p className="text-sm text-gray-600 leading-relaxed mb-5">{profile.description}</p>

        <div className="flex items-center gap-2 text-sm text-gray-500">
          <MapPin size={14} className="text-orange shrink-0" />
          <span>{profile.address}</span>
        </div>
      </div>

      {/* 채용 담당자 정보 */}
      <div className="bg-white rounded-2xl border border-offwhite-200 p-6">
        <div className="flex items-center gap-2 mb-4">
          <User size={16} className="text-navy" />
          <h2 className="font-bold text-navy">채용 담당자</h2>
        </div>
        <div className="grid grid-cols-2 gap-3">
          {[
            { icon: User,     label: '담당자', value: `${profile.manager.name} (${profile.manager.title})` },
            { icon: Phone,    label: '연락처', value: profile.manager.phone },
            { icon: Mail,     label: '이메일', value: profile.manager.email },
            { icon: Building2, label: '소속',  value: companyName },
          ].map(item => (
            <div key={item.label} className="flex items-start gap-3 p-3 bg-offwhite rounded-xl">
              <item.icon size={15} className="text-orange shrink-0 mt-0.5" />
              <div className="min-w-0">
                <p className="text-[10px] text-gray-400 mb-0.5">{item.label}</p>
                <p className="text-sm font-medium text-navy truncate">{item.value}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* 현재 공고 */}
      <div className="bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
        <div className="flex items-center gap-2 px-5 py-4 border-b border-offwhite-200">
          <Briefcase size={16} className="text-navy" />
          <h2 className="font-bold text-navy">현재 모집 공고</h2>
          <span className="ml-auto text-xs text-gray-400">{jobs.length}건</span>
        </div>

        {jobs.length === 0 ? (
          <div className="px-5 py-10 text-center text-gray-400">
            <Briefcase size={28} className="mx-auto mb-2 opacity-30" />
            <p className="text-sm">현재 모집 중인 공고가 없습니다.</p>
          </div>
        ) : (
          <div className="divide-y divide-offwhite-200">
            {jobs.map(job => {
              const applied = isApplied(job.id)
              return (
                <div key={job.id} className="px-5 py-4">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        {job.isNew && (
                          <span className="text-[10px] font-bold bg-orange text-white px-1.5 py-0.5 rounded-full">NEW</span>
                        )}
                        <span className="font-semibold text-navy text-sm">{job.title}</span>
                      </div>
                      <div className="flex flex-wrap gap-3 text-xs text-gray-400 mb-2">
                        <span className="flex items-center gap-1"><Banknote size={11} />{job.wage}</span>
                        <span className="flex items-center gap-1"><MapPin size={11} />{job.location}</span>
                        <span className="flex items-center gap-1"><Clock size={11} />~{job.deadline}</span>
                      </div>
                      <div className="flex flex-wrap gap-1">
                        {job.tags.map(t => (
                          <span key={t} className="text-[11px] bg-offwhite px-2 py-0.5 rounded-full text-gray-500">{t}</span>
                        ))}
                      </div>
                    </div>
                    <button
                      onClick={() => !applied && applyJob({ jobId: job.id, jobTitle: job.title, company: job.company, wage: job.wage, location: job.location })}
                      disabled={applied}
                      className={`shrink-0 text-xs font-semibold px-3 py-1.5 rounded-lg transition-colors ${
                        applied
                          ? 'bg-green-50 text-green-600 border border-green-200 cursor-default'
                          : 'bg-orange text-white hover:bg-orange-600'
                      }`}
                    >
                      {applied ? '지원 완료' : '지원하기'}
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
