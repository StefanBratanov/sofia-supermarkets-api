package com.stefata.sofiasupermarketsapi.scheduled

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.common.checkIfUrlHasAcceptableHttpResponse
import com.stefata.sofiasupermarketsapi.image.GoogleImageSearch
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Log
@Component
class ScheduledImagesVerifier(
    val googleImageSearch: GoogleImageSearch,
    val cacheManager: CacheManager
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
                log.info("Changing cached image of {} from {} to {}", it.key, it.value, imageUrl)
                productImagesCache[it.key] = imageUrl
            }
        }

    }

}