package com.stefata.sofiasupermarketsapi.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Objects.isNull
import kotlin.math.absoluteValue

@Api(tags = ["Product"], description = "All operations for supermarket products")
@RestController
class FlatProductController(
    val alcoholController: AlcoholController,
    val supermarketController: SupermarketController
) {

    @ApiOperation(value = "Get all alcohol products from supermarkets in a flat format")
    @GetMapping("/products/flat/alcohol")
    fun alcoholFlat(productCriteria: ProductCriteria): List<FlatProduct> {
        val alcoholProductStore = alcoholController.alcohol(productCriteria)
        val supermarkets = supermarketController.supermarkets()

        return alcoholProductStore.flatMap {
            val supermarketStaticData = supermarkets.firstOrNull { supermarket ->
                supermarket.name == it.supermarket
            }

            it.products?.map { product ->
                FlatProduct(
                    supermarket = it.supermarket,
                    logo = supermarketStaticData?.logo,
                    name = product.name,
                    quantity = product.quantity,
                    price = product.price,
                    oldPrice = product.oldPrice,
                    discount = getDiscount(product.price, product.oldPrice),
                    category = product.category,
                    picUrl = product.picUrl
                )
            }!!.toList()
        }
    }

    data class FlatProduct(
        val supermarket: String?, val logo: String?, val name: String, val quantity: String?,
        val price: Double?, val oldPrice: Double?, val discount: Int?,
        val category: String?, val picUrl: String?
    )

    fun getDiscount(price: Double?, oldPrice: Double?): Int? {
        return price?.takeUnless {
            isNull(oldPrice)
        }?.let {
            ((1 - (price.div(oldPrice!!))) * 100).toInt().absoluteValue
        }
    }
}