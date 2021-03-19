package com.stefata.sofiasupermarketsapi

import com.stefata.sofiasupermarketsapi.model.Product
import kotlin.random.Random

fun getProductWithName(name: String): Product {
    return Product(
        name = name,
        price = Random.nextDouble(),
        oldPrice = null
    )
}