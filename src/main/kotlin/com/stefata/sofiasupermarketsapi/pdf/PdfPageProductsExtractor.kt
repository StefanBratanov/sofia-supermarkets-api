package com.stefata.sofiasupermarketsapi.pdf

import com.stefata.sofiasupermarketsapi.ml.KMeansWithInitialCenters
import org.apache.commons.math3.ml.clustering.CentroidCluster
import org.apache.commons.math3.ml.distance.ManhattanDistance
import org.apache.pdfbox.pdmodel.PDDocument
import java.util.function.Predicate

class PdfPageProductsExtractor(
    private val pdfDoc: PDDocument,
    private val regexesToIgnore: List<Regex>,
    private val initialCenterPredicate: Predicate<TextWithCoordinates>,
    private val productSectionResolver: Map<ProductSection, (String) -> Boolean>
) {

    fun getProductTextsWithSections(page: Int): List<List<Pair<ProductSection, TextWithCoordinates>>> {
        val pdfTextStripper = PDFTextStripperWithCoordinates(regexesToIgnore)

        pdfTextStripper.startPage = page
        pdfTextStripper.endPage = page

        //don't need the output of this operation
        pdfTextStripper.getText(pdfDoc)

        val initialCenters = pdfTextStripper.strippedTexts.filter {
            initialCenterPredicate.test(it)
        }.map {
            CentroidCluster<TextWithCoordinates>(it)
        }

        val kMeansPlus = KMeansWithInitialCenters(
            initialCenters.size, 100, ManhattanDistance(), initialCenters
        )
        val clusteredTexts = kMeansPlus.cluster(pdfTextStripper.strippedTexts).map {
            it.points
        }

        return clusteredTexts.map { cluster ->
            cluster.map {
                val section = productSectionResolver.entries.first { (_, v) ->
                    v.invoke(it.text!!)
                }.key
                Pair(section, it)
            }
            //each product should have NAME and NEW_PRICE at least
        }.filter { cluster ->
            val nameCount = cluster.count {
                it.first == ProductSection.NAME
            }
            val newPricesCount = cluster.count {
                it.first == ProductSection.NEW_PRICE
            }
            nameCount >= 1 && newPricesCount == 1
        }
    }

}