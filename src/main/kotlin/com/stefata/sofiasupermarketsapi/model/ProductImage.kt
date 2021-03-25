package com.stefata.sofiasupermarketsapi.model

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class ProductImage(
    @Id val product: String,
    val url: String?
)
