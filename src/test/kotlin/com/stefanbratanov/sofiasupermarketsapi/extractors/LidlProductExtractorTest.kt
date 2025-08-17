package com.stefanbratanov.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isEqualToIgnoringGivenProperties
import assertk.assertions.isNotNull
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
        name = "Розе Престиж",
        quantity = "0.75 l/опаковка",
        price = 7.49,
        oldPrice = 9.99,
        category = null,
        picUrl = null,
      )

    assertThat(product)
      .isNotNull()
      .isEqualToIgnoringGivenProperties(expectedProduct, Product::validFrom, Product::validUntil)

    assertThat(product?.validFrom?.dayOfMonth).isEqualTo(11)
    assertThat(product?.validFrom?.month).isEqualTo(Month.AUGUST)
    assertThat(product?.validFrom?.year).isEqualTo(LocalDate.now().year)
    assertThat(product?.validUntil?.dayOfMonth).isEqualTo(17)
    assertThat(product?.validUntil?.month).isEqualTo(Month.AUGUST)
    assertThat(product?.validUntil?.year).isEqualTo(LocalDate.now().year)
  }
}
