import { useState } from 'react'

export default function CreateGoalForm({ onSubmit, isSubmitting }) {
  const [title, setTitle] = useState('')
  const [targetAmount, setTargetAmount] = useState('')
  const [targetDate, setTargetDate] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    if (!title.trim() || !targetAmount) return
    await onSubmit({
      title: title.trim(),
      targetAmount: parseFloat(targetAmount),
      targetDate: targetDate || null,
    })
    setTitle('')
    setTargetAmount('')
    setTargetDate('')
  }

  return (
    <form onSubmit={handleSubmit} style={styles.form}>
      <div style={styles.formHeader}>
        <h3 style={styles.formTitle}>What are you saving for?</h3>
        <p style={styles.formHint}>
          Tell us what you want to buy — we'll build a plan from your real spending history.
        </p>
      </div>
      <div style={styles.fields}>
        <input
          type="text"
          placeholder="e.g. iPhone 16, Goa trip, new laptop"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          style={styles.titleInput}
          required
        />
        <input
          type="number"
          placeholder="Target amount (₹)"
          value={targetAmount}
          onChange={(e) => setTargetAmount(e.target.value)}
          style={styles.amountInput}
          min="0"
          step="1"
          required
        />
        <input
          type="date"
          value={targetDate}
          onChange={(e) => setTargetDate(e.target.value)}
          style={styles.dateInput}
        />
        <button type="submit" style={styles.submitBtn} disabled={isSubmitting}>
          {isSubmitting ? 'Building plan…' : 'Create goal'}
        </button>
      </div>
    </form>
  )
}

const styles = {
  form: {
    background: 'var(--color-bg-raised)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    padding: 'var(--space-5)',
    marginBottom: 'var(--space-6)',
  },
  formHeader: {
    marginBottom: 'var(--space-4)',
  },
  formTitle: {
    fontSize: 18,
    marginBottom: 'var(--space-1)',
  },
  formHint: {
    fontSize: 13,
    color: 'var(--color-ink-dim)',
  },
  fields: {
    display: 'flex',
    gap: 'var(--space-3)',
    flexWrap: 'wrap',
  },
  titleInput: {
    flex: 2,
    minWidth: 220,
    background: 'var(--color-bg-raised-2)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-3) var(--space-4)',
    fontSize: 14,
  },
  amountInput: {
    flex: 1,
    minWidth: 140,
    background: 'var(--color-bg-raised-2)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-3) var(--space-4)',
    fontSize: 14,
  },
  dateInput: {
    flex: 1,
    minWidth: 150,
    background: 'var(--color-bg-raised-2)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-3) var(--space-4)',
    fontSize: 14,
    colorScheme: 'dark',
  },
  submitBtn: {
    background: 'var(--color-accent)',
    color: 'var(--color-bg)',
    border: 'none',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-3) var(--space-5)',
    fontSize: 14,
    fontWeight: 600,
  },
}
