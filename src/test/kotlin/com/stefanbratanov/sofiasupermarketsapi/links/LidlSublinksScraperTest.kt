package com.stefanbratanov.sofiasupermarketsapi.links

import assertk.assertThat
import assertk.assertions.isNotEmpty
import org.junit.jupiter.api.Test
import java.net.URL

internal class LidlSublinksScraperTest {

    @Test
    fun `scrapes real lidl website`() {
        val underTest = LidlSublinksScraper(URL("https://www.lidl.bg"))
        val result = underTest.getSublinks()

        result.forEach {
            println(it)
        }

        assertThat(result).isNotEmpty()
    }
}