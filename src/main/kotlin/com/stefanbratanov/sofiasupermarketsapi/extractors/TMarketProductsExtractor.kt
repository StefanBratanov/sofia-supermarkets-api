package com.stefanbratanov.sofiasupermarketsapi.extractors

import com.stefanbratanov.sofiasupermarketsapi.common.*
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.RegexOption.IGNORE_CASE
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.apache.logging.log4j.util.Strings
import org.springframework.stereotype.Component

@Log
@Component("TMarket")
class TMarketProductsExtractor : UrlProductsExtractor {

  private val ignoreRegex = "Очаквайте".toRegex(IGNORE_CASE)

  override fun extract(url: URL): List<Product> {
    log.info("Processing TMarket URL: {}", url.toString())

    val document = getHtmlDocument(url)

    val category = document.selectFirst("._section-title > h1")?.text()

    return document.select("._products-list").select("div[data-box=product]").mapNotNull {
      val productName = it.select("._product-name").text()

      val endDate =
        it.selectFirst("div[data-end-date]")?.attr("data-end-date")?.let { date ->
          try {
            LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
              .toLocalDate()
          } catch (ex: Exception) {
            log.error("Error while parsing $date", ex)
            null
          }
        }

      var price = it.selectFirst("._product-price-compare")
      var oldPrice = it.selectFirst("._product-price-old")

      if (price?.hasText() == false) {
        val onlyPrice = it.selectFirst(".price")
        price = onlyPrice
        oldPrice = null
      }

      val picUrl =
        it
          .selectFirst("._product-image img")
          ?.attr("data-src")
          ?.takeUnless { purl -> Strings.isBlank(purl) }
          ?.replace("600x600.", "300x300.")

      val nameAndQuantity = separateNameAndQuantity(productName)

      if (price?.text()?.contains(ignoreRegex) == true) {
        null
      } else {
        Product(
          name = normalizeSpace(nameAndQuantity.first),
          quantity = normalizeSpace(nameAndQuantity.second),
          price = normalizePrice(price?.text()),
          oldPrice = normalizePrice(oldPrice?.text()),
          category = category,
          picUrl = picUrl,
          validUntil = endDate,
        )
      }
    }
  }
}
