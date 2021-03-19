package com.stefata.sofiasupermarketsapi.flows

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.interfaces.SupermarketFlow
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Supermarket
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL

@Log
@Component
class BillaFlow(
    @Value("\${billa.url}") private val url: URL,
    @Qualifier("Billa") val urlProductsExtractor: UrlProductsExtractor
) : SupermarketFlow {

    override fun run() {
        val products = urlProductsExtractor.extract(url)

        log.info("Retrieved ${products.size} products")
    }

    override fun getSupermarket(): Supermarket {
        return Supermarket.BILLA
    }
}