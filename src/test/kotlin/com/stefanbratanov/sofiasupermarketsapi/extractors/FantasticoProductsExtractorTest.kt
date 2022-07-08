package com.stefanbratanov.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.stefanbratanov.sofiasupermarketsapi.getPath
import com.stefanbratanov.sofiasupermarketsapi.readResource
import com.stefanbratanov.sofiasupermarketsapi.testObjectMapper
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE
import java.nio.file.Paths

internal class FantasticoProductsExtractorTest {

    private val objectMapper = testObjectMapper()

    private val underTest = FantasticoProductsExtractor()

    @ParameterizedTest
    @CsvSource(
        "fantastico_test.pdf,expected.json",
        "fantastico_test_2.pdf,expected_2.json",
        "fantastico_test_3.pdf,expected_3.json",
        "fantastico_test_4.pdf,expected_4.json",
        "fantastico_test_5.pdf,expected_5.json",
        "fantastico_test_6.pdf,expected_6.json",
        "fantastico_test_7.pdf,expected_7.json",
        "fantastico_test_8.pdf,expected_8.json",
        "fantastico_test_9.pdf,expected_9.json",
        "fantastico_test_10.pdf,expected_10.json",
        "fantastico_test_11.pdf,expected_11.json"
    )
    fun `test extracting products`(inputFile: String, expectedFile: String) {
        val testPdf = getPath("/extractors/fantastico/$inputFile")

        val products = underTest.extract(testPdf)

        val actualJson = objectMapper.writeValueAsString(products)

        println(actualJson)
        val expectedJson = readResource("/extractors/fantastico/$expectedFile")

        JSONAssert.assertEquals(expectedJson, actualJson, NON_EXTENSIBLE)
    }

    @Test
    @Disabled("used for manual testing")
    fun `test fetching from real pdf`() {
        val pdf = Paths.get("fantastico_2.pdf")
        val products = underTest.extract(pdf)

        val json = objectMapper.writeValueAsString(products)

        println(json)

        assertThat(products).isNotEmpty()
    }
}
