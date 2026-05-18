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
- State that needs to survive reload goes to `localStorage` (e.g. 채용 확정 데이터).
- ESLint config uses the React Hooks plugin — fix lint errors instead of disabling rules.
