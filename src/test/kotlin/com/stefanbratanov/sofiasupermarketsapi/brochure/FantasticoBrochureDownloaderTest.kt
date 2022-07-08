package com.stefanbratanov.sofiasupermarketsapi.brochure

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

        result.forEach {
            assertThat(it.path.toFile()).exists()
            assertThat(it.path.fileName.toString()).endsWith(".pdf")
            assertThat(it.validFrom).isNotNull()
            assertThat(it.validUntil).isNotNull()
            Files.delete(it.path)
        }
    }
}
