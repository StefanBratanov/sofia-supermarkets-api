package com.stefanbratanov.sofiasupermarketsapi.interfaces

import com.stefanbratanov.sofiasupermarketsapi.model.Product
import java.net.URL

interface UrlProductsExtractor {

  fun extract(url: URL): List<Product>
}
