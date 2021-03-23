package com.stefata.sofiasupermarketsapi.common

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

fun normalizePrice(price: String?): Double? {
    return price?.replace("лв.*".toRegex(), "")?.replace(',', '.')?.trim()?.toDouble()
}

fun getHtmlDocument(url: URL): Document {
    if (url.protocol == "file") {
        return Jsoup.parse(Paths.get(url.toURI()).toFile(), StandardCharsets.UTF_8.name())
    }
    return Jsoup.parse(url, 60000)
}

fun readResource(resource: String): String {
    val uri = object {}.javaClass.getResource(resource).toURI()
    return Files.readString(Paths.get(uri), StandardCharsets.UTF_8)
}