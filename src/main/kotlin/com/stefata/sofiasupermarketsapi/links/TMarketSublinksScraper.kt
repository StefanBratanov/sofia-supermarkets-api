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
class TMarketSublinksScraper(
    @Value("\${tmarket.base.url}") private val baseUrl: URL,
    private val tMarketPagesRetriever: TMarketPagesRetriever
) : SublinksScraper {

    /**
     * Currently only НАПИТКИ because there is too much data otherwise and loads slowly
     */
    override fun getSublinks(): List<URL> {
        log.info("Scraping {} for sublinks", baseUrl)

        return getHtmlDocument(baseUrl).select("li._navigation-dropdown-list-item").first {
            it.selectFirst("span._figure-stack-label")?.text()?.contains("НАПИТКИ") == true
        }
            .select("._navigation-dropdown-list-item > a")
            .filter {
                !it.parent().classNames().contains("item-collapse")
            }
            .map {
                it.attr("href")
            }.filter {
                !it.contains("javascript") && it.startsWith("http")
            }.flatMap {
                tMarketPagesRetriever.retrieveAllPages(URL(it))
            }
    }
}