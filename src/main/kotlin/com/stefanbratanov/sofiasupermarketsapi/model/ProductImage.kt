package com.stefanbratanov.sofiasupermarketsapi.model

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class ProductImage(
    @Id val product: String,
    var url: String?,
)
