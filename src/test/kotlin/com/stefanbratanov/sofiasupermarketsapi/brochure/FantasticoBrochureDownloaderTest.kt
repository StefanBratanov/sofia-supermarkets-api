package com.stefanbratanov.sofiasupermarketsapi.brochure

import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.exists
import assertk.assertions.isNotEmpty
import java.net.URI
import java.nio.file.Files
import org.junit.jupiter.api.Test

internal class FantasticoBrochureDownloaderTest {

  private lateinit var underTest: FantasticoBrochureDownloader

  @Test
  fun `downloads real brochure`() {
    underTest =
      FantasticoBrochureDownloader(URI("https://www.fantastico.bg/special-offers").toURL())
    val result = underTest.download()

    assertThat(result).isNotEmpty()

    result.forEach {
      assertThat(it.path).exists()
      assertThat(it.path).isNotEmpty()
      assertThat(it.path.fileName.toString()).endsWith(".pdf")
      Files.delete(it.path)
    }
  }
}
