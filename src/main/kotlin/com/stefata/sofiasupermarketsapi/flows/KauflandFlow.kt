package com.stefata.sofiasupermarketsapi.flows

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.interfaces.SupermarketFlow
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.links.KauflandSublinksScraper
import com.stefata.sofiasupermarketsapi.model.Supermarket
import com.stefata.sofiasupermarketsapi.model.SupermarketData
import com.stefata.sofiasupermarketsapi.repository.SupermarketDataRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Log
@Component
class KauflandFlow(
    val kauflandSublinksScraper: KauflandSublinksScraper,
    @Qualifier("Kaufland") val urlProductsExtractor: UrlProductsExtractor,
    val supermarketDataRepository: SupermarketDataRepository
) : SupermarketFlow {

    override fun run() {
        val products = kauflandSublinksScraper.getSublinks().flatMap {
            urlProductsExtractor.extract(it)
        }

        log.info("Retrieved ${products.size} products")
        log.info("Saving ${getSupermarket().title} products")

        val toSave = SupermarketData(supermarket = getSupermarket().title, products = products)
        supermarketDataRepository.saveIfProductsNotEmpty(toSave)

    }

    override fun getSupermarket(): Supermarket {
        return Supermarket.KAUFLAND
    }
}