package com.stefata.sofiasupermarketsapi.extractors

import assertk.assertThat
import assertk.assertions.isNotEmpty
import com.fasterxml.jackson.databind.ObjectMapper
import com.stefata.sofiasupermarketsapi.getPath
import com.stefata.sofiasupermarketsapi.readResource
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode.NON_EXTENSIBLE
import java.nio.file.Paths

internal class FantasticoProductsExtractorTest {

    private val objectMapper = ObjectMapper()

    private val underTest = FantasticoProductsExtractor()

    @Test
    fun `test extracting products`() {

        val testPdf = getPath("/extractors/fantastico/fantastico_test.pdf")

        val products = underTest.extract(testPdf)

        val actualJson = objectMapper.writeValueAsString(products)
        val expectedJson = readResource("/extractors/fantastico/expected.json")

        JSONAssert.assertEquals(expectedJson, actualJson, NON_EXTENSIBLE)
    }

    @Test
    @Disabled("used for manual testing")
    fun `test fetching from real pdf`() {

        val pdf = Paths.get("fantastiko.pdf")
        val products = underTest.extract(pdf)

        assertThat(products).isNotEmpty()
    }
}