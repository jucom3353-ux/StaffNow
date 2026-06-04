# StaffNow (스태프나우)

> BTL/현장 인력 구인구직 플랫폼 — 기업/구직자/관리자 통합 서비스

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.0 |
| Security | Spring Security (JWT + HttpOnly Cookie) |
| ORM | Spring Data JPA |
| Database | MySQL 8.0 |
| Real-time | WebSocket (SockJS + STOMP) |
| PDF | iText7 |
| QR | ZXing |

### 외부 연동
- 카카오 Geocoding API (주소 → 위도/경도 변환)
- 국세청 API (사업자번호 검증)
- Gmail SMTP (이메일 인증)
- Firebase Admin SDK (FCM 푸시 알림)
- CoolSMS (SMS 알림 - 연동 예정)

---

## 실행 방법

```bash
./gradlew bootRun
```

### 환경변수 설정 (.env)
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret_32chars
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your_app_password
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_API_KEY=your_kakao_api_key
NTS_API_KEY=your_nts_api_key

---

## 포털 구성

| 역할 | 진입 경로 |
|------|----------|
| 기업 담당자 | /dashboard |
| 개인 구직자 | /individual |
| 관리자 | /admin |

---

## 권한 체계

| 역할 | 설명 |
|------|------|
| ADMIN | 전체 관리 |
| COMPANY | 기업 담당자 |
| MANAGER | 기업 소속 매니저 |
| INDIVIDUAL | 구직자 |

---

## 인증

- JWT Access Token (1시간) + Refresh Token (7일)
- HttpOnly Cookie 기반
- 카카오/구글/네이버 소셜 로그인 (연동 예정)
- 2단계 인증 (ADMIN 필수 / 기업·구직자 선택)

---

## 주요 도메인

| 도메인 | 설명 |
|--------|------|
| 공고 (JobPost) | 생성/수정/삭제/상태관리/조회수/즐겨찾기/템플릿/복사 |
| 공고 검색 | 자동완성 (제목/지역/기업명), 구직자 맞춤 추천 |
| 지원 (Application) | 지원/승인/거절/완료/노쇼/결근, 온라인/전화/문자 지원방식 |
| 계약 (Contract) | 생성/서명/PDF 생성/도장 적용 |
| 근태 (WorkAttendance) | GPS 출퇴근/QR 출퇴근/사진 메타데이터 |
| 정산 (Payroll) | 자동 생성/승인/지급/휴게시간 차감/3.3% 세금 공제 |
| 마일리지 | 적립/차감/출금/부스트 교환 |
| 목표 시스템 | 목표 금액 설정/자동 누적/달성 시 보너스 마일리지 |
| 등급 시스템 | 아마추어/스탭/프로/프로모터 (근무 횟수 + 경력 기반) |
| 구독 | BASIC/STANDARD/PREMIUM 플랜 |
| 공지사항 | ADMIN/COMPANY 공지 분리/대상자 알림 |
| 이벤트 | CRUD/당첨자 발표/자동 종료 |
| 포트폴리오 | 현장 사진/설명/카테고리 (최대 10장) |
| 행사 캘린더 | 월별/지역별 공고 캘린더 (비회원 가능) |
| 메시지 | 1:1 채팅/WebSocket/신고 |
| 알림 | 실시간 WebSocket + FCM 푸시 + SMS |
| 초대 코드 | 기업 초대 코드 발급/관리 |
| QR 출퇴근 | WorkSession별 QR 생성/스캔 |
| 상단 노출 | 공고 메인 노출 신청 (7일/30만원) |
| 얼리버드 | 사전 등록/마케팅 수신 동의 |
| 구직자 검색 | 지역/MBTI/성별/나이/시간대/포트폴리오/자격증 필터 |
| 리뷰 | 성실도/친절도/숙련도 세부 평가 |
| 스크랩 | 구직자 스크랩/바로 초대 |

---

## 핵심 운영 플로우
공고 생성 (COMPANY/MANAGER)
└── 카카오 Geocoding 주소 자동 변환
└── 긴급 공고 등록 시 즉시출근 가능 구직자 자동 알림
└── 템플릿 저장/불러오기 가능
WorkSession (Shift) 생성
└── 휴게시간 설정 → 실급여 자동 계산
└── QR 코드 생성 → 현장 부착
지원 및 채용
└── 온라인/전화/문자 지원 방식 선택
└── 승인 시 근로계약서 자동 생성
계약 서명
└── 기업: 도장 자동 적용
└── 구직자: 서명 이미지 업로드
└── 계약서 PDF 자동 생성
근태 관리
└── QR 스캔 or GPS 사진 출퇴근
└── 지각/결근/노쇼 자동 처리
└── 온도 점수 반영
정산
└── 퇴근 시 자동 생성
└── 휴게시간 차감 + 3.3% 세금 공제
└── 마일리지 적립 + 목표 금액 누적

---

## 스케줄러 목록

| 스케줄러 | 실행 주기 | 역할 |
|----------|----------|------|
| JobPostScheduler | 매일 자정 | 마감일 지난 공고 자동 CLOSED |
| ContractScheduler | 매일 | 미서명 계약서 만료 처리 |
| PayrollScheduler | 매주 월요일 | 정산 자동 생성 |
| ProfileBoostScheduler | 매 시간 | 부스트 만료 처리 |
| RefreshTokenScheduler | 매일 새벽 3시 | 만료 토큰 정리 |
| EventScheduler | 매일 자정 | 종료일 지난 이벤트 ENDED 처리 |
| JobPostExposureScheduler | 매 시간 | 상단 노출 만료 처리 |
| AbsentScheduler | 매일 | 미출근자 결근 처리 |
| InvitationScheduler | 매일 | 초대 만료 처리 |
| SubscriptionScheduler | 매일 | 구독 만료 처리 |
| BookmarkScheduler | 매일 오전 9시 | 북마크 공고 마감 D-3 알림 |
| MarketingScheduler | 매일 | 미접속 7일 구직자 알림 |

---

## 보안

- JWT HttpOnly Cookie + Refresh Token 자동 갱신
- CORS 환경변수화 (`app.cors.allowed-origins`)
- Rate Limit (로그인 10회/분, 회원가입 5회/분)
- 구독 플랜별 API 호출 제한
- 민감정보 분리 (계좌정보 본인/ADMIN만 노출)
- Swagger 운영 환경 비활성화

---

## 성능 최적화

- `JobPostRepository` EntityGraph (user, category fetch join)
- `ApplicationRepository` EntityGraph (user, jobPost, jobPostRole fetch join)
- `WorkAttendanceRepository` EntityGraph (application, user, jobPost fetch join)

---

## 단위 테스트

| 테스트 | 케이스 수 |
|--------|---------|
| ApplicationServiceTest | 12개 |
| MileageServiceTest | 10개 |
| ContractServiceTest | 11개 |
| PayrollServiceTest | 12개 |
| UserServiceTest | 14개 |
| ReviewServiceTest | 9개 |
| NotificationServiceTest | 7개 |
| GoalServiceTest | 10개 |
| GradeServiceTest | 8개 |
| **합계** | **93개** |

---

## 보류 중 (추후 구현)

- PG 연동 (결제/수수료/공고 상단 노출)
- SMS 알림 업체 확정 후 연동 (CoolSMS/NCP)
- 소셜 로그인 API 키 발급 후 연동
- 마일리지 적립 나머지 (정시출근/근무완료/리뷰/추천)
- 카테고리 확정
- 기업 이력서 열람 체험판