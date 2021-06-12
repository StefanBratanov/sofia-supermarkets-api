package com.stefata.sofiasupermarketsapi.brochure

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.interfaces.BrochureDownloader
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Log
@Component
class FantasticoBrochureDownloader(
    @Value("\${fantastico.url}") private val url: URL
) : BrochureDownloader {

    override fun download(): Pair<Path, LocalDate?> {
        val htmlDoc = getHtmlDocument(url)

        val validUntil = htmlDoc.selectFirst("div.brochure-container p.paragraph")?.text()?.trim()?.let {
            "\\d+.\\d+.\\d+\$".toRegex().find(it)
        }?.let {
            val match = it.groupValues[0]
            try {
                LocalDate.parse(match, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            } catch (ex: Exception) {
                log.error("Error while parsing $match", ex)
                null
            }
        }

        log.info("Fantastico brochure is vaild until $validUntil")

        val iFrameUrl = htmlDoc.selectFirst("div.brochure-container iframe.brochure-iframe")
            .attr("src")

        val downloadHref = getHtmlDocument(URL(iFrameUrl)).selectFirst("a#brochure__controls__download")
            .attr("href")
        val filenameMinusPath = FilenameUtils.getName(downloadHref)
        val encodedHref = downloadHref.replace(filenameMinusPath, URLEncoder.encode(filenameMinusPath, UTF_8.name()))
        val downloadUrl = URL(encodedHref)

        val tempDirectory = Files.createTempDirectory("brochures-download")
        val filename = filenameMinusPath.let {
            if (it.contains(".")) it else "$it.pdf"
        }

        val downloadPath = tempDirectory.resolve(filename)

        log.info("Downloading {}", downloadUrl)

        FileUtils.copyURLToFile(downloadUrl, downloadPath.toFile())

        return Pair(downloadPath, validUntil)
    }
}