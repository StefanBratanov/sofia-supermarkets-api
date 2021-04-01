package com.stefata.sofiasupermarketsapi.scheduled

import com.stefata.sofiasupermarketsapi.api.AlcoholController
import com.stefata.sofiasupermarketsapi.api.ProductCriteria
import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller

@Log
@Controller
class ScheduledAlcoholRetriever(
    val alcoholController: AlcoholController
) {

    @Scheduled(cron = "\${alcohol.retriever.cron}")
    fun retrieveAlcohol() {
        log.info("Scheduled to retrieve alcohol products")
        val productCriteria = ProductCriteria(null, false)
        alcoholController.alcohol(productCriteria, null)
    }
}