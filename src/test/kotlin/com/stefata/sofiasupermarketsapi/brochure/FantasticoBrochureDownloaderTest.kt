package com.stefata.sofiasupermarketsapi.brochure

import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.exists
import org.junit.jupiter.api.Test
import java.net.URL
import java.nio.file.Files

internal class FantasticoBrochureDownloaderTest {

    private lateinit var underTest: FantasticoBrochureDownloader

    @Test
    fun `downloads real brochure`() {

        underTest = FantasticoBrochureDownloader(URL("https://www.fantastico.bg/special-offers"))
        val result = underTest.download()

        assertThat(result.toFile()).exists()
        assertThat(result.fileName.toString()).endsWith(".pdf")

        Files.delete(result)

    }
}