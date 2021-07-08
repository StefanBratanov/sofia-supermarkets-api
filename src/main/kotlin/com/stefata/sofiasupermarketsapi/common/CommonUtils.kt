package com.stefata.sofiasupermarketsapi.common

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Objects.isNull
import kotlin.text.RegexOption.IGNORE_CASE

fun normalizePrice(price: String?): Double? {
    return price?.replace("лв.*".toRegex(), "")?.replace(',', '.')?.trim()?.toDouble()
}

fun getHtmlDocument(url: URL): Document {
    if (url.protocol == "file") {
        return Jsoup.parse(Paths.get(url.toURI()).toFile(), StandardCharsets.UTF_8.name())
    }
    return Jsoup.parse(url, 60000)
}

private val quantityRegex = "(\\d*(,|\\.|))?\\d+\\s*(кг|бр|л|мл|г|м|ml|g|kg|l)\\.?(?!оди)".toRegex(IGNORE_CASE)

fun separateNameAndQuantity(name: String): Pair<String?, String?> {
    val matchResult = quantityRegex.find(name)
    if (isNull(matchResult)) {
        return Pair(name, null)
    }
    val quantity = matchResult?.value
    val modifiedName = quantity?.let { name.replace(it, "") }
    return Pair(modifiedName?.replace("(за|-)\\s*\$".toRegex(IGNORE_CASE), ""), quantity)
}

fun checkIfUrlHasAcceptableHttpResponse(url: String): Boolean {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = "HEAD"
    return try {
        connection.responseCode != HttpURLConnection.HTTP_NOT_FOUND
    } catch (e: Exception) {
        false
    }
}

fun removeDuplicateSubstrings(input: String?): String? {
    return input?.split("\\s+".toRegex())?.distinct()?.joinToString(" ");
}