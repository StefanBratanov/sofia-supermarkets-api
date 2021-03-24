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
import java.util.*
import kotlin.text.RegexOption.IGNORE_CASE

@Log
@Component("Billa")
class BillaProductsExtractor : UrlProductsExtractor {

    private val regexesToIgnoreBilla = listOf(
        "Най.*добра.*цена.*".toRegex(IGNORE_CASE),
        "Изпечен.*всеки.*минути".toRegex(IGNORE_CASE),
        "Виж още.*".toRegex(IGNORE_CASE),
        "Продукт означен.*със символа.*звезда".toRegex(IGNORE_CASE)
    )

    override fun extract(url: URL): List<Product> {

        log.info("Processing Billa URL: {}", url.toString())

        return getHtmlDocument(url).select(".productSection > .product").map {
            val productName = it.select(".actualProduct").text()
            val oldPrice = it.select(".price").first()?.text()
            val price = it.select(".price").last()?.text()

            Product(name = productName, price = price?.toDouble(), oldPrice = oldPrice?.toDouble())
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