package com.stefata.sofiasupermarketsapi.image

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.checkIfUrlHasAcceptableHttpResponse
import com.stefata.sofiasupermarketsapi.common.getHtmlDocument
import com.stefata.sofiasupermarketsapi.interfaces.ImageSearch
import com.stefata.sofiasupermarketsapi.model.ProductImage
import com.stefata.sofiasupermarketsapi.repository.ProductImageRepository
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.apache.commons.validator.routines.UrlValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.Objects.isNull
import java.util.Objects.nonNull

@Log
@Component
class GoogleImageSearch(
    @Value("\${google.image.search.url}") private val googleImageSearchUrl: String,
    @Value("\${google.custom.search.url}") private val googleSearchUrl: String,
    private val productImageRepository: ProductImageRepository,
    private val restTemplate: RestTemplate = RestTemplate(),
    private val urlValidator: UrlValidator = UrlValidator()
) : ImageSearch {

    private val minWidth: Double = 200.0
    private val minHeight: Double = 200.0

    private val imagesRegex = "\\[\"([^,]*)\",\\s*(\\d+),\\s*(\\d+)]".toRegex()

    private val urlIgnore = listOf("https://encrypted", "x-raw-image:")
        .map { it.toRegex(RegexOption.IGNORE_CASE) }

    @Cacheable("productImages")
    override fun search(query: String): String? {
        return search(query, true)
    }

    fun search(query: String, useDatabase: Boolean): String? {

        val maybeProductImage = if (useDatabase) {
            productImageRepository.findById(query)
        } else {
            Optional.empty()
        }

        if (maybeProductImage.isPresent) {
            log.info("Retrieved image url for {} from database", query)
            return maybeProductImage.get().url
        }

        log.info("Querying for an image for {} from Google", query)

        try {
            val urlWithQuery = "$googleImageSearchUrl&q=${URLEncoder.encode(query, StandardCharsets.UTF_8)}"
            val googleResultHtml = getHtmlDocument(URL(urlWithQuery)).html()
            val imageLink = findImage(googleResultHtml)
            if (nonNull(imageLink)) {
                if (useDatabase) {
                    saveImageToDatabase(query, imageLink)
                }
                return imageLink
            }
        } catch (ex: Exception) {
            log.error("Error querying google image search. Will fallback to custom search", ex)
        }

        log.info("Using custom search to find an image for {} from Google", query)

        val response = try {
            restTemplate.exchange(
                "$googleSearchUrl&q={query}", HttpMethod.GET, null,
                SearchResult::class.java, query
            )
        } catch (httpEx: HttpStatusCodeException) {
            log.error("Error querying google custom search", httpEx)
            ResponseEntity.status(httpEx.statusCode).body(null)
        }

        if (response.statusCode != HttpStatus.OK || !response.hasBody()) {
            log.error("There was an error querying for an image for $query from Google: ${response.statusCode}")
            return null
        }

        val imageLink = response.body!!.items?.sortedByDescending {
            if (isNull(it.title)) 0 else FuzzySearch.ratio(query.toLowerCase(), it.title!!.toLowerCase())
        }?.firstOrNull {
            val sizeIsGood = it.image.width >= minWidth && it.image.height >= minHeight
            sizeIsGood && it.fileFormat?.equals("image/", ignoreCase = true) == false
        }?.link

        if (useDatabase) {
            saveImageToDatabase(query, imageLink)
        }

        return imageLink
    }

    fun findImage(html: String): String? {
        return imagesRegex.findAll(html).filter {
            urlIgnore.any { regex ->
                val imageUrl = it.groups[1]?.value
                imageUrl?.contains(regex) == false
            }
        }.filter {
            val width = it.groups[2]?.value?.toDouble()
            val height = it.groups[3]?.value?.toDouble()
            compareValues(width, minWidth) >= 0 && compareValues(height, minHeight) >= 0
        }.map {
            it.groups[1]?.value
        }.firstOrNull {
            urlValidator.isValid(it) && it?.let { url -> checkIfUrlHasAcceptableHttpResponse(url) } == true
        }
    }

    fun saveImageToDatabase(query: String, imageLink: String?) {
        val toSave = ProductImage(query, imageLink)
        log.info("Saving image for {} to database", query)
        productImageRepository.save(toSave)
    }

    data class SearchResult(val items: List<ResultItem>?)

    data class ResultItem(val title: String?, val link: String?, val fileFormat: String?, val image: Image)

    data class Image(val height: Double, val width: Double)

}