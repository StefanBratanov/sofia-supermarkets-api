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

@Log
@Component("Lidl")
class LidlProductsExtractor(
    private val urlValidator: UrlValidator = UrlValidator()
) : UrlProductsExtractor {

    override fun extract(url: URL): List<Product> {
        log.info("Processing Lidl URL: {}", url.toString())

        val document = getHtmlDocument(url)

        val category = document.selectFirst("meta[property=og:title]")?.attr("content")

        return document.select("article[data-price]")
            .filter {
                !it.select(".lidl-m-pricebox__price").isEmpty()
            }
            .map {
                val dateRange =
                    it.selectFirst(".lidl-m-ribbon-item__text")?.text()?.trim()
                        ?.let { dateSpan ->
                            "\\d+.\\d+.".toRegex().findAll(dateSpan).map { date ->
                                val match = date.groupValues[0]
                                try {
                                    LocalDate.parse(
                                        match.plus(LocalDate.now().year),
                                        DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                    )
                                } catch (ex: Exception) {
                                    log.error("Error while parsing $date", ex)
                                    null
                                }
                            }
                        }

                val name = it.attr("data-name")
                val oldPrice = it.select(".lidl-m-pricebox__discount-price").textNodes()
                    .takeIf { tn ->
                        tn.isNotEmpty()
                    }?.first()?.text()
                val newPrice = it.selectFirst(".lidl-m-pricebox__price")?.text()
                val quantity = it.selectFirst(".lidl-m-pricebox__basic-quantity")?.text()

                var picUrl = it.select("picture").select("source[data-srcset]")
                    .eachAttr("data-srcset")
                    .firstOrNull { srcSet -> srcSet.contains("/sm/") }?.split(",")
                    ?.map { picUrl -> picUrl.trim() }
                    ?.firstOrNull { picUrl ->
                        urlValidator.isValid(picUrl)
                    }

                if (Objects.isNull(picUrl)) {
                    picUrl = it.select("picture").select("img").attr("src")
                }

                Product(
                    name = StringUtils.normalizeSpace(name),
                    price = normalizePrice(newPrice),
                    oldPrice = normalizePrice(oldPrice),
                    quantity = StringUtils.normalizeSpace(quantity),
                    picUrl = picUrl,
                    category = category,
                    validFrom = dateRange?.elementAtOrNull(0),
                    validUntil = dateRange?.elementAtOrNull(1)
                )
            }
    }
}
