package com.stefanbratanov.sofiasupermarketsapi.common

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Objects.isNull
import kotlin.text.RegexOption.IGNORE_CASE
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.http.HttpStatus

fun normalizePrice(price: String?): Double? {
  return price
    ?.replace("(лв|\\*).*".toRegex(IGNORE_CASE), "")
    ?.replace(',', '.')
    ?.trim()
    ?.toDoubleOrNull()
}

fun getHtmlDocument(url: URL, maxBodySize: Int? = null): Document {
  if (url.protocol == "file") {
    return Jsoup.parse(Paths.get(url.toURI()).toFile(), StandardCharsets.UTF_8.name())
  }
  val connection = Jsoup.connect(url.toExternalForm()).timeout(6000)
  maxBodySize?.let { connection.maxBodySize(it) }
  return connection.get()
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
    val connection = URI(url).toURL().openConnection() as HttpURLConnection
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
