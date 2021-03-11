package com.stefata.sofiasupermarketsapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.stefata.sofiasupermarketsapi.model.Product
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.util.Objects.nonNull
import kotlin.text.RegexOption.IGNORE_CASE

class SofiaSupermarketsApiApplicationTests {

    private val regexesToRemove = listOf(
        "Най.*добра.*цена.*".toRegex(IGNORE_CASE),
        "Изпечен.*всеки.*минути".toRegex(IGNORE_CASE),
        "Виж още.*".toRegex(IGNORE_CASE)
    )

    @Test
    fun readsBilla() {
        val doc = Jsoup.connect("https://ssbbilla.site/weekly").get()

        val products = doc.select(".productSection > .product").map {
            val productName = it.select(".actualProduct").text()
            val oldPrice = it.select(".price").first()?.text()
            val price = it.select(".price").last()?.text()

            Product(name = productName, price = price?.toDouble(), oldPrice = oldPrice?.toDouble())
        }.filter {
            nonNull(it.price)
        }.map {
            val normalizedName =
                regexesToRemove.fold(it.name) { name, toRemove -> name.replace(toRemove, "") }
            it.copy(name = normalizeSpace(normalizedName))
        }

        val json = ObjectMapper().writeValueAsString(products)

        Files.writeString(Paths.get("billa.json"), json, CREATE, TRUNCATE_EXISTING)

    }

    @Test
    fun readsKaufland() {
        val doc =
            Jsoup.connect("https://www.kaufland.bg/aktualni-predlozheniya/ot-ponedelnik/obsht-pregled.category=08_%D0%90%D0%BB%D0%BA%D0%BE%D1%85%D0%BE%D0%BB%D0%BD%D0%B8_%D0%B8_%D0%B1%D0%B5%D0%B7%D0%B0%D0%BB%D0%BA%D0%BE%D1%85%D0%BE%D0%BB%D0%BD%D0%B8_%D0%BD%D0%B0%D0%BF%D0%B8%D1%82%D0%BA%D0%B8.html")
                .get()

        val products = doc.select(".o-overview-list__list-item").map {
            val subtitle = it.select(".m-offer-tile__subtitle").text()
            val title = it.select(".m-offer-tile__title").text()
            val quantity = it.select(".m-offer-tile__quantity").text()

            val oldPrice = it.select(".a-pricetag__old-price")?.text()
            val price = it.select(".a-pricetag__price")?.text()

            Product(
                name = normalizeSpace("$subtitle $title"),
                quantity = normalizeSpace(quantity),
                price = normalizePrice(price),
                oldPrice = normalizePrice(oldPrice)
            )
        }

        val json = ObjectMapper().writeValueAsString(products)

        Files.writeString(Paths.get("kaufland.json"), json, CREATE, TRUNCATE_EXISTING)

    }

    @Test
    fun readsTMarket() {
        val doc = Jsoup.connect("https://tmarketonline.bg/category/visokoalkoholni-napitki").get()

        val products = doc.select("._products-list").select("div[data-box=product]").map {
            val productName = it.select("._product-name").text()

            var price = it.select("._product-price-compare")
            var oldPrice = it.select("._product-price-old")

            if (!price.hasText()) {
                val onlyPrice = it.select(".price")
                price = onlyPrice
                oldPrice = onlyPrice
            }

            Product(
                name = productName,
                price = normalizePrice(price.text()),
                oldPrice = normalizePrice(oldPrice.text())
            )
        }

        val json = ObjectMapper().writeValueAsString(products)

        Files.writeString(Paths.get("tmarket.json"), json, CREATE, TRUNCATE_EXISTING)
    }

    private fun normalizePrice(price: String?): Double? {
        return price?.replace("лв.*".toRegex(), "")?.replace(',', '.')?.toDouble()
    }

}
