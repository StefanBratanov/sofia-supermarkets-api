package com.stefata.sofiasupermarketsapi.links

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.interfaces.SublinksScraper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL

@Log
@Component
class KauflandSublinksScraper(
    @Value("\${kaufland.base.url}") private val baseUrl: URL
) : SublinksScraper {

    override fun getSublinks(): List<URL> {

        log.info("Scraping {} for links", baseUrl)

        return getHtmlDocument(baseUrl).select("a[title='Разгледай всички предложения в тази категория']")
            .map {
                it.attr("href")
            }.map {
                URL(it)
            }

    }
}