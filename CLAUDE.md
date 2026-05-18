# StaffNow Dashboard — Claude Code Guide

React 19 + Vite 8 + Tailwind v4 + React Router v7. Plain JavaScript (.js/.jsx), no TypeScript.

## Commands

- **Dev server:** `npm run dev`
- **Production build:** `npm run build`
- **Lint:** `npm run lint` (ESLint flat config)
- **Preview built site:** `npm run preview`

No test framework configured.

## Structure

- `src/` — application source (pages, hooks, components)
- `public/` — static assets
- `dist/` — Vite build output (gitignored)
- `vercel.json` — Vercel deploy config
- `everything-claude-code/` — vendored ECC repo (do not modify in-place)

## Portals

| Role | Path |
|------|------|
| 기업 담당자 | `/dashboard` |
| 개인 구직자 | `/individual` |
| 관리자 | `/admin` |

## Conventions

- Korean is the primary UI language; commit messages follow conventional-commit prefixes (feat/fix/docs/...).
- ESLint config uses the React Hooks plugin — fix lint errors instead of disabling rules.

## 현재 진행 상황 (2026-05-18 기준)

localStorage 전면 제거 + TanStack Query 전환 작업 중.

**이번 주 작업 방향**
- localStorage 사용처 전수 조사 후 제거
- 인증 토큰: httpOnly 쿠키 방식으로 전환 (백엔드 협의 완료)
- 서버 상태(공고·지원·계약 등): TanStack Query(React Query)로 전환
- AppDataContext 역할을 API 호출 + React Query 캐시로 분리

**인증 방식 변경 내용**
- 기존: JWT를 Authorization 헤더로 보내고 localStorage에 저장
- 변경: httpOnly 쿠키 방식으로 전환 — localStorage 저장 제거, Authorization 헤더 제거, 쿠키 자동 전송
- 백엔드에서 AuthController·JwtFilter·SecurityConfig 수정 예정

**주요 변경 대상 파일**
- `src/context/AuthContext` — 토큰·사용자 정보 localStorage 제거
- `src/context/AppDataContext` — 공고·shift·지원·계약 캐시 → TanStack Query로 교체

## Backend 연동

- 백엔드: Spring Boot (Gradle), 포트 8080
- 인증: JWT, httpOnly 쿠키 전환 예정
- CORS allowCredentials = true 설정 필요
