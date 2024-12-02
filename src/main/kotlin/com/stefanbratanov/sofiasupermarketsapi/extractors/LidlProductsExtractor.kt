package com.stefanbratanov.sofiasupermarketsapi.extractors

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import com.stefanbratanov.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import java.net.URL
import java.util.*
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Log
@Component("Lidl")
class LidlProductsExtractor(
  @Value("\${lidl.base.url}") private val baseUrl: URL,
  val lidlProductExtractor: LidlProductExtractor,
) : UrlProductsExtractor {

  @OptIn(DelicateCoroutinesApi::class)
  override fun extract(url: URL): List<Product> {
    log.info("Processing Lidl URL: {}", url)

    val document = getHtmlDocument(url)

    val category = document.title()

    val deferredProducts =
      document.select("div[data-selector=PRODUCT]").mapNotNull {
        val picUrl = it.attr("image").takeIf { url -> url.isNotEmpty() }
        it
          // retrieve product URL
          .attr("canonicalurl")
          .takeIf { canonicalUrl -> canonicalUrl.isNotEmpty() }
          ?.let { canonicalUrl -> baseUrl.toURI().resolve(canonicalUrl).toURL() }
          ?.let { productUrl ->
            GlobalScope.async {
              lidlProductExtractor.extract(productUrl).copy(category = category, picUrl = picUrl)
            }
          }
      }

    return runBlocking { deferredProducts.awaitAll() }
  }
}
