package com.stefata.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.stefata.sofiasupermarketsapi.getUri
import com.stefata.sofiasupermarketsapi.readResource
import com.stefata.sofiasupermarketsapi.testObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode.STRICT
import java.net.URL

internal class TMarketProductsExtractorTest {

    private val objectMapper = testObjectMapper()

    private val underTest = TMarketProductsExtractor()

    @Test
    fun `test extracting products`() {
        val testHtmlUrl = getUri("/extractors/tmarket/input.html").toURL()

        val products = underTest.extract(testHtmlUrl)

        val actualJson = objectMapper.writeValueAsString(products)
        val expectedJson = readResource("/extractors/tmarket/expected.json")

        JSONAssert.assertEquals(expectedJson, actualJson, STRICT)

    }

    @Test
    @Disabled("used for manual testing")
    fun `test fetching from real url`() {

        val tmarketUrl = URL("https://tmarketonline.bg/category/visokoalkoholni-napitki");
        val products = underTest.extract(tmarketUrl)

        assertThat(products).isNotEmpty()
    }
}