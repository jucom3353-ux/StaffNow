import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { authApi, userApi, tokenStorage } from '../services/api'

const AUTH_KEY = 'staffnow_auth_user'

const ROLE_LABELS = {
  INDIVIDUAL: '개인 회원',
  BUSINESS:   '기업 회원',
  ADMIN:      '운영자',
}

const ROLE_TO_API = { INDIVIDUAL: 'INDIVIDUAL', BUSINESS: 'COMPANY', ADMIN: 'ADMIN' }
const ROLE_FROM_API = { INDIVIDUAL: 'INDIVIDUAL', COMPANY: 'BUSINESS', ADMIN: 'ADMIN' }

export const ROLE_HOME = {
  INDIVIDUAL: '/individual',
  BUSINESS:   '/dashboard',
  ADMIN:      '/admin',
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

  // login: returns { path } on success, { error } on failure
  const login = useCallback(async (_role, email, password) => {
    try {
      const res = await authApi.login(email, password)
      const body = await res.text()

      if (!res.ok) {
        try { const d = JSON.parse(body); if (d.message) return { error: d.message } } catch {}
        return { error: body || '이메일 또는 비밀번호가 올바르지 않습니다' }
      }

      const data = JSON.parse(body) // { accessToken, refreshToken, role }
      tokenStorage.set(data.accessToken, data.refreshToken)

      const role = ROLE_FROM_API[data.role] ?? data.role
      const userObj = {
        role,
        roleLabel: ROLE_LABELS[role] ?? role,
        email:     data.email ?? email,
        name:      data.name ?? email.split('@')[0],
        avatar:    (data.name ?? email)[0].toUpperCase(),
        phone:     data.phone ?? '',
        mbti:      data.mbti ?? '',
      }

      setUser(userObj)
      return { path: ROLE_HOME[role] }
    } catch {
      return { error: '서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.' }
    }
  }, [])

  // signup: returns {} on success, { error } on failure
  const signup = useCallback(async (role, { name, email, phone, password = '', mbti = '' }) => {
    try {
      const res = await userApi.signup({ email, password, name, phone, role: ROLE_TO_API[role] ?? role, mbti })
      const body = await res.text()

      if (!res.ok) {
        try { const d = JSON.parse(body); if (d.message) return { error: d.message } } catch {}
        return { error: body || '회원가입에 실패했습니다' }
      }

      localStorage.setItem('staffnow_new_signup', 'true')
      return {}
    } catch {
      return { error: '서버에 연결할 수 없습니다. 백엔드 서버가 실행 중인지 확인해주세요.' }
    }
  }, [])

  const logout = useCallback(() => {
    tokenStorage.clear()
    setUser(null)
  }, [])

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
