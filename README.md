# SmartSpend

A goal-driven expense tracker: log expenses, get them auto-categorized by
a machine learning model, see where your money goes, and — the
differentiating feature — tell the app "I want to buy an iPhone for
₹70,000 in 3 months" and get back a specific, data-driven savings plan
generated from your *actual* spending history.

## Architecture

Three independently deployable pieces:

```
frontend/      React app - dashboard, charts, goal UI
backend/       Spring Boot (Java) - auth, expenses, goals, orchestration
ml-service/    Python/FastAPI - ML text classifier for categorization
```

Spring Boot is the system of record: it owns the database, business
rules, and authentication. It calls two external things over plain
REST: the Python ML microservice (for category prediction) and the
Claude API (for turning spending data into a savings plan). Neither of
those services knows anything about users or persistence — they're
both stateless request/response services, which is what makes the
boundary clean.

See `backend/README.md` and `ml-service/README.md` for service-specific
setup. (frontend not yet built — backend services come first.)

## Quick start (local development)

You'll need: Java 17+, Maven, Python 3.10+, PostgreSQL, and an
Anthropic API key.

**1. Start PostgreSQL** and create a database:
```sql
CREATE DATABASE smartspend;
```

**2. Start the ML service** (run once to train, then start the server):
```bash
cd ml-service
pip install -r requirements.txt
python generate_training_data.py
python train_model.py
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

**3. Start the Spring Boot backend** (in a new terminal):
```bash
cd backend
cp .env.example .env   # fill in ANTHROPIC_API_KEY and DB credentials
export $(cat .env | xargs)   # or use your IDE's env var support
mvn spring-boot:run
```

The backend will be live at `http://localhost:8080`, and will seed
default categories on first startup.

**4. Test it:**
```bash
# Sign up
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password123"}'

# Use the returned accessToken for authenticated requests
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"description":"Swiggy order 450","amount":450,"date":"2026-06-20"}'
```

## Status

- **ML microservice** — ✅ built and verified by running it. Model trained
  (97% accuracy on held-out data), tested via FastAPI's `TestClient`
  with real and edge-case inputs.
- **Spring Boot backend** — ⚠️ built (37 files: auth, entities,
  repositories, security/JWT, DTOs, services including the AI goal
  engine, controllers, config), manually cross-checked file-by-file,
  but **not compiler-verified**. This sandbox's network allowlist
  blocks Maven Central (`repo.maven.apache.org` → `403 host_not_allowed`),
  so `mvn compile` could not be run here. Run it yourself as the first
  step before building further on this code.
- **React frontend** — ✅ built and verified: `npm install` and
  `npm run build` both succeed with zero errors, and the dev server was
  started and confirmed serving (HTTP 200) in this environment.
- Not yet built: connecting the frontend's category-correction flow to
  actually retrain the ML model (currently a one-time training script,
  not a feedback loop).

## Why Spring Boot + Python instead of all-Python or all-Java

Worth being able to explain this trade-off out loud: scikit-learn has no
mature Java equivalent for short-text classification, so porting the ML
model to Java would mean a weaker model for no real benefit. Keeping it
as a small, separate, stateless Python service and calling it over REST
from Spring Boot gives a genuine two-language microservices architecture
— each language doing the part it's actually good at — rather than
forcing one language to do both jobs.
