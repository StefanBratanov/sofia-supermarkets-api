package com.stefanbratanov.sofiasupermarketsapi.image

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class CloudinaryCdnUploaderTest {

    private lateinit var underTest: CloudinaryCdnUploader

    @Test
    @Disabled("too lazy to mock this")
    fun `test uploading image to cdn`() {
        val cloudinary = Cloudinary(
            ObjectUtils.asMap(
                "cloud_name",
                "",
                "api_key",
                "",
                "api_secret",
                ""
            )
        )

        underTest = CloudinaryCdnUploader(cloudinary)

        val cdnUrl =
            underTest.upload("test", "http://margo2013.com/image/cache/margo/Vino/04181-600x315.jpg")

        print(cdnUrl)
    }
}
