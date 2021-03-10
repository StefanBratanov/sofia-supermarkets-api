package com.stefata.sofiasupermarketsapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.stefata.sofiasupermarketsapi.model.Product
import com.stefata.sofiasupermarketsapi.model.Supermarket.BILLA
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

    @Test
    fun readsBilla() {
        val doc = Jsoup.connect("https://ssbbilla.site/weekly").get()

        val productsDivs = doc.select(".productSection > .product")

        val products = productsDivs.map {
            val productName = it.select(".actualProduct").text()
            val oldPrice = it.select(".price").first()?.text()
            val price = it.select(".price").last()?.text()
            Product(supermarket = BILLA, name = productName, price = price?.toDouble(), oldPrice = oldPrice?.toDouble())
        }.filter {
            nonNull(it.price)
        }.map {
            val productName = it.name
            val normalizedName = productName.replace("Най.*добра.*цена.*".toRegex(IGNORE_CASE), "")
                .replace("Изпечен.*всеки.*минути".toRegex(IGNORE_CASE), "")
                .replace("Виж още.*".toRegex(IGNORE_CASE), "")
            it.copy(name = normalizeSpace(normalizedName))
        }

        val json = ObjectMapper().writeValueAsString(products)

        Files.writeString(Paths.get("billa.json"), json, CREATE, TRUNCATE_EXISTING)

    }

}
