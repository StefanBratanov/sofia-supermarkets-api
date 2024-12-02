package com.stefanbratanov.sofiasupermarketsapi.image

import com.cloudinary.Cloudinary
import com.cloudinary.EagerTransformation
import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.interfaces.CdnUploader
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Log
@Component
class CloudinaryCdnUploader(private val cloudinary: Cloudinary) : CdnUploader {

  @Cacheable("cdnImages")
  @Suppress("UNCHECKED_CAST")
  override fun upload(key: String, url: String): String {
    val imageKey = if (key.length >= 100) key.substring(0, 100) else key
    val search = cloudinary.search()
    val searchResult =
      search.expression("metadata.image_key=\"${imageKey}\"").execute()["resources"]
        as List<Map<Any, Any>>

    if (searchResult.isNotEmpty()) {
      log.info("Found a result for {} in CDN", key)
      return searchResult.first()["secure_url"] as String
    }

    log.info("Uploading image for {} to CDN", key)

    return cloudinary
      .uploader()
      .upload(
        url,
        mapOf(
          "metadata" to "image_key=$imageKey",
          "folder" to "alcohol",
          "transformation" to EagerTransformation().responsiveWidth(true).height(160),
        ),
      )["secure_url"]
      as String
  }
}
