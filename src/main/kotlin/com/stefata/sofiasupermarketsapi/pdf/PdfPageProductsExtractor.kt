package com.stefata.sofiasupermarketsapi.pdf

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.concatenate
import com.stefata.sofiasupermarketsapi.ml.KMeansWithInitialCenters
import org.apache.commons.math3.ml.clustering.CentroidCluster
import org.apache.commons.math3.ml.distance.ManhattanDistance
import org.apache.pdfbox.pdmodel.PDDocument
import java.util.function.Predicate
import kotlin.math.absoluteValue

@Log
class PdfPageProductsExtractor(
    private val pdfDoc: PDDocument,
    private val regexesToIgnore: List<Regex>,
    private val fontsToKeep: List<Regex>,
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

        if (initialCenters.isEmpty()) {
            return emptyList()
        }

        val filteredStrippedTexts = pdfTextStripper.strippedTexts.filter {
            fontsToKeep.any { rgx ->
                it.font?.name?.contains(rgx) == true
            }
        }.toMutableList()

        val kMeansPlus = KMeansWithInitialCenters(
            initialCenters.size, 100, ManhattanDistance(), initialCenters
        )
        val clusteredTexts = kMeansPlus.cluster(filteredStrippedTexts).map {
            it.points
        }

        val clusteredTextsWithSections = clusteredTexts.map { cluster ->
            cluster.map {
                addProductSection(it)
            }
        }

        //each product should have NAME and NEW_PRICE at least
        val validClusteredTexts = clusteredTextsWithSections.filter { cluster ->
            hasNameAndNewPrice(cluster)
        }

        //centers which were not matched
        val noMatchedCenters = initialCenters.filter {
            val centerText = it.center as TextWithCoordinates
            validClusteredTexts.flatten().none { pair ->
                pair.second == centerText
            }
        }

        //START -> logic for vertical products case
        val horizontalCentersEntry = noMatchedCenters.groupBy {
            it.center.point[1]
        }.entries.maxByOrNull { it.value.size }

        val horizontalCenters = horizontalCentersEntry?.value?.map {
            it.center as TextWithCoordinates
        }.orEmpty().toMutableList()

        val additionalCenters = noMatchedCenters.map {
            it.center as TextWithCoordinates
        }.filter {
            it !in horizontalCenters
        }.filter {
            horizontalCentersEntry != null && it.y != null &&
                    it.y.minus(horizontalCentersEntry.key).absoluteValue < 30
        }

        horizontalCenters.addAll(additionalCenters)

        if (horizontalCenters.size >= 3) {
            log.info(
                "There seems to be {} products which need vertical extracting",
                horizontalCenters.size
            )
            val verticalProducts = getVerticalProducts(
                horizontalCenters,
                clusteredTextsWithSections.flatten()
            )
            val flattenedTexts = verticalProducts.flatten()
            val filteredValid = validClusteredTexts.map {
                it.filter { pair ->
                    pair !in flattenedTexts
                }
            }
            val augmentedClusteredTexts = concatenate(filteredValid, verticalProducts)

            val stillNotMatchedCenters = noMatchedCenters.filter {
                it.center as TextWithCoordinates !in horizontalCenters
            }

            if (stillNotMatchedCenters.isNotEmpty()) {
                log.info(
                    "There are still {} products which haven't been extracted. Will try.",
                    stillNotMatchedCenters.size
                )
                val allTextsSoFar = augmentedClusteredTexts.flatten().map {
                    it.second
                }
                val leftTexts = filteredStrippedTexts.filter {
                    it !in allTextsSoFar
                }.toMutableList()
                val kMeansPlus2 = KMeansWithInitialCenters(
                    stillNotMatchedCenters.size, 100, ManhattanDistance(), stillNotMatchedCenters
                )
                val moreClusteredTexts = kMeansPlus2.cluster(leftTexts).map {
                    it.points
                }.map { cluster ->
                    cluster.map {
                        addProductSection(it)
                    }
                }.filter { cluster ->
                    hasNameAndNewPrice(cluster)
                }

                return concatenate(augmentedClusteredTexts, moreClusteredTexts)
            }

            return augmentedClusteredTexts
        }
        //END -> logic for vertical products case

        return validClusteredTexts
    }

    private fun hasNameAndNewPrice(cluster: List<Pair<ProductSection, TextWithCoordinates>>): Boolean {
        val nameCount = cluster.count {
            it.first == ProductSection.NAME
        }
        val newPricesCount = cluster.count {
            it.first == ProductSection.NEW_PRICE
        }
        return nameCount >= 1 && newPricesCount == 1
    }

    private fun addProductSection(textWithCoordinates: TextWithCoordinates): Pair<ProductSection, TextWithCoordinates> {
        val section = productSectionResolver.entries.first { (_, v) ->
            v.invoke(textWithCoordinates.text!!)
        }.key
        return Pair(section, textWithCoordinates)
    }

}