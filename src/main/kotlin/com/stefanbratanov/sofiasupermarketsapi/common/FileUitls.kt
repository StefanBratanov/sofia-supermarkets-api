package com.stefanbratanov.sofiasupermarketsapi.common

import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

fun copyURLToFile(source: URL, destination: Path): Long {
  val connection = source.openConnection()
  // Set a Browser-like User-Agent
  connection.setRequestProperty(
    "User-Agent",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36",
  )
  return connection.inputStream.use { inputStream ->
    Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING)
  }
}
