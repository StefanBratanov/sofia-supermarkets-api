package com.stefata.sofiasupermarketsapi.ml

import org.apache.commons.math3.ml.clustering.Clusterable

data class TextWithCoordinates(val text: String?, val x: Double?, val y: Double?, val isBold: Boolean) : Clusterable {
    override fun getPoint(): DoubleArray {
        return doubleArrayOf(x!!, y!!)
    }
}