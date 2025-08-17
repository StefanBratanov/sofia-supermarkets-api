package com.stefanbratanov.sofiasupermarketsapi.brochure

import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.exists
import java.net.URI
import java.nio.file.Files
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class FantasticoBrochureDownloaderTest {

  private lateinit var underTest: FantasticoBrochureDownloader

  @Test
  @Disabled("need to change the downloading logic")
  fun `downloads real brochure`() {
    underTest =
      FantasticoBrochureDownloader(URI("https://www.fantastico.bg/special-offers").toURL())
    val result = underTest.download()

    result.forEach {
      assertThat(it.path.toFile()).exists()
      assertThat(it.path.fileName.toString()).endsWith(".pdf")
      Files.delete(it.path)
    }
  }
}
