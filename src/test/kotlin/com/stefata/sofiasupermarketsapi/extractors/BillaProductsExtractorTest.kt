package com.stefata.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.fasterxml.jackson.databind.ObjectMapper
import com.stefata.sofiasupermarketsapi.getUri
import com.stefata.sofiasupermarketsapi.readResource
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.net.URL

internal class BillaProductsExtractorTest {

    private val objectMapper = ObjectMapper()

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