export const DASHBOARD_STATS = {
  activeJobs:          { value: 4,  delta: { value: 1, dir: 'up',      label: '지난주 대비' } },
  weekShifts:          { value: 12, delta: { value: 3, dir: 'up',      label: '지난주 대비' } },
  pendingInvitations:  { value: 7,  delta: { value: 2, dir: 'up',      label: '지난주 대비' } },
  unpaidPayroll:       { value: 2,  delta: { value: 0, dir: 'neutral', label: '변동 없음'   } },
}

export const RECENT_ACTIVITIES = [
  {
    id: 1,
    type: 'invitation_sent',
    text: '홍길동님을 "주말 행사 스태프" Shift에 초대했습니다',
    time: '10분 전',
    actor: '김운영',
  },
  {
    id: 2,
    type: 'shift_confirmed',
    text: '이영희님이 "5월 10일 오전 Shift" 초대를 수락했습니다',
    time: '32분 전',
    actor: '이영희',
  },
  {
    id: 3,
    type: 'job_created',
    text: '"6월 박람회 안내 스태프" 공고가 생성되었습니다',
    time: '1시간 전',
    actor: '박관리',
  },
  {
    id: 4,
    type: 'contract_signed',
    text: '김철수님 근로계약서가 서명 완료되었습니다',
    time: '2시간 전',
    actor: '김철수',
  },
  {
    id: 5,
    type: 'payroll_approved',
    text: '"5월 1주차" 정산이 승인되었습니다 (3건 · ₩450,000)',
    time: '3시간 전',
    actor: '김운영',
  },
  {
    id: 6,
    type: 'shift_started',
    text: '"강남 매장 오픈 지원" Shift가 시작되었습니다',
    time: '어제',
    actor: null,
  },
]
