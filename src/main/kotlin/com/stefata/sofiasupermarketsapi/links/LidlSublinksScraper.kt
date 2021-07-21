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
class LidlSublinksScraper(
    @Value("\${lidl.base.url}") private val baseUrl: URL
) : SublinksScraper {

    override fun getSublinks(): List<URL> {
        log.info("Scraping {} for sublinks", baseUrl)

        return getHtmlDocument(baseUrl).select("div[data-Creative=Main Navigation] > a.navigation__link")
            .map {
                val href = it.attr("href")
                val sublink = baseUrl.toURI().resolve(href).toURL()
                getHtmlDocument(sublink)
            }.flatMap {
                it.select("li.navigation__item > a[data-controller=navigation/main/featured]")
                    .select("a[data-controller=navigation/main/featured] > span:contains(актуални)")
                    .flatMap { elem ->
                        val liNavigationItem = elem.parent().parent()
                        liNavigationItem.select("a[data-controller=navigation/link/burgernavigation]")
                    }.filter { elem ->
                        elem.text()?.contains("lidl plus".toRegex(RegexOption.IGNORE_CASE)) == false
                    }.map { elem ->
                        elem.attr("href")
                    }
            }.distinct().map {
                baseUrl.toURI().resolve(it).toURL()
            }
    }
}