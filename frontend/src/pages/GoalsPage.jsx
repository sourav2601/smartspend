import { useEffect, useState } from 'react'
import CreateGoalForm from '../components/CreateGoalForm'
import GoalRunwayCard from '../components/GoalRunwayCard'
import { createGoal, deleteGoal, listGoals, regeneratePlan } from '../api/goals'

export default function GoalsPage() {
  const [goals, setGoals] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [isCreating, setIsCreating] = useState(false)
  const [regeneratingId, setRegeneratingId] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    refresh()
  }, [])

  async function refresh() {
    setIsLoading(true)
    try {
      const data = await listGoals()
      setGoals(data)
    } catch (err) {
      setError(err.userMessage || 'Failed to load goals')
    } finally {
      setIsLoading(false)
    }
  }

  async function handleCreate(payload) {
    setIsCreating(true)
    setError(null)
    try {
      const created = await createGoal(payload)
      setGoals((prev) => [created, ...prev])
    } catch (err) {
      setError(err.userMessage || 'Failed to create goal')
    } finally {
      setIsCreating(false)
    }
  }

  async function handleRegeneratePlan(goalId) {
    setRegeneratingId(goalId)
    setError(null)
    try {
      const updated = await regeneratePlan(goalId)
      setGoals((prev) => prev.map((g) => (g.id === goalId ? updated : g)))
    } catch (err) {
      setError(err.userMessage || 'Failed to regenerate plan')
    } finally {
      setRegeneratingId(null)
    }
  }

  async function handleDelete(goalId) {
    try {
      await deleteGoal(goalId)
      setGoals((prev) => prev.filter((g) => g.id !== goalId))
    } catch (err) {
      setError(err.userMessage || 'Failed to remove goal')
    }
  }

  return (
    <div>
      <header style={styles.header}>
        <h1 style={styles.heading}>Goals</h1>
        <p style={styles.subheading}>
          Each goal gets a savings plan built from your actual spending — not generic advice.
        </p>
      </header>

      {error && <div style={styles.errorBanner}>{error}</div>}

      <CreateGoalForm onSubmit={handleCreate} isSubmitting={isCreating} />

      {isLoading ? (
        <div style={styles.loadingState}>Loading your goals…</div>
      ) : goals.length === 0 ? (
        <div style={styles.emptyState}>
          No goals yet — create one above to get your first AI savings plan.
        </div>
      ) : (
        goals.map((goal) => (
          <GoalRunwayCard
            key={goal.id}
            goal={goal}
            onRegeneratePlan={handleRegeneratePlan}
            onDelete={handleDelete}
            isRegenerating={regeneratingId === goal.id}
          />
        ))
      )}
    </div>
  )
}

const styles = {
  header: {
    marginBottom: 'var(--space-6)',
  },
  heading: {
    fontSize: 32,
    marginBottom: 'var(--space-2)',
  },
  subheading: {
    color: 'var(--color-ink-dim)',
    fontSize: 14,
  },
  errorBanner: {
    background: 'rgba(217, 113, 78, 0.12)',
    border: '1px solid var(--color-warning)',
    color: 'var(--color-warning)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-3) var(--space-4)',
    fontSize: 13,
    marginBottom: 'var(--space-5)',
  },
  loadingState: {
    padding: 'var(--space-6)',
    textAlign: 'center',
    color: 'var(--color-ink-faint)',
    fontSize: 14,
  },
  emptyState: {
    background: 'var(--color-bg-raised)',
    border: '1px dashed var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    padding: 'var(--space-6)',
    textAlign: 'center',
    color: 'var(--color-ink-faint)',
    fontSize: 14,
  },
}
