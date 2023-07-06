package com.stefanbratanov.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.stefanbratanov.sofiasupermarketsapi.getUri
import com.stefanbratanov.sofiasupermarketsapi.readResource
import com.stefanbratanov.sofiasupermarketsapi.testObjectMapper
import java.net.URL
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

internal class BillaProductsExtractorTest {

  private val objectMapper = testObjectMapper()

  private val underTest = BillaProductsExtractor()

  @Test
  fun `test extracting products`() {
    val testHtmlUrl = getUri("/extractors/billa/input.html").toURL()

    val products = underTest.extract(testHtmlUrl)

    val actualJson = objectMapper.writeValueAsString(products)
    val expectedJson = readResource("/extractors/billa/expected.json")

    JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT)
  }

  @Test
  @Disabled("used for manual testing")
  fun `test fetching from real url`() {
    val billaUrl = URL("https://ssbbilla.site/weekly")
    val products = underTest.extract(billaUrl)

    assertThat(products).isNotEmpty()
  }
}
