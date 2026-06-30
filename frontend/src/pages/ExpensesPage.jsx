import { useEffect, useState } from 'react'
import AddExpenseForm from '../components/AddExpenseForm'
import ExpenseRow from '../components/ExpenseRow'
import { createExpense, deleteExpense, listExpenses } from '../api/expenses'

export default function ExpensesPage() {
  const [expenses, setExpenses] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    refresh()
  }, [])

  async function refresh() {
    setIsLoading(true)
    try {
      const data = await listExpenses()
      setExpenses(data)
    } catch (err) {
      setError(err.userMessage || 'Failed to load expenses')
    } finally {
      setIsLoading(false)
    }
  }

  async function handleAdd(payload) {
    setIsSubmitting(true)
    setError(null)
    try {
      const created = await createExpense(payload)
      // New expense goes to the top since the list is newest-first.
      setExpenses((prev) => [created, ...prev])
    } catch (err) {
      setError(err.userMessage || 'Failed to add expense')
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleDelete(id) {
    try {
      await deleteExpense(id)
      setExpenses((prev) => prev.filter((e) => e.id !== id))
    } catch (err) {
      setError(err.userMessage || 'Failed to delete expense')
    }
  }

  return (
    <div>
      <header style={styles.header}>
        <h1 style={styles.heading}>Expenses</h1>
        <p style={styles.subheading}>
          Add an expense and the AI will tag its category automatically — correct it any time.
        </p>
      </header>

      {error && <div style={styles.errorBanner}>{error}</div>}

      <AddExpenseForm onSubmit={handleAdd} isSubmitting={isSubmitting} />

      <div style={styles.listCard}>
        <div style={styles.listHeaderRow}>
          <div style={{ width: 70 }}>Date</div>
          <div style={{ flex: 1 }}>Description</div>
          <div style={{ width: 200 }}>Category</div>
          <div style={{ width: 100, textAlign: 'right' }}>Amount</div>
          <div style={{ width: 24 }} />
        </div>

        {isLoading ? (
          <div style={styles.loadingState}>Loading…</div>
        ) : expenses.length === 0 ? (
          <div style={styles.emptyState}>
            No expenses yet. Add your first one above to see AI categorization in action.
          </div>
        ) : (
          expenses.map((expense) => (
            <ExpenseRow key={expense.id} expense={expense} onDelete={handleDelete} />
          ))
        )}
      </div>
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
  listCard: {
    background: 'var(--color-bg-raised)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    padding: 'var(--space-4) var(--space-5)',
  },
  listHeaderRow: {
    display: 'flex',
    gap: 'var(--space-3)',
    padding: 'var(--space-2) var(--space-2)',
    fontSize: 11,
    fontWeight: 600,
    letterSpacing: '0.05em',
    textTransform: 'uppercase',
    color: 'var(--color-ink-faint)',
    borderBottom: '1px solid var(--color-border)',
  },
  loadingState: {
    padding: 'var(--space-6)',
    textAlign: 'center',
    color: 'var(--color-ink-faint)',
    fontSize: 14,
  },
  emptyState: {
    padding: 'var(--space-6)',
    textAlign: 'center',
    color: 'var(--color-ink-faint)',
    fontSize: 14,
  },
}
