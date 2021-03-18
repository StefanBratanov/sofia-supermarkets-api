package com.stefata.sofiasupermarketsapi

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun getPath(resource: String): Path {
    return Paths.get(getUri(resource))
}

fun getUri(resource: String): URI {
    return object {}.javaClass.getResource(resource).toURI()
}

fun readResource(resource: String): String {
    return Files.readString(getPath(resource), StandardCharsets.UTF_8)
}
