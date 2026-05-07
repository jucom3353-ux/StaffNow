import { Briefcase, Search, Eye, Trash2 } from 'lucide-react'

const JOBS = [
  { id: 1, title: '주말 행사 스태프 모집',    company: '(주)스태프나우', status: 'active',   postedAt: '2026-05-06', applications: 12 },
  { id: 2, title: '6월 박람회 안내 스태프',   company: '코엑스 전시',    status: 'active',   postedAt: '2026-05-05', applications: 8  },
  { id: 3, title: '물류창고 야간 파트',       company: '강남물류(주)',   status: 'reported', postedAt: '2026-05-04', applications: 3  },
  { id: 4, title: '강남 팝업스토어 도우미',   company: '브랜드X',        status: 'closed',   postedAt: '2026-04-28', applications: 19 },
]

const STATUS = {
  active:   { label: '진행 중', color: 'bg-green-100 text-green-700' },
  reported: { label: '신고됨',  color: 'bg-red-100 text-red-700' },
  closed:   { label: '마감',    color: 'bg-gray-100 text-gray-600' },
}

export default function AdminJobsPage() {
  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-navy">공고 관리</h1>
        <p className="text-sm text-gray-500 mt-1">플랫폼 전체 공고 현황</p>
      </div>

      <div className="flex items-center gap-2 bg-white border border-offwhite-200 rounded-xl px-4 py-2.5 max-w-sm">
        <Search size={15} className="text-gray-400" />
        <input type="text" placeholder="공고명 검색..." className="bg-transparent text-sm outline-none w-full placeholder-gray-400 text-navy" />
      </div>

      <div className="bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-offwhite-200 bg-offwhite">
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">공고명</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">회사</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">지원자</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">게시일</th>
              <th className="text-left px-5 py-3 text-xs font-semibold text-gray-500">상태</th>
              <th className="px-5 py-3" />
            </tr>
          </thead>
          <tbody className="divide-y divide-offwhite-200">
            {JOBS.map(job => {
              const s = STATUS[job.status]
              return (
                <tr key={job.id} className="hover:bg-offwhite-100 transition-colors">
                  <td className="px-5 py-3.5 font-semibold text-navy">{job.title}</td>
                  <td className="px-5 py-3.5 text-gray-500">{job.company}</td>
                  <td className="px-5 py-3.5 text-gray-500">{job.applications}명</td>
                  <td className="px-5 py-3.5 text-gray-500">{job.postedAt}</td>
                  <td className="px-5 py-3.5">
                    <span className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${s.color}`}>{s.label}</span>
                  </td>
                  <td className="px-5 py-3.5">
                    <div className="flex gap-1">
                      <button className="p-1.5 rounded hover:bg-offwhite text-gray-400 hover:text-navy"><Eye size={14} /></button>
                      <button className="p-1.5 rounded hover:bg-red-50 text-gray-400 hover:text-red-500"><Trash2 size={14} /></button>
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}
