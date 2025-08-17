package com.stefanbratanov.sofiasupermarketsapi.links

import assertk.assertThat
import assertk.assertions.isNotEmpty
import java.net.URI
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class TMarketPagesRetrieverTest {

  val underTest = TMarketPagesRetriever()

  @Test
  @Disabled("Accessing the webpage fails in CI")
  fun `retrieves all pages for an url`() {
    val result =
      underTest.retrieveAllPages(
        URI("https://tmarketonline.bg/category/visokoalkoholni-napitki").toURL()
      )

    assertThat(result).isNotEmpty()
  }
}
