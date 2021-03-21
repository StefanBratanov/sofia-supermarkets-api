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

@Log
@Component
class FantasticoBrochureDownloader(
    @Value("\${fantastico.url}") private val url: URL
) : BrochureDownloader {

    override fun download(): Path {
        val iFrameUrl = getHtmlDocument(url).selectFirst("div.brochure-container iframe.brochure-iframe")
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

        return downloadPath
    }
}