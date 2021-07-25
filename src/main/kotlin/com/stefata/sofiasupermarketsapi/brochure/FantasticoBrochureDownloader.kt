package com.stefata.sofiasupermarketsapi.brochure

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.interfaces.BrochureDownloader
import com.stefata.sofiasupermarketsapi.interfaces.BrochureDownloader.Brochure
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.jsoup.nodes.Element
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Log
@Component
class FantasticoBrochureDownloader(
    @Value("\${fantastico.url}") private val url: URL
) : BrochureDownloader {

    private val yearPattern = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    override fun download(): List<Brochure> {
        val htmlDoc = getHtmlDocument(url)

        return htmlDoc.select("div.brochure-container.first div.hold-options").map {
            val dateRange = extractDateRange(it.selectFirst("p.paragraph"))
            log.info(
                "Fantastico brochure is vaild " +
                        "from ${dateRange?.first} until ${dateRange?.second}"
            )

            val iFrameUrl = it.attr("data-brochure")

            val downloadHref = getHtmlDocument(URL(iFrameUrl)).selectFirst("a#brochure__controls__download")
                .attr("href")
            val filenameMinusPath = FilenameUtils.getName(downloadHref)
            val encodedHref =
                downloadHref.replace(filenameMinusPath, URLEncoder.encode(filenameMinusPath, UTF_8.name()))
            val downloadUrl = URL(encodedHref)

            val tempDirectory = Files.createTempDirectory("brochures-download")
            val filename = filenameMinusPath.let { fn ->
                if (fn.contains(".")) fn else "$fn.pdf"
            }

            val downloadPath = tempDirectory.resolve(filename)
            log.info("Downloading {}", downloadUrl)
            FileUtils.copyURLToFile(downloadUrl, downloadPath.toFile())

            Brochure(downloadPath, dateRange?.first, dateRange?.second)

        }
    }

    private fun extractDateRange(element: Element?): Pair<LocalDate?, LocalDate?>? {
        return element?.text()?.trim()?.let {
            "(\\d+.\\d+(\\.\\d+)?)-(\\d+.\\d+\\.(\\d+)?)\$".toRegex().find(it)
        }?.let {
            try {
                val start = addYearIfApplicable(it.groupValues[1])
                val end = addYearIfApplicable(it.groupValues[3])
                val startDate = LocalDate.parse(start, yearPattern)
                val endDate = LocalDate.parse(end, yearPattern)
                Pair(startDate, endDate)
            } catch (ex: Exception) {
                log.error("Error while parsing dates from: ${it.groupValues[0]}", ex)
                Pair(null, null)
            }
        }
    }

    private fun addYearIfApplicable(input: String): String {
        return input.replace("(\\.|(?<=\\.\\d{2}))\$".toRegex(), ".${LocalDate.now().year}")
    }
}