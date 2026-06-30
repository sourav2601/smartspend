import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import { categoryColor, formatCurrency } from '../utils/format'

export default function CategoryBreakdownChart({ data }) {
  if (!data || data.length === 0) {
    return <div style={styles.empty}>No expenses logged for this period yet.</div>
  }

  return (
    <div style={styles.wrapper}>
      <ResponsiveContainer width="100%" height={280}>
        <PieChart>
          <Pie
            data={data}
            dataKey="total"
            nameKey="category"
            innerRadius={70}
            outerRadius={110}
            paddingAngle={2}
            stroke="none"
          >
            {data.map((entry) => (
              <Cell key={entry.category} fill={categoryColor(entry.category)} />
            ))}
          </Pie>
          <Tooltip
            formatter={(value) => formatCurrency(value)}
            contentStyle={{
              background: 'var(--color-bg-raised-2)',
              border: '1px solid var(--color-border)',
              borderRadius: 8,
              color: 'var(--color-ink)',
            }}
          />
          <Legend
            verticalAlign="middle"
            align="right"
            layout="vertical"
            iconType="circle"
            wrapperStyle={{ fontSize: 13, color: 'var(--color-ink-dim)' }}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  )
}

const styles = {
  wrapper: {
    background: 'var(--color-bg-raised)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    padding: 'var(--space-4)',
  },
  empty: {
    background: 'var(--color-bg-raised)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    padding: 'var(--space-7)',
    textAlign: 'center',
    color: 'var(--color-ink-faint)',
    fontSize: 14,
  },
}
