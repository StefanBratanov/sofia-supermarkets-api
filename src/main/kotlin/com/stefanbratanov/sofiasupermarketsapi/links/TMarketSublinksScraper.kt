package com.stefanbratanov.sofiasupermarketsapi.links

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocumentHttpsTrustAll
import com.stefanbratanov.sofiasupermarketsapi.interfaces.SublinksScraper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL

@Log
@Component
class TMarketSublinksScraper(
    @Value("\${tmarket.base.url}") private val baseUrl: URL,
    private val tMarketPagesRetriever: TMarketPagesRetriever,
) : SublinksScraper {

    /**
     * Currently only НАПИТКИ because there is too much data otherwise and loads slowly
     */
    override fun getSublinks(): List<URL> {
        log.info("Scraping {} for sublinks", baseUrl)

        return getHtmlDocumentHttpsTrustAll(baseUrl).select("li._navigation-dropdown-list-item")
            .first {
                it.selectFirst("span._figure-stack-label")?.text()
                    ?.contains("НАПИТКИ") == true
            }
            .select("._navigation-dropdown-list-item > a")
            .filter {
                it.parent()?.classNames()?.contains("item-collapse") == false
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
