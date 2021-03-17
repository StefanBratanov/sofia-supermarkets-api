package com.stefata.sofiasupermarketsapi.common

fun normalizePrice(price: String?): Double? {
    return price?.replace("лв.*".toRegex(), "")?.replace(',', '.')?.trim()?.toDouble()
}