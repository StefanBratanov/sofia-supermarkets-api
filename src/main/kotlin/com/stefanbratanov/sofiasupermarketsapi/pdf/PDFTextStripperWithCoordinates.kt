package com.stefanbratanov.sofiasupermarketsapi.pdf

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import org.apache.commons.math3.ml.clustering.Clusterable
import org.apache.commons.math3.ml.clustering.DBSCANClusterer
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.TextPosition
import kotlin.math.roundToLong

@Log
class PDFTextStripperWithCoordinates(private val regexesToIgnore: List<Regex>) : PDFTextStripper() {

    val strippedTexts: MutableList<TextWithCoordinates> = mutableListOf()
    private val dbScanClusterer = DBSCANClusterer<ClusterableTextPosition>(15.0, 1)

    override fun startDocument(document: PDDocument?) {
        strippedTexts.clear()
    }

    override fun writeString(text: String?, textPositions: MutableList<TextPosition>?) {
        super.writeString(text, textPositions)
        val shouldIgnore = regexesToIgnore.any {
            it.containsMatchIn(text.toString())
        }
        if (shouldIgnore) {
            return
        }
        val clusterableTextPositions = textPositions?.map {
            ClusterableTextPosition(it)
        }
        val clusters = dbScanClusterer.cluster(clusterableTextPositions)
        if (clusters.size > 1) {
            log.info("Separating $text because it is too far apart")
            clusters.map { cluster ->
                val clusterTp = cluster.points
                val (x, y) = getAverageXAndY(clusterTp.map { it.textPosition })
                val clusterText = clusterTp.joinToString("") {
                    it.textPosition.unicode
                }
                val toAdd = TextWithCoordinates(
                    text = clusterText.trim(),
                    x = (x!!).roundToLong().toDouble(),
                    y = (y!!).roundToLong().toDouble(),
                    font = clusterTp.firstOrNull()?.textPosition?.font,
                )
                strippedTexts.add(toAdd)
            }
        } else {
            val (x, y) = getAverageXAndY(textPositions)
            val toAdd = TextWithCoordinates(
                text = text?.trim(),
                x = (x!!).roundToLong().toDouble(),
                y = (y!!).roundToLong().toDouble(),
                font = textPositions?.first()?.font,
            )
            strippedTexts.add(toAdd)
        }
    }

    private class ClusterableTextPosition(val textPosition: TextPosition) : Clusterable {
        override fun getPoint(): DoubleArray {
            return doubleArrayOf(textPosition.x.toDouble())
        }
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
}
