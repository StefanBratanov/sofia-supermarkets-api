package com.stefata.sofiasupermarketsapi.links

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.interfaces.SublinksScraper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL
import kotlin.text.RegexOption.IGNORE_CASE

@Log
@Component
class BillaSublinksScraper(
    @Value("\${billa.url}") private val url: URL
) : SublinksScraper {

    override fun getSublinks(): List<URL> {

        log.info("Scraping {} for sublinks", url)

        return getHtmlDocument(url).select(".buttons div.button")
            .filter {
                val category = it.selectFirst("div.buttonText")?.text()
                category?.contains("billa".toRegex(IGNORE_CASE)) == false
            }
            .map {
                val href = it.selectFirst("a").attr("href")
                URL(href)
            }

    }
}