# SmartSpend Frontend

React + Vite single-page app: dashboard, expense logging, and goal-based
AI savings plans.

## Design

Dark, ink-navy palette with one accent (marigold) reserved for money/
progress. The signature visual is the goal "runway" card — a goal's
progress toward its target is shown as a runway with a plane icon
taxiing toward the purchase, rather than a generic progress bar. See
`src/components/GoalRunwayCard.jsx`.

## Setup

```bash
npm install
cp .env.example .env   # point VITE_API_BASE_URL at your backend
npm run dev
```

Runs at `http://localhost:5173` by default. Requires the Spring Boot
backend running (default expected at `http://localhost:8080/api`).

## Build

```bash
npm run build
```

Verified: installs cleanly and builds with zero errors (see project
root README for full verification status across all three services).

## Notes

- Auth token is kept in memory only (React state), not localStorage —
  refreshing the page logs you out. This is a deliberate trade-off for
  this project; a production app would add refresh tokens or a secure
  cookie session.
- `src/api/` holds one file per backend resource (auth, expenses,
  goals) — all built on a shared Axios instance in `client.js` that
  injects the JWT and normalizes error messages.
