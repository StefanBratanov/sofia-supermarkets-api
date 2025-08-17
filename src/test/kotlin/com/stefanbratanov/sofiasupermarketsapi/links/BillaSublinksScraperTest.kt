package com.stefanbratanov.sofiasupermarketsapi.links

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isNotEmpty
import com.stefanbratanov.sofiasupermarketsapi.getUri
import java.net.URI
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
        URI("https://ssbbilla.site/weekly").toURL(),
        URI("https://ssbbilla.site/sixth").toURL(),
        URI("https://ssbbilla.site/nine").toURL(),
      )
  }

  @Test
  fun `scrapes real billa website`() {
    underTest = BillaSublinksScraper(URI("https://ssbbilla.site/").toURL())

    val result = underTest.getSublinks()

    result.forEach { println(it) }

    assertThat(result).isNotEmpty()
  }
}
