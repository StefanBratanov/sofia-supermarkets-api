package com.stefanbratanov.sofiasupermarketsapi

import com.stefanbratanov.sofiasupermarketsapi.model.Product
import kotlin.random.Random
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper

fun getProduct(name: String): Product {
  return Product(name = name, price = Random.nextDouble(), oldPrice = null)
}

fun getProduct(name: String, price: Double): Product {
  return Product(name = name, price = price, oldPrice = null)
}

fun testObjectMapper(): ObjectMapper {
  return JsonMapper.builder().disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS).build()
}
