package com.stefanbratanov.sofiasupermarketsapi.scheduled

import com.ninjasquad.springmockk.MockkBean
import com.stefanbratanov.sofiasupermarketsapi.flows.BillaFlow
import com.stefanbratanov.sofiasupermarketsapi.flows.FantasticoFlow
import com.stefanbratanov.sofiasupermarketsapi.flows.FlowsConfig
import com.stefanbratanov.sofiasupermarketsapi.flows.KauflandFlow
import com.stefanbratanov.sofiasupermarketsapi.flows.LidlFlow
import com.stefanbratanov.sofiasupermarketsapi.flows.TMarketFlow
import io.mockk.justRun
import io.mockk.verifyAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [FlowsConfig::class, ScheduledFlowsRunner::class])
internal class ScheduledFlowsRunnerTest {

  @MockkBean lateinit var billaFlow: BillaFlow

  @MockkBean lateinit var kauflandFlow: KauflandFlow

  @MockkBean lateinit var lidlFlow: LidlFlow

  @MockkBean lateinit var fantasticoFlow: FantasticoFlow

  @MockkBean lateinit var tMarketFlow: TMarketFlow

  @Autowired lateinit var underTest: ScheduledFlowsRunner

  @Test
  fun `test running flows`() {
    justRun { billaFlow.runSafely() }
    justRun { kauflandFlow.runSafely() }
    justRun { lidlFlow.runSafely() }
    justRun { fantasticoFlow.runSafely() }
    justRun { tMarketFlow.runSafely() }

    underTest.runFlows()

    verifyAll {
      billaFlow.runSafely()
      kauflandFlow.runSafely()
      lidlFlow.runSafely()
      fantasticoFlow.runSafely()
      tMarketFlow.runSafely()
    }
  }
}
