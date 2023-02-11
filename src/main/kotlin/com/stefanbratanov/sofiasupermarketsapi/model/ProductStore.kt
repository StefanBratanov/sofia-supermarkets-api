package com.stefanbratanov.sofiasupermarketsapi.model

import java.util.*
import org.springframework.data.annotation.Id
import org.springframework.data.keyvalue.annotation.KeySpace

@KeySpace("ProductStore")
data class ProductStore(
  @Id val supermarket: String,
  val updatedAt: Date? = Date(),
  val products: List<Product>? = emptyList(),
)
