package com.stefata.sofiasupermarketsapi.extractors

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.normalizePrice
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Product
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.net.URL

@Log
@Component("Kaufland")
class KauflandProductsExtractor : UrlProductsExtractor {

    override fun extract(url: URL): List<Product> {

        log.info("Processing Kaufland URL: {}", url.toString())

        return getHtmlDoc(url).select(".o-overview-list__list-item").map {
            val subtitle = it.select(".m-offer-tile__subtitle").text()
            val title = it.select(".m-offer-tile__title").text()
            val quantity = it.select(".m-offer-tile__quantity").text()

            val oldPrice = it.select(".a-pricetag__old-price")?.text()
            val price = it.select(".a-pricetag__price")?.text()

            Product(
                name = StringUtils.normalizeSpace("$subtitle $title"),
                quantity = StringUtils.normalizeSpace(quantity),
                price = normalizePrice(price),
                oldPrice = normalizePrice(oldPrice)
            )
        }
    }
}