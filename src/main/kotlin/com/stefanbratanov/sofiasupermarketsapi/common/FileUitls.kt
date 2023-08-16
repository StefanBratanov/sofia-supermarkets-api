package com.stefanbratanov.sofiasupermarketsapi.common

import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

fun copyURLToFile(source: URL, destination: Path) {
  source.openStream().use { Files.copy(it, destination, StandardCopyOption.REPLACE_EXISTING) }
}

/** This method is based on org.apache.commons.io.FilenameUtils.getName() */
fun getNameMinusThePath(fileName: String): String {
  val lastUnixPos = fileName.lastIndexOf('/')
  val lastWindowsPos = fileName.lastIndexOf('\\')
  val indexOfLastSeparator = lastUnixPos.coerceAtLeast(lastWindowsPos)
  return fileName.substring(indexOfLastSeparator + 1)
}
