import { Users, Briefcase, FileText, Flag, TrendingUp, Clock, Building2, User, ChevronRight } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { ADMIN_STATS, PENDING_BUSINESSES, RECENT_USERS, ADMIN_ACTIVITY } from '../../data/mockAdmin'

const ACTIVITY_COLORS = {
  business: 'bg-blue-100 text-blue-600',
  report:   'bg-red-100 text-red-600',
  user:     'bg-green-100 text-green-600',
  admin:    'bg-orange-100 text-orange',
  job:      'bg-purple-100 text-purple-600',
}

const ACTIVITY_ICONS = {
  business: Building2,
  report:   Flag,
  user:     User,
  admin:    Users,
  job:      Briefcase,
}

export default function AdminDashboardPage() {
  const navigate = useNavigate()

  return (
    <div className="space-y-6">
      {/* 헤더 */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-navy">플랫폼 현황</h1>
          <p className="text-gray-500 text-sm mt-1">실시간 운영 현황을 확인하세요.</p>
        </div>
        <span className="flex items-center gap-1.5 text-xs font-semibold text-purple-600 bg-purple-50 border border-purple-200 px-3 py-1.5 rounded-full">
          <Users size={12} />플랫폼 전체 데이터
        </span>
      </div>

      {/* KPI 카드 */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: '전체 회원',      value: ADMIN_STATS.totalUsers.toLocaleString(),        icon: Users,     color: 'text-blue-500',   bg: 'bg-blue-50',   sub: `개인 ${ADMIN_STATS.individualUsers.toLocaleString()} / 기업 ${ADMIN_STATS.businessUsers.toLocaleString()}` },
          { label: '누적 공고',      value: ADMIN_STATS.totalJobs.toLocaleString(),          icon: Briefcase, color: 'text-purple-500', bg: 'bg-purple-50', sub: `활성 ${ADMIN_STATS.activeJobs.toLocaleString()}건` },
          { label: '누적 지원',      value: ADMIN_STATS.totalApplications.toLocaleString(),  icon: FileText,  color: 'text-green-500',  bg: 'bg-green-50',  sub: `월 성장률 ${ADMIN_STATS.monthlyGrowth}` },
          { label: '오늘 방문자',    value: ADMIN_STATS.todayVisitors.toLocaleString(),       icon: TrendingUp,color: 'text-orange',     bg: 'bg-orange-50', sub: '실시간' },
        ].map(s => (
          <div key={s.label} className="bg-white rounded-2xl p-4 border border-offwhite-200">
            <div className={`w-9 h-9 rounded-xl ${s.bg} flex items-center justify-center mb-3`}>
              <s.icon size={18} className={s.color} />
            </div>
            <p className="text-2xl font-extrabold text-navy tabular-nums">{s.value}</p>
            <p className="text-xs text-gray-500 mt-0.5">{s.label}</p>
            <p className="text-[11px] text-gray-400 mt-1">{s.sub}</p>
          </div>
        ))}
      </div>

      {/* 긴급 처리 배너 */}
      {(ADMIN_STATS.pendingVerifications > 0 || ADMIN_STATS.reportsToday > 0) && (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {ADMIN_STATS.pendingVerifications > 0 && (
            <button
              onClick={() => navigate('/admin/businesses')}
              className="flex items-center justify-between bg-blue-50 border border-blue-200 rounded-2xl px-5 py-4 hover:bg-blue-100 transition-colors text-left"
            >
              <div className="flex items-center gap-3">
                <Building2 size={20} className="text-blue-500" />
                <div>
                  <p className="text-sm font-bold text-navy">기업 인증 대기</p>
                  <p className="text-xs text-gray-500">{ADMIN_STATS.pendingVerifications}건 처리 필요</p>
                </div>
              </div>
              <ChevronRight size={16} className="text-gray-400" />
            </button>
          )}
          {ADMIN_STATS.reportsToday > 0 && (
            <button
              onClick={() => navigate('/admin/reports')}
              className="flex items-center justify-between bg-red-50 border border-red-200 rounded-2xl px-5 py-4 hover:bg-red-100 transition-colors text-left"
            >
              <div className="flex items-center gap-3">
                <Flag size={20} className="text-red-500" />
                <div>
                  <p className="text-sm font-bold text-navy">신고 접수</p>
                  <p className="text-xs text-gray-500">오늘 {ADMIN_STATS.reportsToday}건 신규</p>
                </div>
              </div>
              <ChevronRight size={16} className="text-gray-400" />
            </button>
          )}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 최근 활동 */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
          <div className="px-5 py-4 border-b border-offwhite-200">
            <h2 className="font-bold text-navy">최근 활동</h2>
          </div>
          <div className="divide-y divide-offwhite-200">
            {ADMIN_ACTIVITY.map(item => {
              const Icon = ACTIVITY_ICONS[item.type] || Users
              const color = ACTIVITY_COLORS[item.type] || 'bg-gray-100 text-gray-600'
              return (
                <div key={item.id} className="px-5 py-3.5 flex items-center gap-3">
                  <div className={`w-8 h-8 rounded-lg flex items-center justify-center shrink-0 ${color}`}>
                    <Icon size={14} />
                  </div>
                  <p className="text-sm text-navy flex-1">{item.text}</p>
                  <span className="text-xs text-gray-400 shrink-0 flex items-center gap-1">
                    <Clock size={11} />{item.time}
                  </span>
                </div>
              )
            })}
          </div>
        </div>

        {/* 기업 인증 대기 + 최근 가입 */}
        <div className="space-y-4">
          <div className="bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
              <h2 className="font-bold text-navy text-sm">기업 인증 대기</h2>
              <button
                onClick={() => navigate('/admin/businesses')}
                className="flex items-center gap-1 text-xs text-orange font-semibold hover:underline"
              >
                전체 <ChevronRight size={13} />
              </button>
            </div>
            <div className="divide-y divide-offwhite-200">
              {PENDING_BUSINESSES.map(b => (
                <div key={b.id} className="px-5 py-3">
                  <p className="text-sm font-semibold text-navy">{b.company}</p>
                  <p className="text-xs text-gray-500">{b.representative} · {b.requestedAt}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-offwhite-200 overflow-hidden">
            <div className="flex items-center justify-between px-5 py-4 border-b border-offwhite-200">
              <h2 className="font-bold text-navy text-sm">최근 가입</h2>
              <button
                onClick={() => navigate('/admin/users')}
                className="flex items-center gap-1 text-xs text-orange font-semibold hover:underline"
              >
                전체 <ChevronRight size={13} />
              </button>
            </div>
            <div className="divide-y divide-offwhite-200">
              {RECENT_USERS.slice(0, 4).map(u => (
                <div key={u.id} className="px-5 py-3 flex items-center justify-between">
                  <div>
                    <p className="text-sm font-semibold text-navy">{u.name}</p>
                    <p className="text-xs text-gray-400">{u.role === 'INDIVIDUAL' ? '개인' : '기업'} · {u.joinedAt}</p>
                  </div>
                  <span className={`w-2 h-2 rounded-full ${u.isActive ? 'bg-green-400' : 'bg-gray-300'}`} />
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
