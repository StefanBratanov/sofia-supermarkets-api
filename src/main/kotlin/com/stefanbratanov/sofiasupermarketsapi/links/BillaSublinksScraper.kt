package com.stefanbratanov.sofiasupermarketsapi.links

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import com.stefanbratanov.sofiasupermarketsapi.interfaces.SublinksScraper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL
import kotlin.text.RegexOption.IGNORE_CASE

@Log
@Component
class BillaSublinksScraper(
    @Value("\${billa.url}") private val url: URL,
) : SublinksScraper {

    override fun getSublinks(): List<URL> {
        log.info("Scraping {} for sublinks", url)

        return getHtmlDocument(url).select(".buttons div.button")
            .filter { it ->
                val category = it.selectFirst("div.buttonText")?.text()
                category?.contains("billa".toRegex(IGNORE_CASE)) == false &&
                    !category.contains("филиал".toRegex(IGNORE_CASE))
            }
            .map {
                val href = it.selectFirst("a")?.attr("href")
                URL(href)
            }
    }
}
