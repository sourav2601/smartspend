import { createContext, useContext, useState, useCallback, useEffect } from 'react'
import { setAuthToken } from '../api/client'
import * as authApi from '../api/auth'

const AuthContext = createContext(null)

// NOTE: per artifact storage rules, we never use localStorage/sessionStorage.
// In this in-memory-only model, refreshing the page logs the user out -
// that's an acceptable trade-off for a portfolio project. A real deployment
// would add a refresh-token flow or secure cookie session instead.
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  const [isLoading, setIsLoading] = useState(false)

  const handleAuthResponse = useCallback((response) => {
    setToken(response.accessToken)
    setAuthToken(response.accessToken)
    setUser(response.user)
  }, [])

  const login = useCallback(async (credentials) => {
    setIsLoading(true)
    try {
      const response = await authApi.login(credentials)
      handleAuthResponse(response)
      return response
    } finally {
      setIsLoading(false)
    }
  }, [handleAuthResponse])

  const signup = useCallback(async (details) => {
    setIsLoading(true)
    try {
      const response = await authApi.signup(details)
      handleAuthResponse(response)
      return response
    } finally {
      setIsLoading(false)
    }
  }, [handleAuthResponse])

  const logout = useCallback(() => {
    setToken(null)
    setAuthToken(null)
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, token, isLoading, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
