import { createContext, useContext, useState, useCallback } from 'react'

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

export const ROLE_HOME = {
  INDIVIDUAL: '/individual',
  BUSINESS:   '/dashboard',
  ADMIN:      '/admin',
}

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)

  const login = useCallback((role) => {
    const u = MOCK_USERS[role]
    setUser(u)
    return ROLE_HOME[role]
  }, [])

  const logout = useCallback(() => setUser(null), [])

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoggedIn: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}
