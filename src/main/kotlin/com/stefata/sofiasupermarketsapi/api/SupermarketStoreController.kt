package com.stefata.sofiasupermarketsapi.api

import com.stefata.sofiasupermarketsapi.model.SupermarketStore
import com.stefata.sofiasupermarketsapi.repository.SupermarketStoreRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SupermarketStoreController(
    val supermarketStoreRepository: SupermarketStoreRepository
) {

    @GetMapping("/supermarkets")
    fun supermarkets(): List<SupermarketStore> {
        return supermarketStoreRepository.findAll().toList()
    }
}