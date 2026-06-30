import { api } from './client'

export async function signup({ name, email, password }) {
  const { data } = await api.post('/auth/signup', { name, email, password })
  return data // { accessToken, tokenType, user }
}

export async function login({ email, password }) {
  const { data } = await api.post('/auth/login', { email, password })
  return data
}
