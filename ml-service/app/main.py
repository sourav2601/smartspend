"""
ML microservice for expense categorization.

Single responsibility: given an expense description string, return the
predicted category and the model's confidence. This service knows
nothing about users, auth, or the database - it's a stateless prediction
endpoint that the Spring Boot backend calls over HTTP. That separation
is the point: the Java backend owns business logic and persistence,
this service owns the ML model, and the two can be developed, tested,
scaled, and even redeployed independently.
"""
import logging
from contextlib import asynccontextmanager

import joblib
from fastapi import FastAPI
from pydantic import BaseModel

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ml-service")

MODEL_PATH = "model/categorizer.joblib"

# Loaded once at startup into this module-level variable, not per-request -
# loading a joblib pipeline involves disk I/O and deserialization, which
# would add unnecessary latency if repeated on every prediction call.
model = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global model
    logger.info("Loading categorization model from %s", MODEL_PATH)
    model = joblib.load(MODEL_PATH)
    logger.info("Model loaded successfully")
    yield
    logger.info("Shutting down ML service")


app = FastAPI(title="SmartSpend ML Categorization Service", lifespan=lifespan)


class CategorizeRequest(BaseModel):
    description: str


class CategorizeResponse(BaseModel):
    category: str
    confidence: float


@app.get("/health")
def health_check():
    return {"status": "ok", "model_loaded": model is not None}


@app.post("/categorize", response_model=CategorizeResponse)
def categorize(request: CategorizeRequest):
    """
    Predicts a category for the given expense description.

    Returns the model's top predicted class plus its probability for
    that class as a confidence score. The Spring Boot backend stores
    this confidence alongside the expense so the frontend can, for
    example, visually flag low-confidence predictions for the user to
    double-check.
    """
    description = request.description.strip()

    if not description:
        return CategorizeResponse(category="Other", confidence=0.0)

    predicted_category = model.predict([description])[0]
    probabilities = model.predict_proba([description])[0]
    confidence = float(max(probabilities))

    return CategorizeResponse(category=predicted_category, confidence=round(confidence, 4))
