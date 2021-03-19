package com.stefata.sofiasupermarketsapi.model

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.util.*

@RedisHash("SupermarketStore")
data class SupermarketStore(
    @Id val supermarket: String,
    @CreatedDate var created: Date = Date(),
    val products: List<Product>? = emptyList()
)