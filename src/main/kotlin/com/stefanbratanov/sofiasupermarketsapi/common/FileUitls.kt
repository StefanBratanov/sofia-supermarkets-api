package com.stefanbratanov.sofiasupermarketsapi.common

import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.fileSize
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClients

fun copyURLToFile(source: URL, destination: Path): Long {
  val client = HttpClients.createDefault()
  val request =
    HttpGet(source.toURI()).apply {
      setHeader(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36",
      )
    }

  client.execute(request) {
    it.entity!!.content.use { inputStream ->
      FileOutputStream(destination.toFile()).use { outputStream ->
        inputStream.copyTo(outputStream)
      }
    }
  }

  return destination.fileSize()
}
