import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard' },
  { to: '/expenses', label: 'Expenses' },
  { to: '/goals', label: 'Goals' },
]

export default function AppLayout() {
  const { user, logout } = useAuth()

  return (
    <div style={styles.shell}>
      <aside style={styles.sidebar}>
        <div style={styles.brand}>
          <span style={styles.brandMark}>$</span>
          <span style={styles.brandName}>SmartSpend</span>
        </div>

        <nav style={styles.nav}>
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              style={({ isActive }) => ({
                ...styles.navLink,
                ...(isActive ? styles.navLinkActive : {}),
              })}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div style={styles.sidebarFooter}>
          <div style={styles.userRow}>
            <div style={styles.userAvatar}>{user?.name?.[0]?.toUpperCase() || '?'}</div>
            <div>
              <div style={styles.userName}>{user?.name}</div>
              <div style={styles.userEmail}>{user?.email}</div>
            </div>
          </div>
          <button style={styles.logoutBtn} onClick={logout}>
            Sign out
          </button>
        </div>
      </aside>

      <main style={styles.main}>
        <Outlet />
      </main>
    </div>
  )
}

const styles = {
  shell: {
    display: 'flex',
    minHeight: '100vh',
  },
  sidebar: {
    width: 240,
    flexShrink: 0,
    background: 'var(--color-bg-raised)',
    borderRight: '1px solid var(--color-border)',
    display: 'flex',
    flexDirection: 'column',
    padding: 'var(--space-5) var(--space-4)',
  },
  brand: {
    display: 'flex',
    alignItems: 'center',
    gap: 'var(--space-2)',
    marginBottom: 'var(--space-7)',
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
    color: 'var(--color-ink)',
  },
  nav: {
    display: 'flex',
    flexDirection: 'column',
    gap: 'var(--space-1)',
    flex: 1,
  },
  navLink: {
    textDecoration: 'none',
    color: 'var(--color-ink-dim)',
    padding: 'var(--space-3) var(--space-4)',
    borderRadius: 'var(--radius-md)',
    fontSize: 14,
    fontWeight: 500,
    transition: 'background 0.15s ease, color 0.15s ease',
  },
  navLinkActive: {
    background: 'var(--color-bg-raised-2)',
    color: 'var(--color-ink)',
  },
  sidebarFooter: {
    borderTop: '1px solid var(--color-border)',
    paddingTop: 'var(--space-4)',
  },
  userRow: {
    display: 'flex',
    alignItems: 'center',
    gap: 'var(--space-3)',
    marginBottom: 'var(--space-3)',
  },
  userAvatar: {
    width: 36,
    height: 36,
    borderRadius: '50%',
    background: 'var(--color-bg-raised-2)',
    color: 'var(--color-ink)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontWeight: 600,
    fontFamily: 'var(--font-display)',
    flexShrink: 0,
  },
  userName: {
    fontSize: 14,
    fontWeight: 600,
    color: 'var(--color-ink)',
  },
  userEmail: {
    fontSize: 12,
    color: 'var(--color-ink-faint)',
  },
  logoutBtn: {
    width: '100%',
    background: 'none',
    border: '1px solid var(--color-border)',
    color: 'var(--color-ink-dim)',
    borderRadius: 'var(--radius-md)',
    padding: 'var(--space-2) var(--space-3)',
    fontSize: 13,
    fontWeight: 500,
  },
  main: {
    flex: 1,
    padding: 'var(--space-6) var(--space-7)',
    maxWidth: 1100,
  },
}
