package com.stefanbratanov.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isEqualToIgnoringGivenProperties
import com.stefanbratanov.sofiasupermarketsapi.getUri
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import java.time.LocalDate
import java.time.Month
import org.junit.jupiter.api.Test

internal class LidlProductExtractorTest {

  private val underTest = LidlProductExtractor()

  @Test
  fun `test extracting product`() {
    val testHtmlUrl = getUri("/extractors/lidl/input-single.html").toURL()

    val product = underTest.extract(testHtmlUrl)

    val expectedProduct =
      Product(
        name = "Багета Рустик",
        quantity = "250 g/бр.",
        price = 0.99,
        oldPrice = 1.99,
        category = null,
        picUrl = null
      )

    assertThat(product)
      .isEqualToIgnoringGivenProperties(expectedProduct, Product::validFrom, Product::validUntil)

    assertThat(product.validFrom?.dayOfMonth).isEqualTo(30)
    assertThat(product.validFrom?.month).isEqualTo(Month.OCTOBER)
    assertThat(product.validFrom?.year).isEqualTo(LocalDate.now().year)
    assertThat(product.validUntil?.dayOfMonth).isEqualTo(5)
    assertThat(product.validUntil?.month).isEqualTo(Month.NOVEMBER)
    assertThat(product.validUntil?.year).isEqualTo(LocalDate.now().year)
  }
}
