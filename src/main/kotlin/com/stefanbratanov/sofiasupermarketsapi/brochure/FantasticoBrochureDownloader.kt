package com.stefanbratanov.sofiasupermarketsapi.brochure

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.copyURLToFile
import com.stefanbratanov.sofiasupermarketsapi.common.getHtmlDocument
import com.stefanbratanov.sofiasupermarketsapi.interfaces.BrochureDownloader
import com.stefanbratanov.sofiasupermarketsapi.interfaces.BrochureDownloader.Brochure
import io.github.bonigarcia.wdm.WebDriverManager
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ofPattern
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.text.RegexOption.IGNORE_CASE
import org.openqa.selenium.By
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Log
@Component
class FantasticoBrochureDownloader(@Value("\${fantastico.url}") private val url: URL) :
  BrochureDownloader {

  companion object {
    val options: ChromeOptions

    init {
      WebDriverManager.chromedriver().setup()
      options = ChromeOptions()
      options.addArguments("--headless=new")
      options.addArguments("--disable-gpu")
      options.addArguments("--window-size=1920,1200")
      options.addArguments("--no-sandbox")
      options.addArguments("--disable-dev-shm-usage")
      options.setPageLoadStrategy(PageLoadStrategy.NONE)
    }
  }

  private val yearPattern = ofPattern("d.MM.yyyy")

  private val brochureNamesToIgnore =
    listOf("Само за (?!София)".toRegex(IGNORE_CASE), "Легенда цени".toRegex(IGNORE_CASE))

  override fun download(): List<Brochure> {
    val htmlDoc = getHtmlDocument(url)

    val driver = ChromeDriver(options)

    val waitDriver = WebDriverWait(driver, Duration.ofSeconds(30))

    val brochures =
      htmlDoc
        .select("div.brochure-container.first div.hold-options")
        .filter {
          val brochureTitle = it.selectFirst("p.brochure-title")?.text()
          val isApplicable =
            brochureNamesToIgnore.none { rgx -> brochureTitle?.contains(rgx) == true }
          if (!isApplicable) {
            log.info("Ignoring {} because it is not applicable", brochureTitle)
          }
          isApplicable
        }
        .map {
          val brochureTitle = it.selectFirst("p.brochure-title")?.text()
          val brochureDescription = it.selectFirst("div.brochure-description")?.text()
          val dateRange = extractDateRange(brochureDescription)
          log.info(
            "Fantastico brochure is valid from ${dateRange?.first} until ${dateRange?.second}"
          )

          val flippingBookUrl = it.attr("data-url")

          // loading the flipping book
          for (attempt in 1..3) try {
            driver.get(flippingBookUrl)
            break
          } catch (_: TimeoutException) {
            if (attempt != 3) {
              log.warn("Retrying loading flipping book due to timeout...")
            }
          }

          val downloadSelector =
            By.cssSelector("a[aria-label=\"Download the flipbook as a PDF file\"]")

          clickDownloadButton(waitDriver, downloadSelector)

          val downloadUrl =
            URI(driver.findElement(downloadSelector).getDomProperty("href")!!).toURL()

          log.info("Downloading brochure from {}", downloadUrl)

          val tempDirectory = Files.createTempDirectory("brochures-download")

          val downloadPath =
            tempDirectory.resolve("${brochureTitle}_${System.currentTimeMillis()}.pdf")

          val downloadedBytes = copyURLToFile(downloadUrl, downloadPath)

          log.info("Downloaded brochure to {} ({} bytes)", downloadPath, downloadedBytes)

          Brochure(downloadPath, dateRange?.first, dateRange?.second)
        }

    driver.quit()

    return brochures
  }

  private fun extractDateRange(description: String?): Pair<LocalDate?, LocalDate?>? {
    return description
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
    return input.replace("(\\.|(?<=\\.\\d{2}))\$".toRegex(), ".${LocalDate.now().year}")
  }

  private fun clickDownloadButton(waitDriver: WebDriverWait, downloadSelector: By) {
    log.info("Trying to click download button")
    val popupWindowSelector = By.cssSelector("button[title=\"Download\"]")
    waitDriver.until(elementToBeClickable(popupWindowSelector)).click()
    waitDriver.until(elementToBeClickable(downloadSelector)).click()
    log.info("Clicked download button")
    // sleep a bit after clicking
    TimeUnit.SECONDS.sleep(2)
  }
}
