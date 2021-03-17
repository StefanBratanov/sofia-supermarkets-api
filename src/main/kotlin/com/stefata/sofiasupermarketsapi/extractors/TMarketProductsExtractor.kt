package com.stefata.sofiasupermarketsapi.extractors

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.normalizePrice
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Product
import org.springframework.stereotype.Component
import java.net.URL

@Log
@Component("TMarket")
class TMarketProductsExtractor : UrlProductsExtractor {

    override fun extract(url: URL): List<Product> {

        log.info("Processing TMarket URL: {}", url.toString())

        return getHtmlDoc(url).select("._products-list").select("div[data-box=product]").map {
            val productName = it.select("._product-name").text()

            var price = it.select("._product-price-compare")
            var oldPrice = it.select("._product-price-old")

            if (!price.hasText()) {
                val onlyPrice = it.select(".price")
                price = onlyPrice
                oldPrice = onlyPrice
            }

            Product(
                name = productName,
                price = normalizePrice(price.text()),
                oldPrice = normalizePrice(oldPrice.text())
            )
        }
    }
}