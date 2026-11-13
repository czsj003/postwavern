/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api } from './api'
import type { User } from './types'

interface AuthContextValue {
  user: User | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  signup: (name: string, email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

function storedUser(): User | null {
  try {
    const raw = localStorage.getItem('postwave_user')
    return raw ? JSON.parse(raw) as User : null
  } catch { return null }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(storedUser)

  const logout = useCallback(() => {
    localStorage.removeItem('postwave_token')
    localStorage.removeItem('postwave_user')
    setUser(null)
  }, [])

  useEffect(() => {
    window.addEventListener('postwave:unauthorized', logout)
    return () => window.removeEventListener('postwave:unauthorized', logout)
  }, [logout])

  const login = useCallback(async (email: string, password: string) => {
    const result = await api.login(email, password)
    localStorage.setItem('postwave_token', result.token)
    localStorage.setItem('postwave_user', JSON.stringify(result.user))
    setUser(result.user)
  }, [])

  const signup = useCallback(async (name: string, email: string, password: string) => {
    await api.signup(name, email, password)
    await login(email, password)
  }, [login])

  const value = useMemo(() => ({
    user, isAuthenticated: Boolean(user), login, signup, logout,
  }), [user, login, signup, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
