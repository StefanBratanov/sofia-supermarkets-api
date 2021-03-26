package com.stefata.sofiasupermarketsapi.image

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.interfaces.ImageSearch
import com.stefata.sofiasupermarketsapi.model.ProductImage
import com.stefata.sofiasupermarketsapi.repository.ProductImageRepository
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.util.Objects.isNull

@Log
@Component
class GoogleImageSearch(
    @Value("\${google.custom.search.url}") private val googleSearchUrl: String,
    private val productImageRepository: ProductImageRepository,
    private val restTemplate: RestTemplate = RestTemplate()
) : ImageSearch {

    private val minWidth: Double = 250.0
    private val minHeight: Double = 250.0

    @Cacheable("productImages")
    override fun search(query: String): String? {

        val maybeProductImage = productImageRepository.findById(query)

        if (maybeProductImage.isPresent) {
            log.info("Retrieved image url for {} from database", query)
            return maybeProductImage.get().url
        }

        log.info("Querying for an image for {} from Google", query)

        val response = try {
            restTemplate.exchange(
                "$googleSearchUrl&q={query}", HttpMethod.GET, null,
                SearchResult::class.java, query
            )
        } catch (httpEx: HttpStatusCodeException) {
            log.error("Error querying google", httpEx)
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

        val toSave = ProductImage(query, imageLink)
        log.info("Saving image for {} to database", query)
        productImageRepository.save(toSave)

        return imageLink
    }

    data class SearchResult(val items: List<ResultItem>?)

    data class ResultItem(val title: String?, val link: String?, val fileFormat: String?, val image: Image)

    data class Image(val height: Double, val width: Double)

}