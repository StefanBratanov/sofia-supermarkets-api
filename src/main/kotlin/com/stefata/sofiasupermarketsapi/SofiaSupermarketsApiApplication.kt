package com.stefata.sofiasupermarketsapi

import com.stefata.sofiasupermarketsapi.flows.BillaFlow
import com.stefata.sofiasupermarketsapi.flows.KauflandFlow
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.map.repository.config.EnableMapRepositories
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableMapRepositories
class SofiaSupermarketsApiApplication

fun main(args: Array<String>) {
    runApplication<SofiaSupermarketsApiApplication>(*args)
}

@Component
class Main(
    val kauflandFlow: KauflandFlow,
    val billaFlow: BillaFlow
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        kauflandFlow.runSafely()
        billaFlow.runSafely()
    }

}
