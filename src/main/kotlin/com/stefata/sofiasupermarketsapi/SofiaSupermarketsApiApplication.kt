package com.stefata.sofiasupermarketsapi

import com.stefata.sofiasupermarketsapi.scheduled.ScheduledFlowsRunner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.map.repository.config.EnableMapRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableMapRepositories
@EnableScheduling
@EnableCaching
class SofiaSupermarketsApiApplication

fun main(args: Array<String>) {
    runApplication<SofiaSupermarketsApiApplication>(*args)
}

@Component
class Main(
    val scheduledFlowsRunner: ScheduledFlowsRunner
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        scheduledFlowsRunner.runFlows()
    }

}
