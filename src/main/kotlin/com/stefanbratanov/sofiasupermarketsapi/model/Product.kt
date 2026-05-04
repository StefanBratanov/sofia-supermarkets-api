package com.stefanbratanov.sofiasupermarketsapi.model

import java.time.LocalDate
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer

data class Product(
  val name: String,
  val quantity: String? = null,
  val price: Double?,
  val oldPrice: Double?,
  val category: String? = null,
  val picUrl: String? = null,
  @JsonDeserialize(using = LocalDateDeserializer::class)
  @JsonSerialize(using = LocalDateSerializer::class)
  val validFrom: LocalDate? = null,
  @JsonDeserialize(using = LocalDateDeserializer::class)
  @JsonSerialize(using = LocalDateSerializer::class)
  val validUntil: LocalDate? = null,
)
