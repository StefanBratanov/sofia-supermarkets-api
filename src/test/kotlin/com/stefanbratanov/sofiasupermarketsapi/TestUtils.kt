package com.stefanbratanov.sofiasupermarketsapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import kotlin.random.Random

fun getProduct(name: String): Product {
  return Product(
    name = name,
    price = Random.nextDouble(),
    oldPrice = null,
  )
}

fun getProduct(name: String, price: Double): Product {
  return Product(
    name = name,
    price = price,
    oldPrice = null,
  )
}

fun testObjectMapper(): ObjectMapper {
  return ObjectMapper()
    .registerModule(JavaTimeModule())
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
}
