package com.stefata.sofiasupermarketsapi.links

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.concatenate
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

        log.info("Scraping {} for sublinks", baseUrl)

        val htmlDoc = getHtmlDocument(baseUrl)

        val otPonedelnik = htmlDoc.select("a[title='Разгледай всички предложения в тази категория']")
            .map {
                it.attr("href")
            }.map {
                URL(it)
            }

        val additional = htmlDoc.selectFirst("div[role=menu]")
            .select("a[role=menuitem]")
            //skip aktualni
            .drop(1)
            .filter {
                !it.text().contains("групи продукти")
            }.map {
                val href = it.attr("href")
                URL(baseUrl, href)
            }

        return concatenate(otPonedelnik, additional)

    }
}