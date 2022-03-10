package com.stefanbratanov.sofiasupermarketsapi.pdf

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.concatenate
import com.stefanbratanov.sofiasupermarketsapi.ml.KMeansWithInitialCenters
import com.stefanbratanov.sofiasupermarketsapi.pdf.ProductSection.*
import org.apache.commons.math3.ml.clustering.CentroidCluster
import org.apache.commons.math3.ml.distance.ManhattanDistance
import org.apache.pdfbox.pdmodel.PDDocument
import java.util.function.Predicate
import kotlin.math.abs
import kotlin.math.absoluteValue

@Log
class PdfPageProductsExtractor(
    private val pdfDoc: PDDocument,
    private val regexesToIgnore: List<Regex>,
    private val fontsToKeep: List<Regex>,
    private val initialCenterPredicate: Predicate<TextWithCoordinates>,
    private val productSectionResolver: Map<ProductSection, (TextWithCoordinates) -> Boolean>
) {

    private val newPricesProductSections = listOf(NEW_PRICE, NEW_PRICE_LEGACY)

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
        }.filter {
            filterUnknownProductSection(it)
        }.toMutableList()

        val kMeansPlus = KMeansWithInitialCenters(
            initialCenters.size, 100, ManhattanDistance(), initialCenters
        )
        val clusteredTexts = kMeansPlus.cluster(filteredStrippedTexts).map {
            it.points
        }

        val clusteredTextsWithSections = clusteredTexts.map { cluster ->
            val withSections = cluster.map {
                addProductSection(it)
            }
            val newPrices = withSections.filter {
                it.first == NEW_PRICE
            }.sortedBy {
                it.second.x
            }
            if (newPrices.size > 1) {
                val newPriceAndMaybeOldPriceToReplace =
                    extractNewPriceAndMaybeOldPriceToReplace(newPrices, withSections)
                val newPrice = newPriceAndMaybeOldPriceToReplace.first
                val toCopyFrom = newPrices[0].second
                val newPairNewPrice = Pair(NEW_PRICE, toCopyFrom.copy(text = newPrice))
                //remove previous NEW_PRICE_NEW and replace with a complete one
                val sectionsWithOneNewPrice = withSections.filter { twc ->
                    twc.first != NEW_PRICE
                }.toMutableList().plus(newPairNewPrice)
                //replacing OLD_PRICE if required
                val maybeOldPriceToReplace = newPriceAndMaybeOldPriceToReplace.second
                if (maybeOldPriceToReplace == null) {
                    sectionsWithOneNewPrice
                } else {
                    sectionsWithOneNewPrice.filter { twc ->
                        twc.first != OLD_PRICE
                    }.plus(Pair(OLD_PRICE, maybeOldPriceToReplace))
                }
            } else {
                withSections
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
            //check if name and name is not a digit
            it.first == NAME && it.second.text?.matches("-?\\d+(\\.\\d+)?".toRegex()) == false
        }
        val newPricesCount = cluster.count {
            it.first in newPricesProductSections
        }
        return nameCount >= 1 && newPricesCount == 1
    }

    private fun addProductSection(textWithCoordinates: TextWithCoordinates): Pair<ProductSection, TextWithCoordinates> {
        val section = productSectionResolver.entries.first { (_, v) ->
            v.invoke(textWithCoordinates)
        }.key
        return Pair(section, textWithCoordinates)
    }

    private fun filterUnknownProductSection(textWithCoordinates: TextWithCoordinates): Boolean {
        val section = productSectionResolver.entries.first { (_, v) ->
            v.invoke(textWithCoordinates)
        }.key
        return section != UNKNOWN
    }

    private fun extractNewPriceAndMaybeOldPriceToReplace(
        newPrices: List<Pair<ProductSection, TextWithCoordinates>>,
        clusterWithSections: List<Pair<ProductSection, TextWithCoordinates>>
    ): Pair<String, TextWithCoordinates?> {
        val sizeOfNewPrices = newPrices.size
        if (sizeOfNewPrices == 2) {
            return Pair(newPrices.joinToString(separator = "") {
                it.second.text.orEmpty().trim()
            }, null)
        }
        if (sizeOfNewPrices % 2 == 0) {
            //first name coordinates
            val nameCoordinates = getNameCoordinates(clusterWithSections)
            val closestOldPrice = findClosestSections(OLD_PRICE, nameCoordinates, clusterWithSections).firstOrNull()
            return Pair(
                findClosestSections(
                    NEW_PRICE,
                    closestOldPrice,
                    clusterWithSections,
                    2
                ).joinToString(separator = "") {
                    it.text.orEmpty().trim()
                }, closestOldPrice
            )
        }
        return Pair(combineTwoCloseToEachOtherPrices(newPrices), null)
    }

    private fun getNameCoordinates(clusterWithSections: List<Pair<ProductSection, TextWithCoordinates>>): TextWithCoordinates? {
        return clusterWithSections.firstOrNull {
            it.first == NAME
        }?.second
    }

    private fun combineTwoCloseToEachOtherPrices(prices: List<Pair<ProductSection, TextWithCoordinates>>): String {
        return prices
            .sortedWith(compareBy({ it.second.x }, { it.second.y })).take(2)
            .joinToString(separator = "") {
                it.second.text.orEmpty().trim()
            }
    }

    private fun findClosestSections(
        sectionToFind: ProductSection,
        coordinates: TextWithCoordinates?,
        clusterWithSections: List<Pair<ProductSection, TextWithCoordinates>>,
        toTake: Int = 1
    ): List<TextWithCoordinates> {
        return clusterWithSections.filter {
            it.first == sectionToFind
        }.sortedBy {
            abs(it.second.x.minus(coordinates?.x)) + abs(it.second.y.minus(coordinates?.y))
        }.map { it.second }.take(toTake)
    }

    operator fun Double?.minus(other: Double?): Double =
        if (this != null && other != null) this - other else 3000.0

    operator fun Double?.plus(other: Double?): Double =
        if (this != null && other != null) this + other else 3000.0
}