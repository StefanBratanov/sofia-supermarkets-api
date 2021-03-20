package com.stefata.sofiasupermarketsapi.scheduled

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.interfaces.SupermarketFlow
import com.stefata.sofiasupermarketsapi.model.Supermarket
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Log
@Component
class ScheduledFlowsRunner(
    val flows: Map<Supermarket, SupermarketFlow>
) {

    @Scheduled(cron = "\${flows.runner.cron}")
    fun runFlows() {
        log.info("Scheduled to run flows")
        flows.forEach { (_, flow) ->
            flow.runSafely()
        }
    }

}