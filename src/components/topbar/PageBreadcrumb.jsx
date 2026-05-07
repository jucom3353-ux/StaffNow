import { useLocation } from 'react-router-dom'
import { ChevronRight } from 'lucide-react'

const PAGE_LABELS = {
  dashboard:  '대시보드',
  jobs:       '공고 관리',
  shifts:     'Shift 관리',
  staff:      '추천 인력',
  invitations:'초대/확정',
  contracts:  '계약 상태',
  attendance: '근태 관리',
  payroll:    '정산 관리',
  messages:   '메시지',
  settings:   '설정',
  company:    '회사 설정',
  members:    '멤버 관리',
  create:     '생성',
}

export default function PageBreadcrumb() {
  const { pathname } = useLocation()
  const parts = pathname.split('/').filter(Boolean)

  return (
    <nav className="flex items-center gap-1 text-sm">
      {parts.map((part, i) => {
        const isLast = i === parts.length - 1
        const label = PAGE_LABELS[part] ?? part
        return (
          <span key={i} className="flex items-center gap-1">
            {i > 0 && <ChevronRight size={14} className="text-gray-300" />}
            <span className={isLast ? 'font-semibold text-navy' : 'text-gray-400'}>
              {label}
            </span>
          </span>
        )
      })}
    </nav>
  )
}
