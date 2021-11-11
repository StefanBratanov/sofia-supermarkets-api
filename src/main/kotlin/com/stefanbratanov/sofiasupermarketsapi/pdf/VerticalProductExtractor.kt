package com.stefanbratanov.sofiasupermarketsapi.pdf

import com.stefanbratanov.sofiasupermarketsapi.common.concatenate
import com.stefanbratanov.sofiasupermarketsapi.pdf.ProductSection.NAME
import com.stefanbratanov.sofiasupermarketsapi.pdf.ProductSection.QUANTITY
import kotlin.math.absoluteValue

fun getVerticalProducts(
    centers: List<TextWithCoordinates>,
    texts: List<Pair<ProductSection, TextWithCoordinates>>
): List<List<Pair<ProductSection, TextWithCoordinates>>> {

    return centers.map { center ->
        val titleTexts = texts.filter {
            it.first in listOf(NAME, QUANTITY)
        }.sortedByDescending { it.second.y }
        var correctTitleTexts: List<Pair<ProductSection, TextWithCoordinates>> = mutableListOf()
        var currentY: Double? = null
        for (text in titleTexts) {
            val textX = text.second.x
            val isValid = textX != null && center.x?.minus(textX)?.let { res ->
                res.absoluteValue <= 20
            } == true
            if (isValid) {
                if (currentY == null) {
                    currentY = text.second.y
                } else {
                    val stopLoop = text.second.y
                        ?.let { it.minus(currentY).absoluteValue > 50 } == true
                    if (stopLoop) {
                        break
                    }
                }
                correctTitleTexts = correctTitleTexts.plus(text)
            }
        }

        //sort again by y
        correctTitleTexts = correctTitleTexts.sortedBy { it.second.y }

        val otherTexts = texts.filter {
            it.first !in listOf(NAME, QUANTITY)
        }.filter {
            val textX = it.second.x
            val textY = it.second.y
            val xIsValid = textX != null && center.x?.minus(textX)?.let { res ->
                res.absoluteValue <= 40
            } == true
            val yIsValid = textY != null && center.y?.minus(textY)?.let { res ->
                res.absoluteValue <= 50
            } == true
            xIsValid && yIsValid
        }

        concatenate(correctTitleTexts, otherTexts)
    }
}