package com.stefanbratanov.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.stefanbratanov.sofiasupermarketsapi.getUri
import com.stefanbratanov.sofiasupermarketsapi.readResource
import com.stefanbratanov.sofiasupermarketsapi.testObjectMapper
import java.net.URI
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode.STRICT

internal class KauflandProductsExtractorTest {

  private val objectMapper = testObjectMapper()

  private val underTest = KauflandProductsExtractor(objectMapper)

  @Test
  fun `test extracting products`() {
    val testHtmlUrl = getUri("/extractors/kaufland/input.html").toURL()

    val products = underTest.extract(testHtmlUrl)

    val actualJson = objectMapper.writeValueAsString(products)
    val expectedJson = readResource("/extractors/kaufland/expected.json")

    JSONAssert.assertEquals(expectedJson, actualJson, STRICT)
  }

  @Test
  fun `test fetching from real url`() {
    val kauflandUrl = URI("https://www.kaufland.bg/aktualni-predlozheniya/oferti.html").toURL()
    val products = underTest.extract(kauflandUrl)

    assertThat(products).isNotEmpty()
  }
}
