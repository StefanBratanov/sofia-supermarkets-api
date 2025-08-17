package com.stefanbratanov.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.stefanbratanov.sofiasupermarketsapi.getUri
import com.stefanbratanov.sofiasupermarketsapi.readResource
import com.stefanbratanov.sofiasupermarketsapi.testObjectMapper
import java.net.URI
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode.STRICT

internal class TMarketProductsExtractorTest {

  private val objectMapper = testObjectMapper()

  private val underTest = TMarketProductsExtractor()

  @Test
  fun `test extracting products`() {
    val testHtmlUrl = getUri("/extractors/tmarket/input.html").toURL()

    val products = underTest.extract(testHtmlUrl)

    val actualJson = objectMapper.writeValueAsString(products)
    val expectedJson = readResource("/extractors/tmarket/expected.json")

    JSONAssert.assertEquals(expectedJson, actualJson, STRICT)
  }

  @Test
  @Disabled("used for manual testing")
  fun `test fetching from real url`() {
    val tmarketUrl = URI("https://tmarketonline.bg/category/visokoalkoholni-napitki").toURL()
    val products = underTest.extract(tmarketUrl)

    assertThat(products).isNotEmpty()
  }
}
