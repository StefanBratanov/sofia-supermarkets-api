package com.stefanbratanov.sofiasupermarketsapi.links

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isNotEmpty
import com.stefanbratanov.sofiasupermarketsapi.getUri
import java.net.URL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class BillaSublinksScraperTest {

  private lateinit var underTest: BillaSublinksScraper

  @BeforeEach
  fun setup() {
    val baseUrl = getUri("/links/billa/base.html").toURL()
    underTest = BillaSublinksScraper(baseUrl)
  }

  @Test
  fun `scrapes billa for sublinks`() {
    val result = underTest.getSublinks()

    assertThat(result)
      .containsExactlyInAnyOrder(
        URL("https://ssbbilla.site/weekly"),
        URL("https://ssbbilla.site/sixth"),
        URL("https://ssbbilla.site/nine"),
      )
  }

  @Test
  fun `scrapes real billa website`() {
    underTest = BillaSublinksScraper(URL("https://ssbbilla.site/"))

    val result = underTest.getSublinks()

    result.forEach { println(it) }

    assertThat(result).isNotEmpty()
  }
}
