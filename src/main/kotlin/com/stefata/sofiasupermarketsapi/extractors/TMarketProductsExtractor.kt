package com.stefata.sofiasupermarketsapi.extractors

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.common.normalizePrice
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Product
import org.apache.logging.log4j.util.Strings
import org.springframework.stereotype.Component
import java.net.URL

@Log
@Component("TMarket")
class TMarketProductsExtractor : UrlProductsExtractor {

    override fun extract(url: URL): List<Product> {

        log.info("Processing TMarket URL: {}", url.toString())

        val document = getHtmlDocument(url)

        val category = document.select("._section-title > h1")?.text()

        return document.select("._products-list").select("div[data-box=product]").map {
            val productName = it.select("._product-name").text()

            var price = it.select("._product-price-compare")
            var oldPrice = it.select("._product-price-old")

            if (!price.hasText()) {
                val onlyPrice = it.select(".price")
                price = onlyPrice
                oldPrice = null
            }

            val picUrl = it.select("._product-image img")?.attr("data-src")?.takeUnless { purl ->
                Strings.isBlank(purl)
            }

            Product(
                name = productName,
                price = normalizePrice(price.text()),
                oldPrice = normalizePrice(oldPrice?.text()),
                category = category,
                picUrl = picUrl
            )
        }
    }
}