# StaffNow Dashboard

단기 스태프 채용 플랫폼 — 기업 운영자용 관리 대시보드

## 기술 스택

- React 18 + Vite
- Tailwind CSS v4
- React Router v7

## 실행 방법

```bash
npm install
npm run dev
```

## 포털 구성

| 역할 | 진입 경로 |
|------|-----------|
| 기업 담당자 | `/dashboard` |
| 개인 구직자 | `/individual` |
| 관리자 | `/admin` |

## 주요 기능

- 공고 생성 및 관리 (계층형 지역 선택, 최저임금 검증)
- Shift 생성 및 인력 선별 (채용/거절, 별표 고정, 사이드 드로워)
- 채용 확정 데이터 localStorage 자동 저장
- 초대/근태/정산 관리
- 알림 시스템
