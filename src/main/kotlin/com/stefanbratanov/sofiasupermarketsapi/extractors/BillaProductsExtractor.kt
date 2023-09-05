package com.stefanbratanov.sofiasupermarketsapi.extractors

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import com.stefanbratanov.sofiasupermarketsapi.common.separateNameAndQuantity
import com.stefanbratanov.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.text.RegexOption.IGNORE_CASE
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.apache.commons.lang3.math.NumberUtils
import org.springframework.stereotype.Component

@Log
@Component("Billa")
class BillaProductsExtractor : UrlProductsExtractor {

  private val regexesToIgnoreBilla =
    listOf(
      "Най.*добра.*цена.*".toRegex(IGNORE_CASE),
      "Изпечен.*всеки.*минути".toRegex(IGNORE_CASE),
      "Виж още.*".toRegex(IGNORE_CASE),
      "(Продукт)?(,|\\s)+означен.*със символа.*звезда".toRegex(IGNORE_CASE),
      "(Продукт)?(,|\\s)+маркиран.*с.*звезда".toRegex(IGNORE_CASE),
      "(\\*\\s*)?(\\*+)?цената не включва \\p{IsCyrillic}+алаж\\s*(\\.)?".toRegex(IGNORE_CASE),
      "(\\*\\s*)?(\\*+)?цената е без \\p{IsCyrillic}+алаж\\s*(\\.)?".toRegex(IGNORE_CASE),
      "(\\*\\s*)?(\\*+)?цена без \\p{IsCyrillic}+алаж\\s*(\\.)?".toRegex(IGNORE_CASE),
      "([;-]\\s*)?цена на \\p{IsCyrillic}+алаж\\s?(-)? .*\$".toRegex(IGNORE_CASE),
      "Произход\\s*-\\s*България".toRegex(IGNORE_CASE),
      "Сега в Billa".toRegex(IGNORE_CASE),
      "Евтино в Billa".toRegex(IGNORE_CASE),
      "до\\s+\\d+\\s+бр.+\\p{IsCyrillic}+ент(и)?".toRegex(IGNORE_CASE),
      "\\*+".toRegex(IGNORE_CASE),
      "Специфика\\s*:.*$".toRegex(IGNORE_CASE),
      "Супер цена".toRegex(IGNORE_CASE),
      "МУЛТИ ПАК \\d+\\+\\d+".toRegex(IGNORE_CASE),
      "Цена\\s+за\\s+\\d+\\s*бр\\.\\s*((без|с)\\s+отстъпка)?\\s*(\\d|\\.)+\\s*лв\\."
        .toRegex(IGNORE_CASE)
    )

  private val regexesToDeleteBilla =
    listOf("с (Billa|билла|била) (card|app|арр)".toRegex(IGNORE_CASE))

  override fun extract(url: URL): List<Product> {
    log.info("Processing Billa URL: {}", url.toString())

    val htmlDoc = getHtmlDocument(url)

    val dateRange =
      htmlDoc.selectFirst(".dateSpan")?.text()?.let {
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

    val priceClass = ".price"

    return htmlDoc
      .select(".productSection > .product")
      .asSequence()
      .filter {
        it.select(priceClass).first()?.text()?.let { pr -> NumberUtils.isCreatable(pr) } == true
      }
      .map {
        val productName = it.select(".actualProduct").text()
        val oldPrice = it.select(priceClass).first()?.text()
        val price = it.select(priceClass).last()?.text()

        Product(
          name = productName,
          price = price?.toDouble(),
          oldPrice = oldPrice?.toDouble(),
          validFrom = dateRange?.elementAtOrNull(0),
          validUntil = dateRange?.elementAtOrNull(1),
        )
      }
      .filter { Objects.nonNull(it.price) }
      .filter { regexesToDeleteBilla.none { rgx -> rgx.containsMatchIn(it.name) } }
      .map {
        val normalizedName =
          regexesToIgnoreBilla.fold(it.name) { name, toRemove -> name.replace(toRemove, "") }
        val nameAndQuantity = separateNameAndQuantity(normalizedName)
        it.copy(
          name = normalizeSpace(nameAndQuantity.first),
          quantity = normalizeSpace(nameAndQuantity.second),
        )
      }
      .toList()
  }
}
