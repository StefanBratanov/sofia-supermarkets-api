package com.stefanbratanov.sofiasupermarketsapi.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class ProductImage(
  @Id @Column(columnDefinition = "TEXT") val product: String,
  @Column(columnDefinition = "TEXT") var url: String?,
)
