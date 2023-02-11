package com.stefanbratanov.sofiasupermarketsapi.links

import assertk.assertThat
import assertk.assertions.isNotEmpty
import java.net.URL
import org.junit.jupiter.api.Test

internal class TMarketPagesRetrieverTest {

  val underTest = TMarketPagesRetriever()

  @Test
  fun `retrieves all pages for an url`() {
    val result =
      underTest.retrieveAllPages(URL("https://tmarketonline.bg/category/visokoalkoholni-napitki"))

    assertThat(result).isNotEmpty()
  }
}
