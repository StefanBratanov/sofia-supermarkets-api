package com.stefanbratanov.sofiasupermarketsapi.extractors

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import com.stefanbratanov.sofiasupermarketsapi.common.normalizePrice
import com.stefanbratanov.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import java.net.URL
import java.time.LocalDate
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component

@Log
@Component("Kaufland")
class KauflandProductsExtractor(val objectMapper: ObjectMapper) : UrlProductsExtractor {

  override fun extract(url: URL): List<Product> {
    log.info("Processing Kaufland URL: {}", url.toString())

    val document = getHtmlDocument(url)

    val offersTemplateJson = getOffersTemplateJson(document) ?: return emptyList()

    val allProducts = mutableListOf<Product>()

    for (cycle in offersTemplateJson.path("props").path("offerData").path("cycles")) {
      for (category in cycle.path("categories")) {
        val categoryName = category.get("displayName").asText()
        val offers = category.path("offers")
        if (offers.isArray) {
          offers
            .filter { it.has("title") && it.has("formattedPrice") }
            .forEach { allProducts.add(extractProduct(categoryName, it)) }
        }
      }
    }

    return allProducts
  }

  private fun getOffersTemplateJson(document: Document): JsonNode? {
    val jsonRegex = """\{"component":.*$""".toRegex()
    return document
      .selectFirst("script:containsData(OfferTemplate)")
      ?.data()
      ?.let { jsonRegex.find(it) }
      ?.value
      ?.let { objectMapper.readTree(it) }
  }

  private fun extractProduct(categoryName: String, offer: JsonNode): Product {
    val title = offer.get("title").asText()
    val subtitle = offer.get("subtitle")?.asText()
    val name = subtitle?.let { "$title $it" } ?: title
    return Product(
      name = StringUtils.normalizeSpace(name),
      quantity = offer.get("unit")?.asText()?.let { StringUtils.normalizeSpace(it) },
      price = normalizePrice(offer.get("formattedPrice").asText()),
      oldPrice = normalizePrice(offer.get("formattedOldPrice")?.asText()),
      category = categoryName,
      picUrl = offer.get("listImage")?.asText(),
      validFrom = offer.get("dateFrom")?.asText()?.let { LocalDate.parse(it) },
      validUntil = offer.get("dateTo")?.asText()?.let { LocalDate.parse(it) },
    )
  }
}
