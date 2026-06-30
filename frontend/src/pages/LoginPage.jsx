import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const { login, isLoading } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    try {
      await login({ email, password })
      navigate('/')
    } catch (err) {
      setError(err.userMessage || 'Login failed')
    }
  }

  return (
    <div style={styles.wrapper}>
      <div style={styles.card}>
        <div style={styles.brand}>
          <span style={styles.brandMark}>$</span>
          <span style={styles.brandName}>SmartSpend</span>
        </div>
        <h1 style={styles.heading}>Welcome back</h1>
        <p style={styles.subheading}>Log in to see where your money's going.</p>

        {error && <div style={styles.errorBanner}>{error}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            style={styles.input}
            required
          />
          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={styles.input}
            required
          />
          <button type="submit" style={styles.submitBtn} disabled={isLoading}>
            {isLoading ? 'Logging in…' : 'Log in'}
          </button>
        </form>

        <p style={styles.footerText}>
          Don't have an account? <Link to="/signup" style={styles.link}>Sign up</Link>
        </p>
      </div>
    </div>
  )
}

const styles = {
  wrapper: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 'var(--space-5)',
  },
  card: {
    width: '100%',
    maxWidth: 380,
    background: 'var(--color-bg-raised)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-lg)',
    padding: 'var(--space-7)',
  },
  brand: {
    display: 'flex',
    alignItems: 'center',
    gap: 'var(--space-2)',
    marginBottom: 'var(--space-6)',
  },
  brandMark: {
    width: 28,
    height: 28,
    borderRadius: 'var(--radius-sm)',
    background: 'var(--color-accent)',
    color: 'var(--color-bg)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontFamily: 'var(--font-display)',
    fontWeight: 700,
    fontSize: 16,
  },
  brandName: {
    fontFamily: 'var(--font-display)',
    fontWeight: 600,
    fontSize: 18,
  },
  heading: {
    fontSize: 26,
    marginBottom: 'var(--space-2)',
  },
  subheading: {
    color: 'var(--color-ink-dim)',
    fontSize: 14,
    marginBottom: 'var(--space-5)',
  },
  errorBanner: {
    background: 'rgba(217, 113, 78, 0.12)',
    border: '1px solid var(--color-warning)',
    color: 'var(--color-warning)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-3) var(--space-4)',
    fontSize: 13,
    marginBottom: 'var(--space-4)',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: 'var(--space-3)',
  },
  input: {
    background: 'var(--color-bg-raised-2)',
    border: '1px solid var(--color-border)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-3) var(--space-4)',
    fontSize: 14,
  },
  submitBtn: {
    background: 'var(--color-accent)',
    color: 'var(--color-bg)',
    border: 'none',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-3)',
    fontSize: 14,
    fontWeight: 600,
    marginTop: 'var(--space-2)',
  },
  footerText: {
    marginTop: 'var(--space-5)',
    fontSize: 13,
    color: 'var(--color-ink-faint)',
    textAlign: 'center',
  },
  link: {
    color: 'var(--color-accent)',
    fontWeight: 600,
    textDecoration: 'none',
  },
}
