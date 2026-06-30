"""
Generates a synthetic but realistic labeled dataset of (description, category)
pairs for training the expense categorizer.

Why synthetic data: real transaction data is sensitive and we don't have
access to any user's actual bank statements. These templates are built
from common merchant names and transaction phrasing patterns seen in
Indian UPI/card transactions, combined programmatically with varying
amounts/phrasing to produce a few hundred diverse examples per category.
This is a reasonable, defensible approach to mention in a viva: "I
bootstrapped training data synthetically, and the in-app correction
feature lets the dataset grow with real user feedback over time."
"""
import csv
import random

random.seed(42)

CATEGORY_TEMPLATES = {
    "Food": [
        "Swiggy order", "Zomato order", "Swiggy Instamart", "Domino's Pizza",
        "McDonald's", "KFC order", "Starbucks coffee", "local restaurant bill",
        "Cafe Coffee Day", "street food vendor", "grocery store - vegetables",
        "BigBasket order", "Blinkit order", "Zepto order", "restaurant dinner",
        "pizza delivery", "biryani order", "ice cream parlour", "bakery purchase",
        "tea stall", "canteen lunch", "food court", "dhaba meal",
    ],
    "Travel": [
        "Uber ride", "Ola cab", "Rapido bike ride", "IRCTC train ticket",
        "petrol pump fuel", "diesel refill", "metro card recharge",
        "auto rickshaw fare", "flight booking IndiGo", "flight booking SpiceJet",
        "bus ticket RedBus", "toll plaza payment", "parking fee",
        "car service center", "bike service", "Ola rental", "airport cab",
        "highway fuel station", "monthly metro pass", "taxi fare",
    ],
    "Shopping": [
        "Amazon order", "Flipkart order", "Myntra purchase", "Ajio order",
        "clothing store purchase", "shoe store", "electronics store",
        "mobile phone purchase", "Croma order", "Reliance Digital",
        "supermarket shopping", "mall purchase", "online shopping cart",
        "watch purchase", "furniture store", "home decor purchase",
        "gift purchase", "jewellery store", "sports shoes order",
    ],
    "Subscriptions": [
        "Netflix subscription", "Amazon Prime renewal", "Spotify premium",
        "Hotstar subscription", "YouTube Premium", "gym membership fee",
        "iCloud storage plan", "Google One subscription", "magazine subscription",
        "Audible subscription", "newspaper subscription", "software license renewal",
        "OTT platform subscription", "cloud storage plan", "music streaming plan",
    ],
    "Bills & Utilities": [
        "electricity bill payment", "water bill payment", "mobile recharge",
        "broadband bill", "DTH recharge", "gas cylinder booking",
        "credit card bill payment", "insurance premium", "rent payment",
        "maintenance society fee", "internet bill", "landline bill",
        "postpaid mobile bill", "water tanker payment", "property tax payment",
    ],
    "Entertainment": [
        "movie ticket BookMyShow", "PVR cinema ticket", "INOX movie ticket",
        "concert ticket", "amusement park entry", "gaming purchase Steam",
        "PlayStation store purchase", "bowling alley", "arcade games",
        "club entry fee", "event ticket booking", "comedy show ticket",
        "theme park", "video game purchase", "music concert",
    ],
    "Health": [
        "pharmacy purchase", "doctor consultation fee", "hospital bill",
        "lab test payment", "Apollo Pharmacy", "dental clinic visit",
        "eye checkup", "physiotherapy session", "health checkup package",
        "medicine purchase", "diagnostic center", "clinic visit fee",
        "Practo consultation", "vitamins purchase", "ambulance fee",
    ],
    "Education": [
        "online course Udemy", "Coursera subscription", "tuition fee payment",
        "college semester fee", "textbook purchase", "exam fee payment",
        "coaching class fee", "stationery purchase", "library fine",
        "certification exam fee", "school fee payment", "skill course payment",
        "workshop registration fee",
    ],
    "Other": [
        "ATM cash withdrawal", "bank service charge", "miscellaneous payment",
        "donation to charity", "gift to friend", "cash transfer to family",
        "lottery ticket", "salon haircut", "spa treatment", "pet store purchase",
        "vet visit", "dry cleaning service", "courier service fee",
        "photocopy and printing", "lost item replacement",
    ],
}

AMOUNT_SUFFIXES = ["", " - {}".format, " Rs.{}"]


def generate_rows(rows_per_category: int = 40):
    rows = []
    for category, templates in CATEGORY_TEMPLATES.items():
        for _ in range(rows_per_category):
            template = random.choice(templates)
            amount = random.choice([99, 150, 250, 349, 499, 650, 899, 1200, 1999, 2500])
            variant = random.choice([
                template,
                f"{template} {amount}",
                f"{template} payment of {amount}",
                f"Paid {amount} for {template.lower()}",
                template.upper(),
            ])
            rows.append((variant, category))
    random.shuffle(rows)
    return rows


def main():
    rows = generate_rows()
    with open("training_data.csv", "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(["description", "category"])
        writer.writerows(rows)
    print(f"Generated {len(rows)} training examples across {len(CATEGORY_TEMPLATES)} categories.")


if __name__ == "__main__":
    main()
