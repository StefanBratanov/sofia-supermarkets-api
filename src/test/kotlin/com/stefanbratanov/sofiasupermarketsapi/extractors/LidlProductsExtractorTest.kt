package com.stefanbratanov.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.stefanbratanov.sofiasupermarketsapi.getUri
import com.stefanbratanov.sofiasupermarketsapi.readResource
import com.stefanbratanov.sofiasupermarketsapi.testObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.CustomComparator
import java.net.URL
import java.time.LocalDate

internal class LidlProductsExtractorTest {

    private val objectMapper = testObjectMapper()

    private val underTest = LidlProductsExtractor()

    @Test
    fun `test extracting products`() {
        val testHtmlUrl = getUri("/extractors/lidl/input.html").toURL()

        val products = underTest.extract(testHtmlUrl)

        val actualJson = objectMapper.writeValueAsString(products)
        val expectedJson = readResource("/extractors/lidl/expected.json")

        val customization: (actualField: Any, expectedField: Any) -> Boolean =
            { actualField, expectedField ->
                LocalDate.parse(expectedField.toString())
                    .withYear(LocalDate.now().year)
                    .equals(LocalDate.parse(actualField.toString()))
            }

        // use current year for comparison
        JSONAssert.assertEquals(
            expectedJson,
            actualJson,
            CustomComparator(
                JSONCompareMode.STRICT,
                Customization("[*].validFrom", customization),
                Customization("[*].validUntil", customization),
            ),
        )
    }

    @Test
    @Disabled("used for manual testing")
    fun `test fetching from real url`() {
        val lidlUrl = URL("https://www.lidl.bg/bg/c/niska-cena-visoko-kachestvo/c1847/w1")
        val products = underTest.extract(lidlUrl)

        assertThat(products).isNotEmpty()
    }
}
