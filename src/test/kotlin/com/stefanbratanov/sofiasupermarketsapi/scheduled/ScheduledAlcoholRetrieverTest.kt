package com.stefanbratanov.sofiasupermarketsapi.scheduled

import com.stefanbratanov.sofiasupermarketsapi.api.AlcoholController
import com.stefanbratanov.sofiasupermarketsapi.api.ProductCriteria
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ScheduledAlcoholRetrieverTest {

  @MockK lateinit var alcoholController: AlcoholController
  @MockK lateinit var flowsRunner: ScheduledFlowsRunner

  @InjectMockKs lateinit var scheduledAlcoholRetriever: ScheduledAlcoholRetriever

  @Test
  fun `retrieves alcohol`() {
    every { flowsRunner.isRunning() } returns false
    every { alcoholController.alcohol(any(), any(), any()) } returns emptyList()

    scheduledAlcoholRetriever.retrieveAlcohol()

    val productCriteria = ProductCriteria(null, false)
    verify { alcoholController.alcohol(productCriteria, null, true) }
  }
}
