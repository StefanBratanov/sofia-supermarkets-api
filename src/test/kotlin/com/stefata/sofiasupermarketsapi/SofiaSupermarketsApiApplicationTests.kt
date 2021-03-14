package com.stefata.sofiasupermarketsapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.stefata.sofiasupermarketsapi.ml.KMeansWithInitialCenters
import com.stefata.sofiasupermarketsapi.model.Product
import com.stefata.sofiasupermarketsapi.ml.TextWithCoordinates
import org.apache.commons.lang3.StringUtils.normalizeSpace
import org.apache.commons.math3.ml.clustering.CentroidCluster
import org.apache.commons.math3.ml.clustering.Clusterable
import org.apache.commons.math3.ml.clustering.DBSCANClusterer
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

    private val regexesToRemoveFantastico = listOf(
        "www\\.fantastico\\.bg".toRegex(IGNORE_CASE),
        "ОФЕРТА ЗА ПЕРИОДА".toRegex(IGNORE_CASE),
        "fantastico\\.stores".toRegex(IGNORE_CASE),
        "Продуктите се продават в количества".toRegex(IGNORE_CASE),
        "си запазва правото на промяна".toRegex(IGNORE_CASE),
        "Промоцията е валидна".toRegex(IGNORE_CASE)
    )

    @Test
    fun readsFantastico() {
        val pdf = Paths.get("fantastiko.pdf")
//        FileUtils.copyURLToFile(
//            URL("https://broshura.bg/platform/download/1103-17032021"), pdf.toFile(),
//            60000, 60000
//        )

        val doc = PDDocument.load(pdf.toFile())

        val pdfTextStripper = PDFTextStripperWithCoordinates(regexesToRemoveFantastico)

        pdfTextStripper.startPage = 17
        pdfTextStripper.endPage = 17

        //don't need the output of this operation
        pdfTextStripper.getText(doc)

        pdfTextStripper.strippedTexts.forEach {
            println("${it.text} -> x=${it.x} y=${it.y} -> ${it.isTitle}")
        }

        val initialCenters = pdfTextStripper.strippedTexts.filter {
            it.text?.contains("лв|") == true
        }.map {
            CentroidCluster<TextWithCoordinates>(it)
        }

        val kMeansPlus = KMeansWithInitialCenters(initialCenters.size, 100, initialCenters)
        val clusteredTexts = kMeansPlus.cluster(pdfTextStripper.strippedTexts).map {
            it.points
        }

        val groupedText = clusteredTexts.joinToString(separator = System.lineSeparator()) { cluster ->
            cluster.joinToString(" | ") {
                it.text.toString()
            }
        }

        Files.writeString(Paths.get("fantastico.txt"), groupedText, CREATE, TRUNCATE_EXISTING)

        doc.close()
    }

}

class ClusterableTextPosition(val textPosition: TextPosition) : Clusterable {

    override fun getPoint(): DoubleArray {
        return doubleArrayOf(textPosition.x.toDouble())
    }

}

class PDFTextStripperWithCoordinates(val regexesToRemove: List<Regex>) : PDFTextStripper() {

    val strippedTexts: MutableList<TextWithCoordinates> = mutableListOf()
    val dbScanClusterer = DBSCANClusterer<ClusterableTextPosition>(10.0, 1)

    override fun startDocument(document: PDDocument?) {
        strippedTexts.clear()
    }

    override fun writeString(text: String?, textPositions: MutableList<TextPosition>?) {
        super.writeString(text, textPositions)
        val shouldRemove = regexesToRemove.any { rgx ->
            rgx.containsMatchIn(text.toString())
        }
        if (shouldRemove) {
            return;
        }
        val clusterableTextPositions = textPositions?.map {
            ClusterableTextPosition(it)
        }
        val clusters = dbScanClusterer.cluster(clusterableTextPositions)
        if (clusters.size > 1) {
            println("Separating $text because it is too far apart")
            clusters.map { cluster ->
                val clusterTp = cluster.points
                val (x, y) = getAverageXAndY(clusterTp.map { it.textPosition })
                val clusterText = clusterTp.joinToString("") {
                    it.textPosition.unicode
                }
                val toAdd = TextWithCoordinates(
                    text = clusterText,
                    x = (x!!).roundToLong().toDouble(), y = (y!!).roundToLong().toDouble(),
                    isTitle = isTitle(clusterTp.map { it.textPosition })
                )
                strippedTexts.add(toAdd)
            }
        } else {
            val (x, y) = getAverageXAndY(textPositions)
            val toAdd = TextWithCoordinates(
                text = text,
                x = (x!!).roundToLong().toDouble(), y = (y!!).roundToLong().toDouble(),
                isTitle = isTitle(textPositions)
            )
            strippedTexts.add(toAdd)
        }
    }

}

private fun isTitle(textPositions: List<TextPosition>?): Boolean {
    val isBold = textPositions?.any {
        it.font.name.contains("Bold".toRegex(IGNORE_CASE))
    }
    val isTitle = textPositions?.any {
        it.font.name.contains("myriad".toRegex(IGNORE_CASE))
    }
    return isBold == true && isTitle == true
}

private fun getAverageXAndY(textPositions: List<TextPosition>?): Pair<Double?, Double?> {
    val x = textPositions?.map {
        it.x
    }?.average()
    val y = textPositions?.map {
        it.y
    }?.average()
    return Pair(x, y)
}

private fun normalizePrice(price: String?): Double? {
    return price?.replace("лв.*".toRegex(), "")?.replace(',', '.')?.toDouble()
}
