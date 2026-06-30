import { useEffect, useState } from 'react'
import StatCard from '../components/StatCard'
import CategoryBreakdownChart from '../components/CategoryBreakdownChart'
import GoalRunwayCard from '../components/GoalRunwayCard'
import { getCategorySummary, listExpenses } from '../api/expenses'
import { listGoals, regeneratePlan as regeneratePlanApi } from '../api/goals'
import { formatCurrency } from '../utils/format'

function startOfMonthISO() {
  const d = new Date()
  return new Date(d.getFullYear(), d.getMonth(), 1).toISOString().slice(0, 10)
}

function todayISO() {
  return new Date().toISOString().slice(0, 10)
}

export default function DashboardPage() {
  const [summary, setSummary] = useState([])
  const [expenseCount, setExpenseCount] = useState(0)
  const [goals, setGoals] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  const [regeneratingId, setRegeneratingId] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setIsLoading(true)
      setError(null)
      try {
        const start = startOfMonthISO()
        const end = todayISO()
        const [summaryData, expenses, goalsData] = await Promise.all([
          getCategorySummary({ start, end }),
          listExpenses({ start, end }),
          listGoals(),
        ])
        if (cancelled) return
        setSummary(summaryData)
        setExpenseCount(expenses.length)
        setGoals(goalsData)
      } catch (err) {
        if (!cancelled) setError(err.userMessage || 'Failed to load dashboard')
      } finally {
        if (!cancelled) setIsLoading(false)
      }
    }

    load()
    return () => { cancelled = true }
  }, [])

  const totalSpend = summary.reduce((sum, c) => sum + c.total, 0)
  const topCategory = summary.length > 0
    ? [...summary].sort((a, b) => b.total - a.total)[0]
    : null

  async function handleRegeneratePlan(goalId) {
    setRegeneratingId(goalId)
    try {
      const updated = await regeneratePlanApi(goalId)
      setGoals((prev) => prev.map((g) => (g.id === goalId ? updated : g)))
    } catch (err) {
      setError(err.userMessage || 'Failed to regenerate plan')
    } finally {
      setRegeneratingId(null)
    }
  }

  if (isLoading) {
    return <div style={styles.loading}>Loading your dashboard…</div>
  }

  return (
    <div>
      <header style={styles.header}>
        <h1 style={styles.heading}>This month</h1>
        <p style={styles.subheading}>Here's where your money went, and what's next.</p>
      </header>

      {error && <div style={styles.errorBanner}>{error}</div>}

      <div style={styles.statsRow}>
        <StatCard label="Total spent" value={formatCurrency(totalSpend)} accentColor="var(--color-accent)" />
        <StatCard label="Transactions" value={expenseCount} />
        <StatCard
          label="Top category"
          value={topCategory ? topCategory.category : '—'}
        />
      </div>

      <div style={styles.chartSection}>
        <h2 style={styles.sectionHeading}>Spending breakdown</h2>
        <CategoryBreakdownChart data={summary} />
      </div>

      <div style={styles.goalsSection}>
        <h2 style={styles.sectionHeading}>Active goals</h2>
        {goals.length === 0 ? (
          <div style={styles.emptyGoals}>
            No goals yet. Head to the Goals tab to set one — tell SmartSpend what
            you're saving for, and it'll build a plan from your real spending.
          </div>
        ) : (
          goals.slice(0, 2).map((goal) => (
            <GoalRunwayCard
              key={goal.id}
              goal={goal}
              onRegeneratePlan={handleRegeneratePlan}
              isRegenerating={regeneratingId === goal.id}
              onDelete={() => {}}
            />
          ))
        )}
      </div>
    </div>
  )
}

const styles = {
  loading: {
    color: 'var(--color-ink-dim)',
    padding: 'var(--space-7)',
    textAlign: 'center',
  },
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
  statsRow: {
    display: 'flex',
    gap: 'var(--space-4)',
    marginBottom: 'var(--space-6)',
  },
  chartSection: {
    marginBottom: 'var(--space-6)',
  },
  sectionHeading: {
    fontSize: 18,
    marginBottom: 'var(--space-4)',
  },
  goalsSection: {
    marginBottom: 'var(--space-6)',
  },
  emptyGoals: {
    background: 'var(--color-bg-raised)',
    border: '1px dashed var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    padding: 'var(--space-6)',
    color: 'var(--color-ink-faint)',
    fontSize: 14,
    lineHeight: 1.6,
  },
}
