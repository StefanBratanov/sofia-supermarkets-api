package com.stefanbratanov.sofiasupermarketsapi.extractors

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import com.stefanbratanov.sofiasupermarketsapi.common.normalizePrice
import com.stefanbratanov.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.stereotype.Component
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.text.RegexOption.IGNORE_CASE

@Log
@Component("Kaufland")
class KauflandProductsExtractor(
    private val urlValidator: UrlValidator = UrlValidator(),
) : UrlProductsExtractor {

    override fun extract(url: URL): List<Product> {
        val document = try {
            getHtmlDocument(url)
        } catch (ex: Exception) {
            log.info(
                "There was an exception fetching products from {}. " +
                    "Will return 0 products for that url.",
                url,
            )
            return emptyList()
        }

        log.info("Processing Kaufland URL: {}", url.toString())

        val category = document.select(".a-icon-tile-headline__container .a-headline")
            .text()

        val dateRange =
            document.selectFirst(".a-icon-tile-headline__subheadline h2")?.text()?.let {
                "\\d+.\\d+.\\d+".toRegex().findAll(it).map { mr ->
                    val match = mr.groupValues[0]
                    try {
                        LocalDate.parse(match, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                    } catch (ex: Exception) {
                        log.error("Error while parsing $match", ex)
                        null
                    }
                }
            }

        return document.select(".o-overview-list__list-item").mapNotNull {
            val subtitle = it.select(".m-offer-tile__subtitle").text()
            val title = it.select(".m-offer-tile__title").text()
            val quantity = it.select(".m-offer-tile__quantity").text()

            val oldPrice =
                it.select(".a-pricetag__old-price span").text().takeUnless { text ->
                    text.contains("само".toRegex(IGNORE_CASE))
                }
            val price = it.select(".a-pricetag__price").text()

            val picUrls = it.select(".m-offer-tile__image img").attr("data-srcset")
                .split(",").map { urls -> urls.trim() }

            var picUrl = picUrls.getOrNull(1)
                ?.split("\\s+".toRegex())?.first { picUrl ->
                    urlValidator.isValid(picUrl)
                }

            if (Objects.isNull(picUrl)) {
                picUrl = it.select(".m-offer-tile__image img").attr("data-src")
                    .ifEmpty { null }
            }

            Product(
                name = StringUtils.normalizeSpace("$subtitle $title"),
                quantity = StringUtils.normalizeSpace(quantity),
                price = normalizePrice(price),
                oldPrice = normalizePrice(oldPrice),
                category = category,
                picUrl = picUrl,
                validFrom = dateRange?.elementAtOrNull(0),
                validUntil = dateRange?.elementAtOrNull(1),
            )
        }
    }
}
