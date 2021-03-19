package com.stefata.sofiasupermarketsapi

import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.links.KauflandSublinksScraper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@SpringBootApplication
class SofiaSupermarketsApiApplication

fun main(args: Array<String>) {
    runApplication<SofiaSupermarketsApiApplication>(*args)
}

@Component
class Main(
    val kauflandSublinksScraper: KauflandSublinksScraper,
    @Qualifier("Kaufland") val urlProductsExtractor: UrlProductsExtractor
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        kauflandSublinksScraper.getSublinks().forEach {
            val products = urlProductsExtractor.extract(it)
            println(products)
        }
    }

}
