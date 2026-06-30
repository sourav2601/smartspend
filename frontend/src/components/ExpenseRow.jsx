import { formatCurrency, formatDateShort, categoryColor } from '../utils/format'

export default function ExpenseRow({ expense, onDelete }) {
  const isLowConfidence = expense.categoryIsAiPredicted && expense.aiConfidence != null && expense.aiConfidence < 0.5

  return (
    <div style={styles.row}>
      <div style={styles.dateCol}>{formatDateShort(expense.date)}</div>
      <div style={styles.descCol}>{expense.description}</div>
      <div style={styles.categoryCol}>
        <span
          style={{
            ...styles.categoryBadge,
            background: `${categoryColor(expense.category?.name)}22`,
            color: categoryColor(expense.category?.name),
          }}
        >
          {expense.category?.name || 'Uncategorized'}
        </span>
        {expense.categoryIsAiPredicted && (
          <span style={isLowConfidence ? styles.aiTagLow : styles.aiTag} title="Predicted by AI">
            {isLowConfidence ? 'AI · low confidence' : 'AI'}
          </span>
        )}
      </div>
      <div className="mono-amount" style={styles.amountCol}>{formatCurrency(expense.amount)}</div>
      <button style={styles.deleteBtn} onClick={() => onDelete(expense.id)} aria-label="Delete expense">
        ×
      </button>
    </div>
  )
}

const styles = {
  row: {
    display: 'flex',
    alignItems: 'center',
    gap: 'var(--space-3)',
    padding: 'var(--space-3) var(--space-2)',
    borderBottom: '1px solid var(--color-border)',
  },
  dateCol: {
    width: 70,
    fontSize: 12,
    color: 'var(--color-ink-faint)',
    flexShrink: 0,
  },
  descCol: {
    flex: 1,
    fontSize: 14,
    color: 'var(--color-ink)',
  },
  categoryCol: {
    display: 'flex',
    alignItems: 'center',
    gap: 'var(--space-2)',
    width: 200,
    flexShrink: 0,
  },
  categoryBadge: {
    fontSize: 12,
    fontWeight: 600,
    padding: '3px 10px',
    borderRadius: 999,
  },
  aiTag: {
    fontSize: 10,
    color: 'var(--color-ink-faint)',
    border: '1px solid var(--color-border)',
    borderRadius: 999,
    padding: '2px 6px',
  },
  aiTagLow: {
    fontSize: 10,
    color: 'var(--color-warning)',
    border: '1px solid var(--color-warning)',
    borderRadius: 999,
    padding: '2px 6px',
  },
  amountCol: {
    width: 100,
    textAlign: 'right',
    fontSize: 14,
    color: 'var(--color-ink)',
    flexShrink: 0,
  },
  deleteBtn: {
    background: 'none',
    border: 'none',
    color: 'var(--color-ink-faint)',
    fontSize: 18,
    width: 24,
    flexShrink: 0,
  },
}
