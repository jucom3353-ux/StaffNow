export const ADMIN_STATS = {
  totalUsers:        12847,
  individualUsers:   11203,
  businessUsers:      1644,
  totalJobs:          3892,
  activeJobs:         1247,
  totalApplications: 28491,
  pendingVerifications:  7,
  reportsToday:          3,
  todayVisitors:       342,
  monthlyGrowth:       '+12.4%',
}

export const PENDING_BUSINESSES = [
  {
    id: 'biz-001',
    company: '(주)이벤트플러스',
    representative: '박사장',
    businessNumber: '123-45-67890',
    requestedAt: '2026-05-06',
    status: 'pending',
  },
  {
    id: 'biz-002',
    company: '강남물류(주)',
    representative: '최대표',
    businessNumber: '234-56-78901',
    requestedAt: '2026-05-05',
    status: 'pending',
  },
  {
    id: 'biz-003',
    company: '서울케이터링',
    representative: '이사장',
    businessNumber: '345-67-89012',
    requestedAt: '2026-05-04',
    status: 'pending',
  },
]

export const RECENT_USERS = [
  { id: 'u-101', name: '이민준', role: 'INDIVIDUAL', email: 'min@test.kr', joinedAt: '2026-05-07', isActive: true },
  { id: 'u-102', name: '(주)한빛물류', role: 'BUSINESS',   email: 'biz2@test.kr', joinedAt: '2026-05-06', isActive: true },
  { id: 'u-103', name: '박서연',  role: 'INDIVIDUAL', email: 'sy@test.kr', joinedAt: '2026-05-06', isActive: true },
  { id: 'u-104', name: '강동원이벤트', role: 'BUSINESS', email: 'kd@test.kr', joinedAt: '2026-05-05', isActive: false },
  { id: 'u-105', name: '최지훈',  role: 'INDIVIDUAL', email: 'jh@test.kr', joinedAt: '2026-05-05', isActive: true },
]

export const ADMIN_REPORTS = [
  {
    id: 'rep-001',
    target: '물류창고 야간 파트',
    targetType: '공고',
    type: '허위 정보',
    reporter: '이민준',
    reporterEmail: 'min@test.kr',
    reportedAt: '2026-05-07',
    detail: '공고에 기재된 급여(시급 15,000원)와 실제 지급 금액(시급 9,860원)이 다릅니다. 계약서와 다른 조건으로 근무를 요구했습니다.',
  },
  {
    id: 'rep-002',
    target: '(주)한빛물류 채용담당',
    targetType: '기업 담당자',
    type: '비매너 행동',
    reporter: '박서연',
    reporterEmail: 'sy@test.kr',
    reportedAt: '2026-05-06',
    detail: '면접 중 부적절한 발언을 했으며, 면접 후 연락 없이 일방적으로 연락을 끊었습니다.',
  },
  {
    id: 'rep-003',
    target: '강남 행사 스태프 모집',
    targetType: '공고',
    type: '허위 정보',
    reporter: '최지훈',
    reporterEmail: 'jh@test.kr',
    reportedAt: '2026-05-05',
    detail: '공고에 명시된 근무 장소와 실제 근무 장소가 달랐습니다.',
  },
]

export const ADMIN_ACTIVITY = [
  { id: 1, text: '새 기업 회원 "(주)이벤트플러스" 인증 요청', time: '방금', type: 'business' },
  { id: 2, text: '신고 접수: 공고 "물류창고 야간" 허위 정보 의심', time: '14분 전', type: 'report' },
  { id: 3, text: '개인 회원 이민준님 가입', time: '32분 전', type: 'user' },
  { id: 4, text: '기업 "강동원이벤트" 계정 비활성화', time: '1시간 전', type: 'admin' },
  { id: 5, text: '공고 "강남 매장 오픈 지원" 완료 처리', time: '2시간 전', type: 'job' },
]
