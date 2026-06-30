import { api } from './client'

export async function createGoal({ title, targetAmount, targetDate }) {
  const { data } = await api.post('/goals', { title, targetAmount, targetDate })
  return data
}

export async function regeneratePlan(goalId) {
  const { data } = await api.post(`/goals/${goalId}/regenerate-plan`)
  return data
}

export async function listGoals() {
  const { data } = await api.get('/goals')
  return data
}

export async function deleteGoal(goalId) {
  await api.delete(`/goals/${goalId}`)
}
