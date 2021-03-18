package com.stefata.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URL

internal class BillaProductsExtractorTest {

    private val underTest = BillaProductsExtractor()

    @Test
    @Disabled("used for manual testing")
    fun `test fetching from real url`() {

        val billaUrl = URL("https://ssbbilla.site/weekly")
        val products = underTest.extract(billaUrl)

        assertThat(products).isNotEmpty()
    }

}