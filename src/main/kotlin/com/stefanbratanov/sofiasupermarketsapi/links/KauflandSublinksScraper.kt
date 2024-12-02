package com.stefanbratanov.sofiasupermarketsapi.links

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.concatenate
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import com.stefanbratanov.sofiasupermarketsapi.interfaces.SublinksScraper
import java.net.URL
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Log
@Component
class KauflandSublinksScraper(@Value("\${kaufland.base.url}") private val baseUrl: URL) :
  SublinksScraper {

  override fun getSublinks(): List<URL> {
    log.info("Scraping {} for sublinks", baseUrl)

    val htmlDoc = getHtmlDocument(baseUrl)

    val otPonedelnik =
      htmlDoc
        .select("a[title='Разгледай всички предложения в тази категория']")
        .map { it.attr("href") }
        .map { URL(it) }

    val additional =
      htmlDoc
        .selectFirst("div[role=menu]")
        ?.select("a[role=menuitem]")
        .orEmpty()
        // skip aktualni
        .drop(1)
        .filter { !it.text().contains("групи продукти") }
        .map {
          val href = it.attr("href")
          URL(baseUrl, href)
        }

    return concatenate(otPonedelnik, additional)
  }
}
