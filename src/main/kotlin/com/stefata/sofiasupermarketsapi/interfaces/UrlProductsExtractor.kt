package com.stefata.sofiasupermarketsapi.interfaces

import com.stefata.sofiasupermarketsapi.model.Product
import java.net.URL

interface UrlProductsExtractor {

    fun extract(url: URL): List<Product>

}