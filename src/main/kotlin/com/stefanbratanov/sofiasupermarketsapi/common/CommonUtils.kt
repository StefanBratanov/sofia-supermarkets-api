package com.stefanbratanov.sofiasupermarketsapi.common

import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Objects.isNull
import kotlin.text.RegexOption.IGNORE_CASE
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus

val log: Logger = LoggerFactory.getLogger("CommonUtils")

fun normalizePrice(price: String?): Double? {
  return price?.replace("(лв|\\*).*".toRegex(), "")?.replace(',', '.')?.trim()?.toDoubleOrNull()
}

fun getHtmlDocument(url: URL): Document {
  if (url.protocol == "file") {
    return Jsoup.parse(Paths.get(url.toURI()).toFile(), StandardCharsets.UTF_8.name())
  }
  return Jsoup.parse(url, 60000)
}

private val quantityRegexes =
  listOf(
    "(\\d+\\s*\\+\\s*)?\\d+\\s*(бр(.)?)?\\s*[хx]\\s*[\\d,]+\\s*(кг|бр|л|мл|г|м|ml|g|kg|l)"
      .toRegex(IGNORE_CASE),
    "(\\d+(,|\\.|))?\\d+\\s*(кг|бр|л|мл|г|м|ml|g|kg|l)\\.?(?!оди)".toRegex(IGNORE_CASE),
  )

fun separateNameAndQuantity(name: String): Pair<String?, String?> {
  val matchResult = quantityRegexes.firstNotNullOfOrNull { it.find(name) }

  if (isNull(matchResult)) {
    return Pair(name, null)
  }
  val quantity = matchResult?.value
  val modifiedName = quantity?.let { name.replace(it, "") }
  return Pair(modifiedName?.replace("(за|-)\\s*\$".toRegex(IGNORE_CASE), ""), quantity)
}

fun checkIfUrlHasAcceptableHttpResponse(url: String?): Boolean {
  return try {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.requestMethod = "HEAD"
    connection.instanceFollowRedirects = false
    val responseCode = connection.responseCode
    if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP) {
      val httpStatus = HttpStatus.valueOf(responseCode)
      return !httpStatus.isError
    }
    return connection.getHeaderField("Location")?.let {
      !it.endsWith("suspendedpage.cgi") && checkIfUrlHasAcceptableHttpResponse(it)
    } == true
  } catch (e: Exception) {
    false
  }
}

fun removeDuplicateSubstrings(input: String?): String? {
  return input?.split("\\s+".toRegex())?.distinct()?.joinToString(" ")
}

fun <T> concatenate(vararg lists: List<T>): List<T> {
  return listOf(*lists).flatten()
}
