import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

export const api = axios.create({
  baseURL: BASE_URL,
})

// Attach the JWT to every outgoing request, if we have one stored.
// Token lives in memory (via AuthContext) and is mirrored here through
// setAuthToken so this module doesn't need to read React context itself.
let currentToken = null

export function setAuthToken(token) {
  currentToken = token
}

api.interceptors.request.use((config) => {
  if (currentToken) {
    config.headers.Authorization = `Bearer ${currentToken}`
  }
  return config
})

// Centralized error shape unwrapping - the Spring Boot backend's
// GlobalExceptionHandler always returns { message, status, error, timestamp },
// so callers can rely on err.userMessage existing on any failed request.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const userMessage =
      error.response?.data?.message || 'Something went wrong. Please try again.'
    error.userMessage = userMessage
    return Promise.reject(error)
  }
)
