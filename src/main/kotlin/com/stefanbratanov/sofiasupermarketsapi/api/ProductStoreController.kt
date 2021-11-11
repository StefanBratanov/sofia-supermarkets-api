package com.stefanbratanov.sofiasupermarketsapi.api

import com.stefanbratanov.sofiasupermarketsapi.model.ProductStore
import com.stefanbratanov.sofiasupermarketsapi.repository.ProductStoreRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Objects.nonNull

@Tag(name = "Product", description = "All operations for supermarket products")
@RestController
class ProductStoreController(
    val productStoreRepository: ProductStoreRepository
) {

    @Operation(summary = "Get all products from supermarkets")
    @GetMapping("/products")
    fun products(productCriteria: ProductCriteria): List<ProductStore> {

        val productStores = if (productCriteria.supermarket.isNullOrEmpty()) {
            productStoreRepository.findAll().toList()
        } else {
            productStoreRepository.findAll().filter { productStoreEntry ->
                productCriteria.supermarket!!.any {
                    it.equals(productStoreEntry.supermarket, ignoreCase = true)
                }
            }
        }

        if (productCriteria.offers) {
            return productStores.map {
                val offerProducts = it.products?.filter { product ->
                    nonNull(product.oldPrice) && product.oldPrice?.equals(product.price) == false
                }
                it.copy(products = offerProducts)
            }
        }
        return productStores
    }
}