package com.stefanbratanov.sofiasupermarketsapi.brochure

import assertk.assertThat
import assertk.assertions.endsWith
import assertk.assertions.exists
import java.net.URL
import java.nio.file.Files
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS

internal class FantasticoBrochureDownloaderTest {

  private lateinit var underTest: FantasticoBrochureDownloader

  @Test // requires chromium-browser installed
  @EnabledOnOs(OS.LINUX)
  fun `downloads real brochure`() {
    underTest = FantasticoBrochureDownloader(URL("https://www.fantastico.bg/special-offers"), "")
    val result = underTest.download()

    result.forEach {
      assertThat(it.path.toFile()).exists()
      assertThat(it.path.fileName.toString()).endsWith(".pdf")
      Files.delete(it.path)
    }
  }
}
