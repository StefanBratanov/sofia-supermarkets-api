package com.stefata.sofiasupermarketsapi.model

import org.springframework.data.annotation.Id
import org.springframework.data.keyvalue.annotation.KeySpace
import java.util.*

@KeySpace("ProductStore")
data class ProductStore(
    @Id val supermarket: String,
    val updatedAt: Date? = Date(),
    val products: List<Product>? = emptyList()
)