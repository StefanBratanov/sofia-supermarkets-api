package com.stefata.sofiasupermarketsapi.api

import com.stefata.sofiasupermarketsapi.model.ProductStore
import com.stefata.sofiasupermarketsapi.repository.ProductStoreRepository
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["Product"], description = "All operations for supermarket products")
@RestController
class ProductStoreController(
    val productStoreRepository: ProductStoreRepository
) {

    @ApiOperation(value = "Get all products from supermarkets")
    @GetMapping("/products")
    fun products(
        @ApiParam(
            value = "list of supermarkets to get the products from"
        )
        @RequestParam(required = false) supermarkets: List<String>?
    ): List<ProductStore> {
        if (supermarkets.isNullOrEmpty()) {
            return productStoreRepository.findAll().toList()
        }
        return productStoreRepository.findAll().filter { productStoreEntry ->
            supermarkets.any {
                it.equals(productStoreEntry.supermarket, ignoreCase = true)
            }
        }
    }
}