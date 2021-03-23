package com.stefata.sofiasupermarketsapi.api

import com.stefata.sofiasupermarketsapi.model.ProductStore
import com.stefata.sofiasupermarketsapi.repository.ProductStoreRepository
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Api(tags = ["Product"], description = "All operations for supermarket products")
@RestController
class ProductStoreController(
    val productStoreRepository: ProductStoreRepository
) {

    @ApiOperation(value = "Get all products from supermarkets")
    @GetMapping("/products")
    fun products(productCriteria: ProductCriteria): List<ProductStore> {
        if (productCriteria.supermarkets.isNullOrEmpty()) {
            return productStoreRepository.findAll().toList()
        }
        return productStoreRepository.findAll().filter { productStoreEntry ->
            productCriteria.supermarkets!!.any {
                it.equals(productStoreEntry.supermarket, ignoreCase = true)
            }
        }
    }
}