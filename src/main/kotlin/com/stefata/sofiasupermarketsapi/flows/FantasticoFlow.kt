package com.stefata.sofiasupermarketsapi.flows

import com.stefata.sofiasupermarketsapi.brochure.FantasticoBrochureDownloader
import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.interfaces.PdfProductsExtractor
import com.stefata.sofiasupermarketsapi.interfaces.SupermarketFlow
import com.stefata.sofiasupermarketsapi.model.ProductStore
import com.stefata.sofiasupermarketsapi.model.Supermarket
import com.stefata.sofiasupermarketsapi.repository.ProductStoreRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.nio.file.Files

@Log
@Component
class FantasticoFlow(
    val fantasticoBrochureDownloader: FantasticoBrochureDownloader,
    @Qualifier("Fantastico") val pdfProductsExtractor: PdfProductsExtractor,
    val productStoreRepository: ProductStoreRepository
) : SupermarketFlow {

    override fun run() {
        val brochure = fantasticoBrochureDownloader.download()
        val products = pdfProductsExtractor.extract(brochure.first).map {
            it.copy(validUntil = brochure.second)
        }

        //clean-up
        Files.delete(brochure.first)

        log.info("Retrieved ${products.size} products")
        log.info("Saving ${getSupermarket().title} products")

        val toSave = ProductStore(supermarket = getSupermarket().title, products = products)
        productStoreRepository.saveIfProductsNotEmpty(toSave)
    }

    override fun getSupermarket(): Supermarket {
        return Supermarket.FANTASTICO
    }
}