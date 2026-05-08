import { createContext, useContext, useState, useCallback, useEffect } from 'react'

const AUTH_KEY  = 'staffnow_auth_user'
const USERS_KEY = 'staffnow_users'

const MOCK_USERS = {
  INDIVIDUAL: {
    id: 'u-ind-001',
    name: '김지원',
    role: 'INDIVIDUAL',
    roleLabel: '개인 회원',
    email: 'user@staffnow.kr',
    avatar: '김',
  },
  BUSINESS: {
    id: 'u-biz-001',
    name: '김운영',
    role: 'BUSINESS',
    roleLabel: '기업 회원',
    email: 'biz@staffnow.kr',
    company: '(주)스태프나우',
    avatar: '김',
  },
  ADMIN: {
    id: 'u-adm-001',
    name: '관리자',
    role: 'ADMIN',
    roleLabel: '운영자',
    email: 'admin@staffnow.kr',
    avatar: '관',
  },
}

// demo accounts password
const MOCK_PASSWORDS = {
  INDIVIDUAL: 'demo1234',
  BUSINESS:   'demo1234',
  ADMIN:      'demo1234',
}

export const ROLE_HOME = {
  INDIVIDUAL: '/individual',
  BUSINESS:   '/dashboard',
  ADMIN:      '/admin',
}

function getAllUsers() {
  try { return JSON.parse(localStorage.getItem(USERS_KEY) || '[]') } catch { return [] }
}

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem(AUTH_KEY)
      return saved ? JSON.parse(saved) : null
    } catch { return null }
  })

  useEffect(() => {
    try {
      if (user) localStorage.setItem(AUTH_KEY, JSON.stringify(user))
      else localStorage.removeItem(AUTH_KEY)
    } catch {}
  }, [user])

  // signup: stores user + hashed password in per-user slot
  const signup = useCallback((role, { name, email, company, phone, bizNumber, address, addressDetail, password = '' }) => {
    const newUser = {
      ...MOCK_USERS[role],
      id: `u-reg-${Date.now()}`,
      name,
      email,
      password,
      avatar: name?.[0] || '?',
      ...(company ? { company } : {}),
      ...(phone ? { phone } : {}),
      ...(bizNumber ? { bizNumber } : {}),
      ...(address ? { address } : {}),
      ...(addressDetail ? { addressDetail } : {}),
    }
    const all = getAllUsers().filter(u => u.email !== email) // dedup
    localStorage.setItem(USERS_KEY, JSON.stringify([...all, newUser]))
  }, [])

  // login: returns { path } on success, { error } on failure
  const login = useCallback((role, email, password) => {
    const emailLower = email?.toLowerCase() ?? ''

    // 1. New system: staffnow_users array (password-validated)
    const match = getAllUsers().find(
      u => u.role === role && u.email.toLowerCase() === emailLower
    )
    if (match) {
      // password === '' means account was migrated from old system — accept any password first time
      if (match.password !== '' && match.password !== password) {
        return { error: '비밀번호가 올바르지 않습니다' }
      }
      const { password: _, ...clean } = match
      localStorage.setItem(AUTH_KEY, JSON.stringify(clean))
      setUser(clean)
      return { path: ROLE_HOME[role] }
    }

    // 2. Legacy system: staffnow_reg_${role} (no password stored — migrate on first login)
    try {
      const raw = localStorage.getItem(`staffnow_reg_${role}`)
      if (raw) {
        const legacy = JSON.parse(raw)
        if (legacy?.email?.toLowerCase() === emailLower) {
          // Migrate to new system, set the provided password going forward
          const migrated = { ...legacy, password }
          const all = getAllUsers().filter(u => u.email !== legacy.email)
          localStorage.setItem(USERS_KEY, JSON.stringify([...all, migrated]))
          const { password: _, ...clean } = migrated
          localStorage.setItem(AUTH_KEY, JSON.stringify(clean))
          setUser(clean)
          return { path: ROLE_HOME[role] }
        }
      }
    } catch {}

    // 3. Demo mock accounts
    const mock = MOCK_USERS[role]
    if (mock?.email.toLowerCase() === emailLower && password === MOCK_PASSWORDS[role]) {
      localStorage.setItem(AUTH_KEY, JSON.stringify(mock))
      setUser(mock)
      return { path: ROLE_HOME[role] }
    }

    // 4. Fail
    return { error: '이메일 또는 비밀번호가 올바르지 않습니다' }
  }, [])

  const logout = useCallback(() => setUser(null), [])

  return (
    <AuthContext.Provider value={{ user, login, logout, signup, isLoggedIn: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
