package com.stefata.sofiasupermarketsapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.stefata.sofiasupermarketsapi.SofiaSupermarketsApiApplicationTests.ProductSection
import com.stefata.sofiasupermarketsapi.SofiaSupermarketsApiApplicationTests.ProductSection.*
import com.stefata.sofiasupermarketsapi.ml.KMeansWithInitialCenters
import com.stefata.sofiasupermarketsapi.pdf.TextWithCoordinates
import com.stefata.sofiasupermarketsapi.model.Product
import com.stefata.sofiasupermarketsapi.pdf.PDFTextStripperWithCoordinates
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.apache.commons.math3.ml.clustering.CentroidCluster
import org.apache.commons.math3.ml.clustering.Clusterable
import org.apache.commons.math3.ml.clustering.DBSCANClusterer
import org.apache.commons.math3.ml.distance.ManhattanDistance
import org.apache.logging.log4j.util.Strings
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.util.Objects.nonNull
import kotlin.math.roundToLong
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

    private val regexesToIgnoreFantastico = listOf(
        "www\\.fantastico\\.bg".toRegex(IGNORE_CASE),
        "ОФЕРТА ЗА ПЕРИОДА".toRegex(IGNORE_CASE),
        "fantastico\\.stores".toRegex(IGNORE_CASE),
        "Продуктите се продават в количества".toRegex(IGNORE_CASE),
        "си запазва правото на промяна".toRegex(IGNORE_CASE),
        "(Промоцията|акцията) е валидна".toRegex(IGNORE_CASE),
        "за магазините.*цени".toRegex(IGNORE_CASE)
    )

    enum class ProductSection {
        NAME,
        QUANTITY,
        OLD_PRICE,
        NEW_PRICE,
        DISCOUNT,
        LV_BR
    }

    private val productSectionResolver: Map<ProductSection, (String) -> Boolean> = mapOf(
        OLD_PRICE to { text -> text.matches("\\d{1,2}\\.\\d{2}".toRegex()) },
        NEW_PRICE to { text -> text.matches("\\d{3,4}".toRegex()) },
        DISCOUNT to { text -> text.matches("-?\\d{1,2}%".toRegex()) },
        LV_BR to { text -> text.contains("лв|") },
        QUANTITY to { text -> text.contains("\\d+\\s*(мл|г|л|бр|см)".toRegex(IGNORE_CASE)) },
        NAME to { true }
    )

    @Test
    fun readsFantastico() {
        val pdf = Paths.get("fantastiko.pdf")
//        FileUtils.copyURLToFile(
//            URL("https://broshura.bg/platform/download/1103-17032021"), pdf.toFile(),
//            60000, 60000
//        )

        val doc = PDDocument.load(pdf.toFile())

        val pdfTextStripper = PDFTextStripperWithCoordinates(regexesToIgnoreFantastico)

        pdfTextStripper.startPage = 17
        pdfTextStripper.endPage = 17

        //don't need the output of this operation
        pdfTextStripper.getText(doc)

        val initialCenters = pdfTextStripper.strippedTexts.filter {
            it.text?.contains("лв|") == true
        }.map {
            CentroidCluster<TextWithCoordinates>(it)
        }

        val kMeansPlus = KMeansWithInitialCenters(
            initialCenters.size, 100, ManhattanDistance(),
            initialCenters
        )
        val clusteredTexts = kMeansPlus.cluster(pdfTextStripper.strippedTexts).map {
            it.points
        }

        val clusteredTextsWithSection = clusteredTexts.map { cluster ->
            cluster.map {
                val section = productSectionResolver.entries.first { (_, v) ->
                    v.invoke(it.text!!)
                }.key
                Pair(section, it)
            }
        }.filter { cluster ->
            val nameCount = cluster.count {
                it.first == NAME
            }
            val newPricesCount = cluster.count {
                it.first == NEW_PRICE
            }
            nameCount >= 1 && newPricesCount == 1
        }

        val products = clusteredTextsWithSection.mapNotNull {
            val name = getName(it)
            val oldPrice = it.firstOrNull { sectionAndText ->
                sectionAndText.first == OLD_PRICE
            }?.second?.text
            val newPrice = it.first { sectionAndText ->
                sectionAndText.first == NEW_PRICE
            }.second.text?.replace("^0".toRegex(), "")
            val quantity = it.filter { sectionAndText ->
                sectionAndText.first == QUANTITY
            }.joinToString(separator = " ") { sectionAndText ->
                sectionAndText.second.text.toString()
            }.takeUnless { text ->
                Strings.isBlank(text)
            }

            Product(
                name = normalizeSpace(name),
                price = normalizePrice(newPrice)?.div(100),
                oldPrice = normalizePrice(oldPrice),
                quantity = normalizeSpace(quantity)
            ).takeIf {
                isNotBlank(name)
            }
        }

        val json = ObjectMapper().writeValueAsString(products)

        Files.writeString(Paths.get("fantastico.json"), json, CREATE, TRUNCATE_EXISTING)

        doc.close()
    }

}

private fun getName(cluster: List<Pair<ProductSection, TextWithCoordinates>>): String {
    return cluster.filter {
        it.first == NAME
    }.filter {
        it.second.font?.name?.contains("Officina") == false
    }.joinToString(" ") {
        it.second.text.toString()
    }
}

private fun normalizePrice(price: String?): Double? {
    return price?.replace("лв.*".toRegex(), "")?.replace(',', '.')?.toDouble()
}
