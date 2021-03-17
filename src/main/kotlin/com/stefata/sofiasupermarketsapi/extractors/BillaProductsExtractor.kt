package com.stefata.sofiasupermarketsapi.extractors

import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Product
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Component
import java.net.URL
import java.util.*

@Component("Billa")
class BillaProductsExtractor : UrlProductsExtractor {

    private val regexesToIgnoreBilla = listOf(
        "Най.*добра.*цена.*".toRegex(RegexOption.IGNORE_CASE),
        "Изпечен.*всеки.*минути".toRegex(RegexOption.IGNORE_CASE),
        "Виж още.*".toRegex(RegexOption.IGNORE_CASE)
    )

    override fun extract(url: URL): List<Product> {

        return getHtmlDoc(url).select(".productSection > .product").map {
            val productName = it.select(".actualProduct").text()
            val oldPrice = it.select(".price").first()?.text()
            val price = it.select(".price").last()?.text()

            Product(name = productName, price = price?.toDouble(), oldPrice = oldPrice?.toDouble())
        }.filter {
            Objects.nonNull(it.price)
        }.map {
            val normalizedName =
                regexesToIgnoreBilla.fold(it.name) { name, toRemove -> name.replace(toRemove, "") }
            it.copy(name = StringUtils.normalizeSpace(normalizedName))
        }
    }


}