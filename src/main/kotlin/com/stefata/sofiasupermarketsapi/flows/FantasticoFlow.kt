package com.stefata.sofiasupermarketsapi.flows

import com.stefata.sofiasupermarketsapi.brochure.FantasticoBrochureDownloader
import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.interfaces.PdfProductsExtractor
import com.stefata.sofiasupermarketsapi.interfaces.SupermarketFlow
import com.stefata.sofiasupermarketsapi.model.Supermarket
import com.stefata.sofiasupermarketsapi.model.SupermarketStore
import com.stefata.sofiasupermarketsapi.repository.SupermarketStoreRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.nio.file.Files

@Log
@Component
class FantasticoFlow(
    val fantasticoBrochureDownloader: FantasticoBrochureDownloader,
    @Qualifier("Fantastico") val pdfProductsExtractor: PdfProductsExtractor,
    val supermarketStoreRepository: SupermarketStoreRepository
) : SupermarketFlow {

    override fun run() {
        val brochure = fantasticoBrochureDownloader.download()
        val products = pdfProductsExtractor.extract(brochure)

        //clean-up
        Files.delete(brochure)

        log.info("Retrieved ${products.size} products")
        log.info("Saving ${getSupermarket().title} products")

        val toSave = SupermarketStore(supermarket = getSupermarket().title, products = products)
        supermarketStoreRepository.saveIfProductsNotEmpty(toSave)
    }

    override fun getSupermarket(): Supermarket {
        return Supermarket.FANTASTICO
    }
}