package com.stefata.sofiasupermarketsapi.brochure

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.interfaces.BrochureDownloader
import com.stefata.sofiasupermarketsapi.interfaces.BrochureDownloader.Brochure
import io.github.bonigarcia.wdm.WebDriverManager
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.jsoup.nodes.Element
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.CapabilityType.SUPPORTS_JAVASCRIPT
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern
import java.util.concurrent.TimeUnit

@Log
@Component
class FantasticoBrochureDownloader(
    @Value("\${fantastico.url}") private val url: URL
) : BrochureDownloader {

    companion object {
        var capabilities: DesiredCapabilities

        init {
            WebDriverManager.phantomjs().setup()
            capabilities = DesiredCapabilities()
            capabilities.setCapability(SUPPORTS_JAVASCRIPT, true)
        }
    }

    private val yearPattern = ofPattern("dd.MM.yyyy")

    override fun download(): List<Brochure> {
        val htmlDoc = getHtmlDocument(url)

        val driver = PhantomJSDriver(capabilities)
        driver.manage().window().size = Dimension(1920, 1200)
        driver.get(url.toExternalForm())
        val waitDriver = WebDriverWait(driver, 10)

        val brochures = htmlDoc.select("div.brochure-container.first div.hold-options").map {
            val dateRange = extractDateRange(it.selectFirst("p.paragraph"))
            log.info(
                "Fantastico brochure is vaild " +
                        "from ${dateRange?.first} until ${dateRange?.second}"
            )

            val iFrameUrl = it.attr("data-brochure")

            val downloadHref = if (iFrameUrl.isEmpty()) {
                val dataId = it.attr("data-id")
                clickBrochure(dataId, waitDriver)
                val downloadSelector = By.cssSelector("div.brochure-container.first a[title='Сваляне']")
                driver.findElement(downloadSelector).getAttribute("href")
            } else {
                getHtmlDocument(URL(iFrameUrl)).selectFirst("a#brochure__controls__download")
                    .attr("href")
            }

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

        driver.quit()

        return brochures
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

    private fun clickBrochure(dataId: String, waitDriver: WebDriverWait) {
        log.info("Trying to click brochure with data-id: $dataId")
        val cssSelector = By.cssSelector("div.hold-options[data-id='${dataId}']")
        waitDriver.until(elementToBeClickable(cssSelector)).click()
        //sleep a bit after clicking
        TimeUnit.SECONDS.sleep(2)
    }
}