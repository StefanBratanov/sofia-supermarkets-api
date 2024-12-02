package com.stefanbratanov.sofiasupermarketsapi.pdf

import org.apache.commons.math3.ml.clustering.Clusterable
import org.apache.pdfbox.pdmodel.font.PDFont

data class TextWithCoordinates(
  val text: String?,
  val x: Double?,
  val y: Double?,
  val font: PDFont?,
) : Clusterable {
  override fun getPoint(): DoubleArray {
    return doubleArrayOf(x!!, y!!)
  }
}
