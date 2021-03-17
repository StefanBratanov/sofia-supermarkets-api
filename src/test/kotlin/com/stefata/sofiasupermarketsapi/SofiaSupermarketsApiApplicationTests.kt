package com.stefata.sofiasupermarketsapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.stefata.sofiasupermarketsapi.extractors.*
import org.junit.jupiter.api.Test
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING

class SofiaSupermarketsApiApplicationTests {

    @Test
    fun readsBilla() {
        val billaProductsExtractor = BillaProductsExtractor()
        val products = billaProductsExtractor.extract(URL("https://ssbbilla.site/weekly"))
        val json = ObjectMapper().writeValueAsString(products)

        Files.writeString(Paths.get("billa.json"), json, CREATE, TRUNCATE_EXISTING)

    }

    @Test
    fun readsKaufland() {
        val kauflandProductsExtractor = KauflandProductsExtractor()
        val kauflandUrl =
            URL("https://www.kaufland.bg/aktualni-predlozheniya/ot-ponedelnik/obsht-pregled.category=08_%D0%90%D0%BB%D0%BA%D0%BE%D1%85%D0%BE%D0%BB%D0%BD%D0%B8_%D0%B8_%D0%B1%D0%B5%D0%B7%D0%B0%D0%BB%D0%BA%D0%BE%D1%85%D0%BE%D0%BB%D0%BD%D0%B8_%D0%BD%D0%B0%D0%BF%D0%B8%D1%82%D0%BA%D0%B8.html");
        val products = kauflandProductsExtractor.extract(kauflandUrl)
        val json = ObjectMapper().writeValueAsString(products)

        Files.writeString(Paths.get("kaufland.json"), json, CREATE, TRUNCATE_EXISTING)

    }

    @Test
    fun readsTMarket() {
        val tMarketProductsExtractor = TMarketProductsExtractor()
        val products = tMarketProductsExtractor.extract(URL("https://tmarketonline.bg/category/visokoalkoholni-napitki"))
        val json = ObjectMapper().writeValueAsString(products)

        Files.writeString(Paths.get("tmarket.json"), json, CREATE, TRUNCATE_EXISTING)
    }

    @Test
    fun readsFantastico() {
        val pdf = Paths.get("fantastiko.pdf")
//        FileUtils.copyURLToFile(
//            URL("https://broshura.bg/platform/download/1103-17032021"), pdf.toFile(),
//            60000, 60000
//        )
        val fantasticoProductsExtractor = FantasticoProductsExtractor()
        val products = fantasticoProductsExtractor.extract(pdf)
        val json = ObjectMapper().writeValueAsString(products)

        Files.writeString(Paths.get("fantastico.json"), json, CREATE, TRUNCATE_EXISTING)
    }


    @Test
    fun readsLidl() {
        val lidlProductsExtractor = LidlProductsExtractor()
        val url = URL("https://www.lidl.bg/bg/c/niska-cena-visoko-kachestvo/c1847/w1")
        val products = lidlProductsExtractor.extract(url)
        val json = ObjectMapper().writeValueAsString(products)

        Files.writeString(Paths.get("lidl.json"), json, CREATE, TRUNCATE_EXISTING)

    }

}

