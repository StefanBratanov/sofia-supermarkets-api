package com.stefanbratanov.sofiasupermarketsapi.links

import assertk.assertThat
import assertk.assertions.isNotEmpty
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URL

internal class TMarketSublinksScraperTest {

    @Test
    @Disabled("Takes too long to execute. Only use for manual testing.")
    fun `scrapes real tmarket website`() {
        val underTest = TMarketSublinksScraper(
            URL("https://tmarketonline.bg/"),
            TMarketPagesRetriever(),
        )
        val result = underTest.getSublinks()

        result.forEach {
            println(it)
        }

        assertThat(result).isNotEmpty()
    }
}
