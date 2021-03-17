package com.stefata.sofiasupermarketsapi.interfaces

import com.stefata.sofiasupermarketsapi.model.Product
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URL

interface UrlProductsExtractor {

    fun extract(url: URL): List<Product>

    fun getHtmlDoc(url: URL) : Document {
        return Jsoup.parse(url,60000)
    }

}