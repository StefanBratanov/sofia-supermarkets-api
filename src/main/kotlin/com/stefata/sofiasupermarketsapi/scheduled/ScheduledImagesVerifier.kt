package com.stefata.sofiasupermarketsapi.scheduled

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.checkIfUrlHasAcceptableHttpResponse
import com.stefata.sofiasupermarketsapi.image.GoogleImageSearch
import com.stefata.sofiasupermarketsapi.repository.ProductImageRepository
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Log
@Component
class ScheduledImagesVerifier(
    val googleImageSearch: GoogleImageSearch,
    val cacheManager: CacheManager,
    val productImageRepository: ProductImageRepository,
) {

    @Scheduled(cron = "\${image.verifier.cron}")
    fun verifyImages() {
        log.info("Scheduled to verify images")
        val productImagesCache = cacheManager.getCache("productImages")
            ?.nativeCache as MutableMap<String, String>?

        productImagesCache?.filterValues {
            !checkIfUrlHasAcceptableHttpResponse(it)
        }?.forEach {
            googleImageSearch.search(it.key, false)?.let { imageUrl ->
                log.info(
                    "Changing cached image for {} from {} to {}",
                    it.key, it.value, imageUrl
                )
                productImagesCache[it.key] = imageUrl
                productImageRepository.findById(it.key).ifPresent { dbRow ->
                    log.info(
                        "Changing database image for {} from {} to {}",
                        it.key, dbRow.url, imageUrl
                    )
                    dbRow.url = imageUrl
                    productImageRepository.save(dbRow)
                }
            }

        }

    }

}