import { formatCurrency, formatDate } from '../utils/format'

/**
 * The signature visual of the app: a goal's progress rendered as a
 * "runway" toward the purchase, rather than a generic progress bar.
 * The metaphor: you're taxiing toward the thing you want to buy, and
 * the AI plan is what gets you there faster.
 */
export default function GoalRunwayCard({ goal, onRegeneratePlan, onDelete, isRegenerating }) {
  const progress = goal.targetAmount > 0
    ? Math.min(100, (goal.currentSaved / goal.targetAmount) * 100)
    : 0

  const plan = goal.plan || {}
  const cuts = plan.cuts || []
  const hasPlan = cuts.length > 0 || plan.summary

  return (
    <div style={styles.card}>
      <div style={styles.header}>
        <div>
          <div style={styles.eyebrow}>Goal</div>
          <h3 style={styles.title}>{goal.title}</h3>
        </div>
        <div style={styles.targetAmount}>{formatCurrency(goal.targetAmount)}</div>
      </div>

      {/* The runway itself */}
      <div style={styles.runwayTrack}>
        <div style={{ ...styles.runwayFill, width: `${progress}%` }} />
        <div style={styles.runwayMarkings}>
          {[0, 25, 50, 75, 100].map((mark) => (
            <div key={mark} style={{ ...styles.runwayTick, left: `${mark}%` }} />
          ))}
        </div>
        <div style={{ ...styles.runwayPlane, left: `calc(${progress}% - 10px)` }}>✈</div>
        <div style={styles.runwayDestination}>🎯</div>
      </div>

      {plan.hook && (
        <p style={styles.hookText}>{plan.hook}</p>
      )}

      <div style={styles.progressRow}>
        <span style={styles.savedText}>
          <span className="mono-amount" style={{ color: 'var(--color-positive)' }}>
            {formatCurrency(goal.currentSaved)}
          </span>{' '}
          saved
        </span>
        {goal.targetDate && (
          <span style={styles.deadlineText}>Target: {formatDate(goal.targetDate)}</span>
        )}
      </div>

      {hasPlan ? (
        <div style={plan.infeasible ? styles.infeasibleBox : styles.planBox}>
          <div style={plan.infeasible ? styles.infeasibleLabel : styles.planLabel}>
            {plan.infeasible ? 'Deadline too tight' : 'AI savings plan'}
          </div>
          {plan.summary && <p style={styles.planSummary}>{plan.summary}</p>}

          {cuts.length > 0 && (
            <div style={styles.cutsList}>
              {cuts.map((cut, i) => (
                <div key={i} style={styles.cutRow}>
                  <span style={styles.cutCategory}>{cut.category}</span>
                  <span style={styles.cutArrow}>
                    <span className="mono-amount" style={styles.cutFrom}>
                      {formatCurrency(cut.currentMonthly)}
                    </span>
                    {' → '}
                    <span className="mono-amount" style={styles.cutTo}>
                      {formatCurrency(cut.newMonthly)}
                    </span>
                  </span>
                </div>
              ))}
            </div>
          )}

          {!plan.infeasible && plan.projectedCompletionDate && (
            <div style={styles.projectedDate}>
              Projected completion: <strong>{formatDate(plan.projectedCompletionDate)}</strong>
            </div>
          )}
        </div>
      ) : (
        <div style={styles.noPlanBox}>No plan generated yet.</div>
      )}

      <div style={styles.actions}>
        <button
          style={styles.regenerateBtn}
          onClick={() => onRegeneratePlan(goal.id)}
          disabled={isRegenerating}
        >
          {isRegenerating ? 'Updating plan…' : 'Regenerate plan'}
        </button>
        <button style={styles.deleteBtn} onClick={() => onDelete(goal.id)}>
          Remove
        </button>
      </div>
    </div>
  )
}

const styles = {
  card: {
    background: 'var(--color-bg-raised)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    padding: 'var(--space-5)',
    marginBottom: 'var(--space-5)',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 'var(--space-5)',
  },
  eyebrow: {
    fontSize: 11,
    fontWeight: 600,
    letterSpacing: '0.08em',
    textTransform: 'uppercase',
    color: 'var(--color-ink-faint)',
    marginBottom: 'var(--space-1)',
  },
  title: {
    fontSize: 22,
    color: 'var(--color-ink)',
  },
  targetAmount: {
    fontFamily: 'var(--font-display)',
    fontSize: 28,
    fontWeight: 600,
    color: 'var(--color-accent)',
  },
  runwayTrack: {
    position: 'relative',
    height: 10,
    background: 'var(--color-bg-raised-2)',
    borderRadius: 999,
    margin: '0 18px',
    marginBottom: 'var(--space-3)',
  },
  runwayFill: {
    position: 'absolute',
    inset: 0,
    width: 0,
    background: 'linear-gradient(90deg, var(--color-accent-dim), var(--color-accent))',
    borderRadius: 999,
    transition: 'width 0.6s ease',
  },
  runwayMarkings: {
    position: 'absolute',
    inset: 0,
  },
  runwayTick: {
    position: 'absolute',
    top: -3,
    width: 1,
    height: 16,
    background: 'var(--color-border)',
  },
  runwayPlane: {
    position: 'absolute',
    top: -9,
    fontSize: 18,
    transition: 'left 0.6s ease',
    filter: 'drop-shadow(0 0 6px rgba(232,163,61,0.5))',
  },
  runwayDestination: {
    position: 'absolute',
    right: -20,
    top: -7,
    fontSize: 15,
  },
  progressRow: {
    display: 'flex',
    justifyContent: 'space-between',
    fontSize: 13,
    color: 'var(--color-ink-dim)',
    marginBottom: 'var(--space-4)',
  },
  hookText: {
    fontFamily: 'var(--font-display)',
    fontSize: 16,
    fontWeight: 500,
    color: 'var(--color-accent)',
    lineHeight: 1.5,
    margin: '0 18px var(--space-3) 18px',
  },
  savedText: {
    color: 'var(--color-ink-dim)',
  },
  deadlineText: {
    color: 'var(--color-ink-faint)',
  },
  planBox: {
    background: 'var(--color-bg-raised-2)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-4)',
    marginBottom: 'var(--space-4)',
  },
  infeasibleBox: {
    background: 'rgba(217, 113, 78, 0.1)',
    border: '1px solid var(--color-warning)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-4)',
    marginBottom: 'var(--space-4)',
  },
  planLabel: {
    fontSize: 11,
    fontWeight: 600,
    letterSpacing: '0.06em',
    textTransform: 'uppercase',
    color: 'var(--color-accent)',
    marginBottom: 'var(--space-2)',
  },
  infeasibleLabel: {
    fontSize: 11,
    fontWeight: 600,
    letterSpacing: '0.06em',
    textTransform: 'uppercase',
    color: 'var(--color-warning)',
    marginBottom: 'var(--space-2)',
  },
  planSummary: {
    fontSize: 14,
    color: 'var(--color-ink)',
    lineHeight: 1.5,
    marginBottom: 'var(--space-3)',
  },
  cutsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: 'var(--space-2)',
    marginBottom: 'var(--space-3)',
  },
  cutRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'baseline',
    fontSize: 13,
  },
  cutCategory: {
    color: 'var(--color-ink-dim)',
  },
  cutArrow: {
    fontSize: 13,
  },
  cutFrom: {
    color: 'var(--color-ink-faint)',
    textDecoration: 'line-through',
  },
  cutTo: {
    color: 'var(--color-positive)',
    fontWeight: 600,
  },
  projectedDate: {
    fontSize: 13,
    color: 'var(--color-ink-dim)',
    borderTop: '1px solid var(--color-border)',
    paddingTop: 'var(--space-3)',
  },
  noPlanBox: {
    fontSize: 13,
    color: 'var(--color-ink-faint)',
    fontStyle: 'italic',
    marginBottom: 'var(--space-4)',
  },
  actions: {
    display: 'flex',
    gap: 'var(--space-3)',
  },
  regenerateBtn: {
    background: 'var(--color-accent)',
    color: 'var(--color-bg)',
    border: 'none',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-2) var(--space-4)',
    fontSize: 13,
    fontWeight: 600,
  },
  deleteBtn: {
    background: 'none',
    border: '1px solid var(--color-border)',
    color: 'var(--color-ink-faint)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-2) var(--space-4)',
    fontSize: 13,
  },
}