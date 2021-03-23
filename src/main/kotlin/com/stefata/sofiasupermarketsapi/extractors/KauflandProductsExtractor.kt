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
import kotlin.text.RegexOption.IGNORE_CASE

@Log
@Component("Kaufland")
class KauflandProductsExtractor : UrlProductsExtractor {

    override fun extract(url: URL): List<Product> {

        log.info("Processing Kaufland URL: {}", url.toString())

        val document = getHtmlDocument(url)

        val category = document.select(".a-icon-tile-headline__container .a-headline")?.text()

        return document.select(".o-overview-list__list-item").mapNotNull {
            val subtitle = it.select(".m-offer-tile__subtitle").text()
            val title = it.select(".m-offer-tile__title").text()
            val quantity = it.select(".m-offer-tile__quantity").text()

            val oldPrice = it.select(".a-pricetag__old-price")?.text().takeUnless { text ->
                text?.contains("само".toRegex(IGNORE_CASE)) == true
            }
            val price = it.select(".a-pricetag__price")?.text()

            val picUrl = it.select(".m-offer-tile__image img")?.attr("data-src")

            Product(
                name = StringUtils.normalizeSpace("$subtitle $title"),
                quantity = StringUtils.normalizeSpace(quantity),
                price = normalizePrice(price),
                oldPrice = normalizePrice(oldPrice),
                category = category,
                picUrl = picUrl
            )
        }
    }
}