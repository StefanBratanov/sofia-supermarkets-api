package com.stefata.sofiasupermarketsapi.extractors

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.common.normalizePrice
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Product
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.net.URL

@Log
@Component("Lidl")
class LidlProductsExtractor : UrlProductsExtractor {

    override fun extract(url: URL): List<Product> {

        log.info("Processing Lidl URL: {}", url.toString())

        return getHtmlDocument(url).select("div[data-price],div[data-currency]").map {
            val name = it.select(".product__title").text()
            val oldPrice = it.select(".pricebox__recommended-retail-price")?.textNodes()?.takeIf { tn ->
                tn.isNotEmpty()
            }?.first()?.text()
            val newPrice = it.select(".pricebox__price")?.text()
            val quantity = it.select(".pricebox__basic-quantity")?.text()

            val picUrl = it.select(".picture")?.select("img")?.attr("src")

            Product(
                name = StringUtils.normalizeSpace(name),
                price = normalizePrice(newPrice),
                oldPrice = normalizePrice(oldPrice),
                quantity = StringUtils.normalizeSpace(quantity),
                picUrl = picUrl
            )

        }
    }
}