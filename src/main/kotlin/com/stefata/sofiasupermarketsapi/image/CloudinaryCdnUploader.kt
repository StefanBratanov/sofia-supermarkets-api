package com.stefata.sofiasupermarketsapi.image

import com.cloudinary.Cloudinary
import com.cloudinary.EagerTransformation
import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.interfaces.CdnUploader
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Log
@Component
class CloudinaryCdnUploader(
    private val cloudinary: Cloudinary
) : CdnUploader {

    @Cacheable("cdnImages")
    override fun upload(key: String, url: String): String {
        log.info("Uploading image for {} to CDN", key)

        return cloudinary.uploader().upload(
            url, mapOf(
                "tags" to arrayOf(key),
                "folder" to "alcohol",
                "transformation" to EagerTransformation()
                    .responsiveWidth(true)
                    .height(200)
            )
        )["url"] as String
    }

}