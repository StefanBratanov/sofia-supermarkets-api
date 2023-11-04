package com.stefanbratanov.sofiasupermarketsapi.extractors

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import com.stefanbratanov.sofiasupermarketsapi.common.normalizePrice
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component

@Log
@Component
class LidlProductExtractor {

  fun extract(url: URL): Product {
    log.debug("Processing Lidl product URL: {}", url)

    val document = getHtmlDocument(url)

    val name = document.selectFirst("h1.keyfacts__title")?.text()
    val quantity = document.selectFirst("div.price-footer")?.text()
    val price = document.selectFirst("div.m-price__price")?.text()
    val oldPrice = document.selectFirst("span.m-price__rrp")?.text()

    val dateRange =
      document.selectFirst("span[data-v-35dadb86]")?.text()?.trim()?.let { dateSpan ->
        "\\d+.\\d+.".toRegex().findAll(dateSpan).map { date ->
          val match = date.groupValues[0]
          try {
            LocalDate.parse(
              match.plus(LocalDate.now().year),
              DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            )
          } catch (ex: Exception) {
            log.error("Error while parsing $date", ex)
            null
          }
        }
      }

    return Product(
      name = StringUtils.normalizeSpace(name),
      quantity = StringUtils.normalizeSpace(quantity),
      price = normalizePrice(price),
      oldPrice = normalizePrice(oldPrice),
      category = null,
      picUrl = null,
      validFrom = dateRange?.elementAtOrNull(0),
      validUntil = dateRange?.elementAtOrNull(1)
    )
  }
}
