package com.stefanbratanov.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.stefanbratanov.sofiasupermarketsapi.getUri
import com.stefanbratanov.sofiasupermarketsapi.readResource
import com.stefanbratanov.sofiasupermarketsapi.testObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.net.URL

internal class LidlProductsExtractorTest {

    private val objectMapper = testObjectMapper()

    private val underTest = LidlProductsExtractor()

    @Test
    fun `test extracting products`() {
        val testHtmlUrl = getUri("/extractors/lidl/input.html").toURL()

        val products = underTest.extract(testHtmlUrl)

        val actualJson = objectMapper.writeValueAsString(products)
        val expectedJson = readResource("/extractors/lidl/expected.json")

        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT)

    }

    @Test
    @Disabled("used for manual testing")
    fun `test fetching from real url`() {

        val lidlUrl = URL("https://www.lidl.bg/bg/c/niska-cena-visoko-kachestvo/c1847/w1");
        val products = underTest.extract(lidlUrl)

        assertThat(products).isNotEmpty()
    }
}