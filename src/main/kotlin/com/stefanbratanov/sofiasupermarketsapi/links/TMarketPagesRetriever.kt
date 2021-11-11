package com.stefanbratanov.sofiasupermarketsapi.links

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URL

@Log
@Component
class TMarketPagesRetriever {

    fun retrieveAllPages(url: URL): List<URL> {
        log.info("Retrieving all pages with offers for {}", url)
        return generateSequence(1, { it + 1 }).takeWhile {
            val pageUrl = getPageUrl(url, it)
            pageNumberIsValid(pageUrl)
        }.map {
            getPageUrl(url, it)
        }.toList()
    }

    private fun getPageUrl(baseUrl: URL, pageNumber: Int): URL {
        if (pageNumber == 1) {
            return baseUrl
        }
        return UriComponentsBuilder.fromUri(baseUrl.toURI())
            .queryParam("page", pageNumber)
            .build().toUri().toURL()
    }

    private fun pageNumberIsValid(url: URL): Boolean {
        return try {
            val document = getHtmlDocument(url)
            val firstProduct = document.selectFirst("._products-list > div[data-box=product]")
            val hasProducts = !document.select("div._notification > p")
                .text().contains("Няма продукти")
            hasProducts && firstProduct?.select("._product-price-compare")?.hasText() == true
        } catch (ex: Exception) {
            false
        }
    }
}