package com.stefata.sofiasupermarketsapi.extractors

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.common.normalizePrice
import com.stefata.sofiasupermarketsapi.common.separateNameAndQuantity
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Product
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.apache.logging.log4j.util.Strings
import org.springframework.stereotype.Component
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.RegexOption.IGNORE_CASE

@Log
@Component("TMarket")
class TMarketProductsExtractor : UrlProductsExtractor {

    private val ignoreRegex = "Очаквайте".toRegex(IGNORE_CASE)

    override fun extract(url: URL): List<Product> {

        log.info("Processing TMarket URL: {}", url.toString())

        val document = getHtmlDocument(url)

        val category = document.select("._section-title > h1")?.text()

        return document.select("._products-list").select("div[data-box=product]").mapNotNull {
            val productName = it.select("._product-name").text()

            val endDate = it.selectFirst("div[data-end-date]")?.attr("data-end-date")?.let { date ->
                try {
                    LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")).toLocalDate()
                } catch (ex: Exception) {
                    log.error("Error while parsing $date", ex)
                    null
                }
            }

            var price = it.select("._product-price-compare")
            var oldPrice = it.select("._product-price-old")

            if (!price.hasText()) {
                val onlyPrice = it.select(".price")
                price = onlyPrice
                oldPrice = null
            }

            val picUrl = it.select("._product-image img")?.attr("data-src")?.takeUnless { purl ->
                Strings.isBlank(purl)
            }?.replace("600x600.", "300x300.")

            val nameAndQuantity = separateNameAndQuantity(productName)

            if (price.text().contains(ignoreRegex)) {
                null
            } else {
                Product(
                    name = normalizeSpace(nameAndQuantity.first),
                    quantity = normalizeSpace(nameAndQuantity.second),
                    price = normalizePrice(price.text()),
                    oldPrice = normalizePrice(oldPrice?.text()),
                    category = category,
                    picUrl = picUrl,
                    validUntil = endDate
                )
            }
        }
    }
}