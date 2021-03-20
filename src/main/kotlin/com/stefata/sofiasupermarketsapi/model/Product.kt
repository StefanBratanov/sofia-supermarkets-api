package com.stefata.sofiasupermarketsapi.model

data class Product(
    val name: String,
    val quantity: String? = null,
    val price: Double?,
    val oldPrice: Double?,
    val category: String? = null,
    val picUrl: String? = null
)
