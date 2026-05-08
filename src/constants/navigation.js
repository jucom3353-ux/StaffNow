import {
  LayoutDashboard,
  Briefcase,
  Calendar,
  Users,
  Mail,
  FileCheck,
  Clock,
  DollarSign,
  MessageSquare,
  Settings,
  UserCog,
} from 'lucide-react'

export const NAV_GROUPS = [
  {
    label: null,
    items: [
      { icon: LayoutDashboard, label: '대시보드', path: '/dashboard' },
      { icon: MessageSquare, label: '메시지', path: '/messages' },
    ],
  },
  {
    label: '핵심 기능',
    items: [
      { icon: Briefcase, label: '공고 관리', path: '/jobs' },
      { icon: Calendar, label: 'Shift 관리', path: '/shifts' },
    ],
  },
  {
    label: '운영',
    items: [
      { icon: Users, label: '추천 인력', path: '/staff' },
      { icon: Mail, label: '초대/확정', path: '/invitations' },
      { icon: FileCheck, label: '계약 상태', path: '/contracts' },
      { icon: Clock, label: '근태 관리', path: '/attendance' },
      { icon: DollarSign, label: '정산 관리', path: '/payroll' },
    ],
  },
  {
    label: '관리',
    items: [
      { icon: Settings, label: '회사 설정', path: '/settings/company' },
      { icon: UserCog, label: '멤버 관리', path: '/settings/members' },
    ],
  },
]
