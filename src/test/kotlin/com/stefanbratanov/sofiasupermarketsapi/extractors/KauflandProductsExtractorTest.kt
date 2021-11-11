package com.stefanbratanov.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.stefanbratanov.sofiasupermarketsapi.getUri
import com.stefanbratanov.sofiasupermarketsapi.readResource
import com.stefanbratanov.sofiasupermarketsapi.testObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode.STRICT
import java.net.URL

internal class KauflandProductsExtractorTest {

    private val objectMapper = testObjectMapper()

    private val underTest = KauflandProductsExtractor()

    @Test
    fun `test extracting products`() {
        val testHtmlUrl = getUri("/extractors/kaufland/input.html").toURL()

        val products = underTest.extract(testHtmlUrl)

        val actualJson = objectMapper.writeValueAsString(products)
        val expectedJson = readResource("/extractors/kaufland/expected.json")

        JSONAssert.assertEquals(expectedJson, actualJson, STRICT)

    }

    @Test
    @Disabled("used for manual testing")
    fun `test fetching from real url`() {

        val kauflandUrl =
            URL("https://www.kaufland.bg/aktualni-predlozheniya/ot-ponedelnik/obsht-pregled.category=08_%D0%90%D0%BB%D0%BA%D0%BE%D1%85%D0%BE%D0%BB%D0%BD%D0%B8_%D0%B8_%D0%B1%D0%B5%D0%B7%D0%B0%D0%BB%D0%BA%D0%BE%D1%85%D0%BE%D0%BB%D0%BD%D0%B8_%D0%BD%D0%B0%D0%BF%D0%B8%D1%82%D0%BA%D0%B8.html");
        val products = underTest.extract(kauflandUrl)

        assertThat(products).isNotEmpty()
    }

}