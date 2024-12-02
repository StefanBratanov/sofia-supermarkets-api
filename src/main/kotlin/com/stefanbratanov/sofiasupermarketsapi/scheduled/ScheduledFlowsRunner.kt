package com.stefanbratanov.sofiasupermarketsapi.scheduled

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.interfaces.SupermarketFlow
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Log
@Component
class ScheduledFlowsRunner(val flows: Map<Supermarket, SupermarketFlow>) {

  @Scheduled(cron = "\${flows.runner.cron}")
  fun runFlows() {
    log.info("Scheduled to run flows for {}", flows.keys)
    flows.forEach { (_, flow) -> flow.runSafely() }
  }
}
