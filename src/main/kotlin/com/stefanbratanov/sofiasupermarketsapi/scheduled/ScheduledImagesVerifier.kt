package com.stefanbratanov.sofiasupermarketsapi.scheduled

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.common.checkIfUrlHasAcceptableHttpResponse
import com.stefanbratanov.sofiasupermarketsapi.image.GoogleImageSearch
import com.stefanbratanov.sofiasupermarketsapi.repository.ProductImageRepository
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Log
@Component
class ScheduledImagesVerifier(
    val googleImageSearch: GoogleImageSearch,
    val cacheManager: CacheManager,
    val productImageRepository: ProductImageRepository
) {

    @Scheduled(cron = "\${image.verifier.cron}")
    @Suppress("UNCHECKED_CAST")
    fun verifyImages() {
        log.info("Scheduled to verify images")
        val productImagesCache = cacheManager.getCache("productImages")
            ?.nativeCache as MutableMap<String?, Any?>?

        productImagesCache?.filterValues {
            it is String? && !checkIfUrlHasAcceptableHttpResponse(it)
        }?.forEach {
            it.key?.let { key ->
                googleImageSearch.search(key, false)?.let { imageUrl ->
                    log.info(
                        "Changing cached image for {} from {} to {}",
                        key,
                        it.value,
                        imageUrl
                    )
                    productImagesCache[key] = imageUrl
                    productImageRepository.findById(key).ifPresent { dbRow ->
                        log.info(
                            "Changing database image for {} from {} to {}",
                            key,
                            dbRow.url,
                            imageUrl
                        )
                        dbRow.url = imageUrl
                        productImageRepository.save(dbRow)
                    }
                }
            }
        }
    }
}
