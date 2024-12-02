package com.stefanbratanov.sofiasupermarketsapi.image

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ImageConfig {

  @Bean
  fun cloudinary(
    @Value("\${cloudinary.cloud.name}") cloudName: String,
    @Value("\${cloudinary.api.key}") apiKey: String,
    @Value("\${cloudinary.api.secret}") apiSecret: String,
  ): Cloudinary {
    return Cloudinary(
      ObjectUtils.asMap("cloud_name", cloudName, "api_key", apiKey, "api_secret", apiSecret)
    )
  }
}
