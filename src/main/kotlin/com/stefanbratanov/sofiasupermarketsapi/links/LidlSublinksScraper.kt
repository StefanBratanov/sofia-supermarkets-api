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
class LidlSublinksScraper(@Value("\${lidl.base.url}") private val baseUrl: URL) : SublinksScraper {

  private val sublinksToAccept =
    listOf("niska-tsena-visoko-kachestvo".toRegex(), "lidl-plus".toRegex())

  override fun getSublinks(): List<URL> {
    log.info("Scraping {} for sublinks", baseUrl)

    return getHtmlDocument(baseUrl)
      .select("li.AHeroStageItems__Item > a")
      .mapNotNull {
        val href = it.attr("href")
        if (sublinksToAccept.none { rgx -> href.contains(rgx) }) {
          null
        } else {
          baseUrl.toURI().resolve(href).toURL()
        }
      }
      .distinct()
  }
}
