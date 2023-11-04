package com.stefanbratanov.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.stefanbratanov.sofiasupermarketsapi.getUri
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import com.stefanbratanov.sofiasupermarketsapi.readResource
import com.stefanbratanov.sofiasupermarketsapi.testObjectMapper
import io.mockk.every
import io.mockk.mockk
import java.net.URL
import java.time.LocalDate
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

internal class LidlProductsExtractorTest {

  private val objectMapper = testObjectMapper()

  private val lidlProductExtractor: LidlProductExtractor = mockk()

  private val underTest = LidlProductsExtractor(URL("https://www.lidl.bg"), lidlProductExtractor)

  @Test
  fun `test extracting products`() {
    every { lidlProductExtractor.extract(any()) } returns
      Product(
        name = "foo",
        "1 кг",
        8.99,
        10.99,
        category = null,
        picUrl = null,
        validFrom = LocalDate.of(1993, 7, 28),
        validUntil = LocalDate.of(2023, 11, 4)
      )

    val testHtmlUrl = getUri("/extractors/lidl/input.html").toURL()

    val products = underTest.extract(testHtmlUrl)

    val actualJson = objectMapper.writeValueAsString(products)
    val expectedJson = readResource("/extractors/lidl/expected.json")

    // use current year for comparison
    JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT)
  }

  @Test
  @Disabled("used for manual testing")
  fun `test fetching from real url`() {
    // use real product extractor
    val underTest = LidlProductsExtractor(URL("https://www.lidl.bg"), LidlProductExtractor())
    val lidlUrl =
      URL(
        "https://www.lidl.bg/c/niska-tsena-visoko-kachestvo/a10031916?channel=store&tabCode=Current_Sales_Week"
      )
    val products = underTest.extract(lidlUrl)

    products.forEach { println(it) }

    assertThat(products).isNotEmpty()
  }
}
