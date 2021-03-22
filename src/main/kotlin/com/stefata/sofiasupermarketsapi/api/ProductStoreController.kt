package com.stefata.sofiasupermarketsapi.api

import com.stefata.sofiasupermarketsapi.model.ProductStore
import com.stefata.sofiasupermarketsapi.repository.ProductStoreRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductStoreController(
    val productStoreRepository: ProductStoreRepository
) {

    @GetMapping("/products")
    fun products(): List<ProductStore> {
        return productStoreRepository.findAll().toList()
    }
}