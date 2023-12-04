package com.stefanbratanov.sofiasupermarketsapi.brochure

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.copyURLToFile
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import com.stefanbratanov.sofiasupermarketsapi.common.getNameMinusThePath
import com.stefanbratanov.sofiasupermarketsapi.interfaces.BrochureDownloader
import com.stefanbratanov.sofiasupermarketsapi.interfaces.BrochureDownloader.Brochure
import io.github.bonigarcia.wdm.WebDriverManager
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.text.RegexOption.IGNORE_CASE
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Log
@Component
class FantasticoBrochureDownloader(
  @Value("\${fantastico.url}") private val url: URL,
  @Value("\${chromium.binary}") private val chromiumBinary: String
) : BrochureDownloader {

  companion object {
    var options: ChromeOptions

    init {
      WebDriverManager.chromiumdriver().setup()
      options = ChromeOptions()
      options.addArguments(
        "--headless=new",
        "--disable-gpu",
        "--ignore-certificate-errors",
        "--disable-extensions",
        "--no-sandbox",
        "--disable-dev-shm-usage"
      )
    }
  }

  private val yearPattern = ofPattern("d.MM.yyyy")

  private val brochureNamesToIgnore =
    listOf("Само за (?!София)".toRegex(IGNORE_CASE), "Легенда цени".toRegex(IGNORE_CASE))

  override fun download(): List<Brochure> {
    val htmlDoc = getHtmlDocument(url)

    chromiumBinary.takeUnless { it.isEmpty() }?.let { options.setBinary(it) }
    val driver = ChromeDriver(options)
    driver.manage().window().size = Dimension(1920, 1200)
    driver.get(url.toExternalForm())
    val waitDriver = WebDriverWait(driver, Duration.ofSeconds(10))

    val brochures =
      htmlDoc
        .select("div.brochure-container.first div.hold-options")
        .filter { element ->
          val nameOfBrochure = element.selectFirst("p.paragraph")?.text()
          val isApplicable =
            brochureNamesToIgnore.none { rgx -> nameOfBrochure?.contains(rgx) == true }
          if (!isApplicable) {
            log.info("Ignoring {} because it is not applicable", nameOfBrochure)
          }
          isApplicable
        }
        .map {
          val nameOfBrochure = it.selectFirst("p.paragraph")?.text()
          val dateRange = extractDateRange(nameOfBrochure)
          log.info(
            "Fantastico brochure is vaild " + "from ${dateRange?.first} until ${dateRange?.second}",
          )

          val iFrameUrl = it.attr("data-brochure")

          val downloadHref =
            if (iFrameUrl.isEmpty()) {
              val dataId = it.attr("data-id")
              clickBrochure(dataId, waitDriver)
              val downloadSelector =
                By.cssSelector("div.brochure-container.first a[title='Сваляне']")
              driver.findElement(downloadSelector).getAttribute("href")
            } else {
              getHtmlDocument(URL(iFrameUrl))
                .selectFirst("a#brochure__controls__download")
                ?.attr("href")!!
            }

          val filenameMinusPath = getNameMinusThePath(downloadHref)
          val encodedHref =
            downloadHref.replace(
              filenameMinusPath,
              URLEncoder.encode(filenameMinusPath, UTF_8.name()),
            )
          val downloadUrl = URL(encodedHref)

          val tempDirectory = Files.createTempDirectory("brochures-download")
          val filename =
            filenameMinusPath.let { fn ->
              val unixTime = System.currentTimeMillis()
              if (fn.contains(".")) "${unixTime}_$fn" else "${unixTime}_$fn.pdf"
            }

          val downloadPath = tempDirectory.resolve(filename)
          val downloadedBytes = copyURLToFile(downloadUrl, downloadPath)
          log.info("Downloaded {} ({} bytes)", downloadUrl, downloadedBytes)

          Brochure(downloadPath, dateRange?.first, dateRange?.second)
        }

    driver.quit()

    return brochures
  }

  private fun extractDateRange(title: String?): Pair<LocalDate?, LocalDate?>? {
    return title
      ?.trim()
      ?.let { "(\\d+.\\d+\\.?(\\.\\d+)?)\\s*-\\s*(\\d+.\\d+\\.\\d+)".toRegex().find(it) }
      ?.let {
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
    return input.replace(
      "(\\.|(?<=\\.\\d{2}))\$".toRegex(),
      ".${LocalDate.now().year}",
    )
  }

  private fun clickBrochure(dataId: String, waitDriver: WebDriverWait) {
    log.info("Trying to click brochure with data-id: $dataId")
    val cssSelector = By.cssSelector("div.hold-options[data-id='$dataId']")
    waitDriver.until(ExpectedConditions.invisibilityOfElementLocated(By.className("preloader")))
    waitDriver.until(elementToBeClickable(cssSelector)).click()
    // sleep a bit after clicking
    TimeUnit.SECONDS.sleep(2)
  }
}
