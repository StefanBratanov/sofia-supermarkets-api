package com.stefanbratanov.sofiasupermarketsapi

import com.stefanbratanov.sofiasupermarketsapi.api.AlcoholController
import com.stefanbratanov.sofiasupermarketsapi.api.ProductCriteria
import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.scheduled.ScheduledFlowsRunner
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

@Log
@Component
class Main(
  val scheduledFlowsRunner: ScheduledFlowsRunner,
  val alcoholController: AlcoholController
) : CommandLineRunner {

  override fun run(vararg args: String?) {
    scheduledFlowsRunner.runFlows()
    log.info("Warming up the /products/alcohol endpoint")
    alcoholController.alcohol(ProductCriteria(null, true), null, true)
    log.info("Finished warming up the /products/alcohol endpoint")
  }
}
