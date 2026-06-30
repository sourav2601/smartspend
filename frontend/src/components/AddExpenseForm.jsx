import { useState } from 'react'

export default function AddExpenseForm({ onSubmit, isSubmitting }) {
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10))

  async function handleSubmit(e) {
    e.preventDefault()
    if (!description.trim() || !amount) return
    await onSubmit({ description: description.trim(), amount: parseFloat(amount), date })
    setDescription('')
    setAmount('')
  }

  return (
    <form onSubmit={handleSubmit} style={styles.form}>
      <input
        type="text"
        placeholder="What did you spend on? e.g. Swiggy order"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        style={styles.descInput}
        required
      />
      <input
        type="number"
        placeholder="Amount"
        value={amount}
        onChange={(e) => setAmount(e.target.value)}
        style={styles.amountInput}
        min="0"
        step="0.01"
        required
      />
      <input
        type="date"
        value={date}
        onChange={(e) => setDate(e.target.value)}
        style={styles.dateInput}
        required
      />
      <button type="submit" style={styles.submitBtn} disabled={isSubmitting}>
        {isSubmitting ? 'Adding…' : 'Add expense'}
      </button>
    </form>
  )
}

const styles = {
  form: {
    display: 'flex',
    gap: 'var(--space-3)',
    marginBottom: 'var(--space-6)',
    flexWrap: 'wrap',
  },
  descInput: {
    flex: 2,
    minWidth: 220,
    background: 'var(--color-bg-raised)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-3) var(--space-4)',
    fontSize: 14,
  },
  amountInput: {
    flex: 1,
    minWidth: 110,
    background: 'var(--color-bg-raised)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-3) var(--space-4)',
    fontSize: 14,
  },
  dateInput: {
    flex: 1,
    minWidth: 150,
    background: 'var(--color-bg-raised)',
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
