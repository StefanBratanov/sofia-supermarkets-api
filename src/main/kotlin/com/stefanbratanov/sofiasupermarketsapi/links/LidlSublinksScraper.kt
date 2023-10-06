package com.stefanbratanov.sofiasupermarketsapi.links

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import com.stefanbratanov.sofiasupermarketsapi.interfaces.SublinksScraper
import java.net.URL
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Log
@Component
class LidlSublinksScraper(
  @Value("\${lidl.base.url}") private val baseUrl: URL,
) : SublinksScraper {

  override fun getSublinks(): List<URL> {
    log.info("Scraping {} for sublinks", baseUrl)

    return getHtmlDocument(baseUrl)
      .select("li.AHeroStageItems__Item > a")
      .map {
        val href = it.attr("href")
        baseUrl.toURI().resolve(href).toURL()
      }
      .distinct()
  }
}
