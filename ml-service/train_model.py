"""
Trains the expense categorization model and saves it to disk as a single
pickled sklearn Pipeline (TF-IDF vectorizer + Logistic Regression).

Why this approach (good to be able to defend in an interview):
- Expense descriptions are short text snippets ("Swiggy order 350") -
  this is a classic short-text classification problem, not one that
  benefits from deep learning. TF-IDF + Logistic Regression is fast to
  train, fast to serve, easy to interpret, and performs very well on
  this kind of small-vocabulary, merchant-keyword-driven text.
- Bundling vectorizer + classifier into one sklearn Pipeline means we
  save/load ONE artifact and never risk a train/serve mismatch between
  the vectorizer's vocabulary and the model's expected input shape.
"""
import csv
import joblib
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report


def load_data(path="training_data.csv"):
    descriptions, labels = [], []
    with open(path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            descriptions.append(row["description"])
            labels.append(row["category"])
    return descriptions, labels


def main():
    descriptions, labels = load_data()

    X_train, X_test, y_train, y_test = train_test_split(
        descriptions, labels, test_size=0.2, random_state=42, stratify=labels
    )

    pipeline = Pipeline([
        ("tfidf", TfidfVectorizer(
            lowercase=True,
            ngram_range=(1, 2),   # unigrams + bigrams capture phrases like "gas cylinder"
            min_df=1,
            max_features=5000,
        )),
        ("clf", LogisticRegression(
            max_iter=1000,
            C=5.0,                # slightly relaxed regularization - vocabulary is small
            class_weight="balanced",
        )),
    ])

    pipeline.fit(X_train, y_train)

    y_pred = pipeline.predict(X_test)
    print("Evaluation on held-out test set:")
    print(classification_report(y_test, y_pred))

    joblib.dump(pipeline, "model/categorizer.joblib")
    print("Saved trained pipeline to model/categorizer.joblib")


if __name__ == "__main__":
    main()
