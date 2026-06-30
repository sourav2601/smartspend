import { api } from './client'

export async function createExpense({ description, amount, date, categoryId }) {
  const { data } = await api.post('/expenses', { description, amount, date, categoryId })
  return data
}

export async function updateExpense(id, updates) {
  const { data } = await api.put(`/expenses/${id}`, updates)
  return data
}

export async function deleteExpense(id) {
  await api.delete(`/expenses/${id}`)
}

export async function listExpenses({ start, end } = {}) {
  const { data } = await api.get('/expenses', { params: { start, end } })
  return data
}

export async function getCategorySummary({ start, end }) {
  const { data } = await api.get('/expenses/summary', { params: { start, end } })
  return data
}

export async function listCategories() {
  const { data } = await api.get('/categories')
  return data
}
