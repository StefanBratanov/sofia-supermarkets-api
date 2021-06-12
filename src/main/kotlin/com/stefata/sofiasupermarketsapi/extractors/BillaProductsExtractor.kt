package com.stefata.sofiasupermarketsapi.extractors

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.common.separateNameAndQuantity
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Product
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.springframework.stereotype.Component
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.text.RegexOption.IGNORE_CASE

@Log
@Component("Billa")
class BillaProductsExtractor : UrlProductsExtractor {

    private val regexesToIgnoreBilla = listOf(
        "Най.*добра.*цена.*".toRegex(IGNORE_CASE),
        "Изпечен.*всеки.*минути".toRegex(IGNORE_CASE),
        "Виж още.*".toRegex(IGNORE_CASE),
        "Продукт означен.*със символа.*звезда".toRegex(IGNORE_CASE),
        "(\\*\\s*)?(\\*+)?цената не включва амбалаж".toRegex(IGNORE_CASE),
        "(\\*\\s*)?(\\**)?цената е без амбалаж".toRegex(IGNORE_CASE),
        "([;-]\\s*)?цена на амбалаж\\s?(-)? .*\$".toRegex(IGNORE_CASE),
        "Произход\\s*-\\s*България".toRegex(IGNORE_CASE),
        "Сега в Billa".toRegex(IGNORE_CASE),
        "\\*+".toRegex(IGNORE_CASE)
    )

    override fun extract(url: URL): List<Product> {

        log.info("Processing Billa URL: {}", url.toString())

        val htmlDoc = getHtmlDocument(url)

        val endDate = htmlDoc.selectFirst(".dateSpan")?.text()?.let {
            "\\d+.\\d+.\\d+".toRegex().findAll(it).lastOrNull()
        }?.let {
            val match = it.groupValues[0]
            try {
                LocalDate.parse(match, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            } catch (ex: Exception) {
                log.error("Error while parsing $match", ex)
                null
            }
        }

        return htmlDoc.select(".productSection > .product").map {
            val productName = it.select(".actualProduct").text()
            val oldPrice = it.select(".price").first()?.text()
            val price = it.select(".price").last()?.text()

            Product(name = productName, price = price?.toDouble(), oldPrice = oldPrice?.toDouble(), validUntil = endDate)
        }.filter {
            Objects.nonNull(it.price)
        }.map {
            val normalizedName =
                regexesToIgnoreBilla.fold(it.name) { name, toRemove -> name.replace(toRemove, "") }
            val nameAndQuantity = separateNameAndQuantity(normalizedName)
            it.copy(name = normalizeSpace(nameAndQuantity.first), quantity = normalizeSpace(nameAndQuantity.second))
        }
    }


}