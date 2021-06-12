package com.stefata.sofiasupermarketsapi.extractors

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.common.normalizePrice
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Product
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.UrlValidator
import org.apache.logging.log4j.util.Strings.isNotBlank
import org.springframework.stereotype.Component
import java.net.URL
import java.util.*

@Log
@Component("Lidl")
class LidlProductsExtractor(
    private val urlValidator: UrlValidator = UrlValidator()
) : UrlProductsExtractor {

    override fun extract(url: URL): List<Product> {

        log.info("Processing Lidl URL: {}", url.toString())

        val document = getHtmlDocument(url)

        val category = document.select("meta[name=description]")?.attr("content")

        return document.select("div[data-price],div[data-currency]")
            .filter {
                isNotBlank(it.attr("data-price"))
            }
            .map {
                val name = it.select(".product__title").text()
                val oldPrice = it.select(".pricebox__recommended-retail-price")?.textNodes()?.takeIf { tn ->
                    tn.isNotEmpty()
                }?.first()?.text()
                val newPrice = it.select(".pricebox__price")?.text()
                val quantity = it.select(".pricebox__basic-quantity")?.text()

                var picUrl = it.select(".picture").select("source[data-srcset]").eachAttr("data-srcset")
                    ?.firstOrNull { srcSet -> srcSet.contains("/sm/") }?.split(",")
                    ?.map { picUrl -> picUrl.trim() }
                    ?.firstOrNull { picUrl ->
                        urlValidator.isValid(picUrl)
                    }

                if (Objects.isNull(picUrl)) {
                    picUrl = it.select(".picture")?.select("img")?.attr("src")
                }

                Product(
                    name = StringUtils.normalizeSpace(name),
                    price = normalizePrice(newPrice),
                    oldPrice = normalizePrice(oldPrice),
                    quantity = StringUtils.normalizeSpace(quantity),
                    picUrl = picUrl,
                    category = category
                )

            }
    }
}