export const RECOMMENDED_JOBS = [
  {
    id: 'job-001',
    title: '주말 행사 스태프 모집',
    company: '(주)스태프나우',
    location: '서울 강남구',
    wage: '시급 13,000원',
    deadline: '2026-05-20',
    tags: ['주말', '단기', '행사/이벤트'],
    isNew: true,
    isSaved: false,
  },
  {
    id: 'job-002',
    title: '6월 박람회 안내 스태프',
    company: '코엑스 전시',
    location: '서울 강남구 코엑스',
    wage: '시급 12,000원',
    deadline: '2026-05-25',
    tags: ['단기', '안내/접수'],
    isNew: true,
    isSaved: true,
  },
  {
    id: 'job-005',
    title: '편의점 야간 아르바이트',
    company: 'GS25 논현점',
    location: '서울 강남구 논현동',
    wage: '시급 10,500원',
    deadline: '2026-06-01',
    tags: ['야간', '장기', '편의점'],
    isNew: false,
    isSaved: false,
  },
  {
    id: 'job-006',
    title: '카페 주말 파트타임',
    company: '스타벅스 역삼점',
    location: '서울 강남구 역삼동',
    wage: '시급 11,000원',
    deadline: '2026-05-30',
    tags: ['주말', '카페', '단기'],
    isNew: false,
    isSaved: false,
  },
]

export const MY_APPLICATIONS = [
  {
    id: 'app-001',
    jobTitle: '주말 행사 스태프 모집',
    company: '(주)스태프나우',
    appliedAt: '2026-05-05',
    status: 'pending',
  },
  {
    id: 'app-002',
    jobTitle: '5월 박람회 운영 보조',
    company: '킨텍스',
    appliedAt: '2026-05-01',
    status: 'accepted',
  },
  {
    id: 'app-003',
    jobTitle: '강남 팝업스토어 도우미',
    company: '브랜드X',
    appliedAt: '2026-04-25',
    status: 'rejected',
  },
]

export const INDIVIDUAL_STATS = {
  appliedCount: 3,
  acceptedCount: 1,
  savedCount: 2,
  unreadNotifications: 2,
}
