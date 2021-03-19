package com.stefata.sofiasupermarketsapi

import com.stefata.sofiasupermarketsapi.model.Product
import kotlin.random.Random

fun getProduct(name: String): Product {
    return Product(
        name = name,
        price = Random.nextDouble(),
        oldPrice = null
    )
}

fun getProduct(name: String, price: Double): Product {
    return Product(
        name = name,
        price = price,
        oldPrice = null
    )
}