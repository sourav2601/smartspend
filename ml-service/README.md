# SmartSpend ML Categorization Service

A standalone Python/FastAPI microservice that predicts an expense category
from its description text, using a TF-IDF + Logistic Regression pipeline
trained with scikit-learn.

## Why a separate service

The core SmartSpend backend is built in Spring Boot (Java). Rather than
porting the ML model to a Java ML library, this model stays in Python -
where scikit-learn is genuinely the right tool - and is exposed as a
small REST API. The Spring Boot backend calls it over HTTP, the same way
it calls the Claude API. This keeps each service in the language best
suited to its job and makes the ML logic independently testable,
deployable, and even replaceable without touching the Java codebase.

## Setup

```bash
pip install -r requirements.txt
```

## Training the model

Training data is synthetic (see `generate_training_data.py` for the
templates used) since no real transaction data is available. To
regenerate data and retrain:

```bash
python generate_training_data.py   # writes training_data.csv
python train_model.py               # trains and saves model/categorizer.joblib
```

Current held-out test accuracy: ~97% across 9 categories (see training
output for the full classification report).

## Running the service

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

The `model/categorizer.joblib` file must exist before starting (run the
training steps above first if it's missing).

## API

### `GET /health`
Returns `{"status": "ok", "model_loaded": true}` - used for container
health checks and to confirm the model loaded successfully at startup.

### `POST /categorize`
Request:
```json
{ "description": "Swiggy order 450" }
```
Response:
```json
{ "category": "Food", "confidence": 0.60 }
```

`confidence` is the model's predicted probability for its top class
(0-1). The Spring Boot backend stores this alongside the expense -
low-confidence predictions are a good candidate for the frontend to
visually flag as "please double-check this category."

## Categories

Must stay in sync with the categories seeded by the Spring Boot backend
(`DataSeeder.java`): Food, Travel, Shopping, Subscriptions, Bills &
Utilities, Entertainment, Health, Education, Other. Adding a new
category requires updating both the training templates here AND the
seeder on the Java side.

## Docker

```bash
docker build -t smartspend-ml-service .
docker run -p 8000:8000 smartspend-ml-service
```
