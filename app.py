import os
from datetime import datetime
from flask import Flask, jsonify
import requests
from bs4 import BeautifulSoup

app = Flask(__name__)

KAIULFAND_URL = "https://www.kaufland.bg/sortiment/ceni-v-magazina.html"
HEADERS = {"User-Agent": "Mozilla/5.0 (compatible; Bot/0.1)"}


def scrape_products():
    try:
        response = requests.get(KAIULFAND_URL, headers=HEADERS, timeout=10)
        response.raise_for_status()
    except requests.RequestException as e:
        app.logger.error(f"Request error: {e}")
        raise

    try:
        soup = BeautifulSoup(response.text, "html.parser")
        product_elements = soup.select(".product-box")
        products = []
        for elem in product_elements[:20]:
            title_elem = elem.select_one(".product-title")
            price_elem = elem.select_one(".price")
            if not title_elem or not price_elem:
                continue
            products.append({
                "name": title_elem.get_text(strip=True),
                "price": price_elem.get_text(strip=True),
                "timestamp": datetime.utcnow().isoformat() + "Z",
            })
        return products
    except Exception as e:
        app.logger.error(f"Parsing error: {e}")
        raise


@app.route("/kaufland", methods=["GET"])
def kaufland_endpoint():
    try:
        products = scrape_products()
        return jsonify({"store": "kaufland", "products": products})
    except Exception:
        return jsonify({"error": "Failed to retrieve products"}), 500


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port)

