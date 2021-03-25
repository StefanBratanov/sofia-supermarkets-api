package com.stefata.sofiasupermarketsapi.repository

import com.stefata.sofiasupermarketsapi.model.ProductImage
import org.springframework.data.jpa.repository.JpaRepository

interface ProductImageRepository : JpaRepository<ProductImage, String>