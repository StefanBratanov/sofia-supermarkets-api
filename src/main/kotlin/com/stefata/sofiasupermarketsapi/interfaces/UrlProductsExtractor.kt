package com.stefata.sofiasupermarketsapi.interfaces

import com.stefata.sofiasupermarketsapi.model.Product
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

interface UrlProductsExtractor {

    fun extract(url: URL): List<Product>

    fun getHtmlDoc(url: URL): Document {
        if (url.protocol == "file") {
            return Jsoup.parse(Paths.get(url.toURI()).toFile(), StandardCharsets.UTF_8.name())
        }
        return Jsoup.parse(url, 60000)
    }

}