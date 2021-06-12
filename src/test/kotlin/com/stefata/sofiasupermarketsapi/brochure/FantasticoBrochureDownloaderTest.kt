package com.stefata.sofiasupermarketsapi.brochure

import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.exists
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test
import java.net.URL
import java.nio.file.Files

internal class FantasticoBrochureDownloaderTest {

    private lateinit var underTest: FantasticoBrochureDownloader

    @Test
    fun `downloads real brochure`() {

        underTest = FantasticoBrochureDownloader(URL("https://www.fantastico.bg/special-offers"))
        val result = underTest.download()

        assertThat(result.first.toFile()).exists()
        assertThat(result.first.fileName.toString()).endsWith(".pdf")
        assertThat(result.second).isNotNull()

        Files.delete(result.first)

    }
}