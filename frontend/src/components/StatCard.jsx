export default function StatCard({ label, value, accentColor }) {
  return (
    <div style={styles.card}>
      <div style={styles.label}>{label}</div>
      <div className="mono-amount" style={{ ...styles.value, color: accentColor || 'var(--color-ink)' }}>
        {value}
      </div>
    </div>
  )
}

const styles = {
  card: {
    background: 'var(--color-bg-raised)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-4)',
    flex: 1,
  },
  label: {
    fontSize: 12,
    color: 'var(--color-ink-faint)',
    marginBottom: 'var(--space-2)',
  },
  value: {
    fontSize: 24,
    fontWeight: 600,
  },
}
