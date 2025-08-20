package com.stefanbratanov.sofiasupermarketsapi.common

import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

fun copyURLToFile(source: URL, destination: Path): Long {
  return source.openStream().use { inputStream ->
    Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
  }
}
