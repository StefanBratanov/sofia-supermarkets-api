package com.stefanbratanov.sofiasupermarketsapi.repository

import com.stefanbratanov.sofiasupermarketsapi.model.ProductImage
import org.springframework.data.jpa.repository.JpaRepository

interface ProductImageRepository : JpaRepository<ProductImage, String>
