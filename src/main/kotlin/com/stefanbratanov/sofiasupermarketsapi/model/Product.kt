package com.stefanbratanov.sofiasupermarketsapi.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import java.time.LocalDate

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
