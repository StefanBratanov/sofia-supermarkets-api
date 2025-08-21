package com.stefanbratanov.sofiasupermarketsapi.scheduled

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.interfaces.SupermarketFlow
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket
import java.util.concurrent.atomic.AtomicBoolean
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Log
@Component
class ScheduledFlowsRunner(val flows: Map<Supermarket, SupermarketFlow>) {

  private val isRunning = AtomicBoolean(false)

  @Scheduled(cron = "\${flows.runner.cron}")
  fun runFlows() {
    if (!isRunning.compareAndSet(false, true)) {
      log.warn("Already running. Skipping this execution.")
      return
    }
    log.info("Scheduled to run flows for {}", flows.keys)
    flows.forEach { (_, flow) -> flow.runSafely() }
    isRunning.set(false)
  }
}
