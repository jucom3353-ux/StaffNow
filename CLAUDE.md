# StaffNow Backend — Claude Code Guide

Spring Boot 3.5 + Gradle + Java. 포트 8080.

## Commands

- **서버 실행 (PowerShell):**
  ```
  $env:DB_PASSWORD="A1267843010!"; $env:JWT_SECRET="staffnow-jwt-secret-key-2026-minimum-32chars"; .\gradlew bootRun
  ```
- **빌드:** `.\gradlew build`
- **테스트 제외 빌드:** `.\gradlew build -x test` (DB 없을 때)

## 프로젝트 구조

실제 메인 모듈은 `JUCompany/` 하위에 있음. 루트 `src/`는 구버전 파일.

```
JUCompany/src/main/java/com/example/demo/
├── config/        SecurityConfig, SwaggerConfig
├── controller/    Auth, Application, JobPost, Company, User, Review, WorkAttendance, WorkSession
├── dto/           요청/응답 DTO
├── entity/        User, JobPost, Application, WorkSession, WorkAttendance, Review, RefreshToken 등
├── exception/     GlobalExceptionHandler
├── jwt/           JwtFilter, JwtUtil
├── repository/    각 엔티티 Repository
└── service/       Application, JobPost, Review, User, WorkSession
```

## 주요 도메인

| 도메인 | 설명 |
|--------|------|
| User | 기업 담당자 / 개인 구직자 (Role로 구분) |
| JobPost | 공고 (PostStatus: OPEN/CLOSED) |
| Application | 공고 지원 (ApplicationStatus) |
| WorkSession | 근무 세션 |
| WorkAttendance | 출퇴근 기록 |
| Review | 근로자 평가 |
| Contract | 계약서 |
| RefreshToken | JWT 리프레시 토큰 |

## 현재 진행 상황 (2026-05-18 기준)

**이번 주 핵심 작업 — httpOnly 쿠키 인증 전환**

- 기존: JWT를 Response Body로 반환, 프론트가 localStorage에 저장 후 Authorization 헤더로 전송
- 변경: httpOnly 쿠키 방식으로 전환

변경 대상 파일:
- `AuthController` — 토큰을 Response Body 대신 Set-Cookie 헤더로 반환
- `JwtFilter` — Authorization 헤더 대신 쿠키에서 토큰 읽도록 수정
- `SecurityConfig` — CORS allowCredentials = true, 쿠키 관련 설정 추가

**이번 주 추가 작업**
- 누락 API 보완: 공고 마감일(deadline) 저장 처리, 기업 프로필 companyName 수정 엔드포인트
- 기업 대시보드용 공고별 통계 집계 API (지원자 수, 승인 수, 완료 수)
- 근로자 평가(별점) 등록 엔드포인트 검토

## Frontend 연동

- 프론트: React 19 + Vite, 포트 5173
- 인증 전환 후 CORS에 `allowCredentials = true` 필수
