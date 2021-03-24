package com.stefata.sofiasupermarketsapi.api

import com.stefata.sofiasupermarketsapi.model.ProductStore
import com.stefata.sofiasupermarketsapi.model.Supermarket.KAUFLAND
import com.stefata.sofiasupermarketsapi.model.Supermarket.TMARKET
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.text.RegexOption.IGNORE_CASE

@Api(tags = ["Product"])
@RestController
class AlcoholController(
    val productStoreController: ProductStoreController
) {

    private val tMarketCategoryRegexes = listOf("Бира", "Вино", "(?<!без)алкохолни").map {
        it.toRegex(IGNORE_CASE)
    }

    private val kauflandDrinksCategoryRegex = "Алкохол".toRegex(IGNORE_CASE)

    private val alcoholInNameRegex = listOf(
        "бира", "вино", "(?<!\\sс\\s)узо", "уиски",
        "\\s+мента", "мента\\s+", "ракия", "(?<!вър)джин", "пиво\\s+", "\\s+пиво",
        "водка", "ликьор", "каберне", "мерло", "пино\\s+", "\\s+пино",
        "шардоне", "бордо\\s+", "\\s+бордо", "розе\\s+", "\\s+розе",
        "ром\\s+", "\\s+ром", "текила", "бренди", "коняк", "абсент",
        "jack\\s+daniels", "bushmills", "винена\\s+основа", "спиртна", "бърбън",
        "beer", "whiskey", "vodka"
    ).map {
        it.toRegex(IGNORE_CASE)
    }

    @ApiOperation(value = "Get all alcohol products from supermarkets")
    @GetMapping("/products/alcohol")
    fun alcohol(productCriteria: ProductCriteria): List<ProductStore> {

        return productStoreController.products(productCriteria).map {

            when (it.supermarket) {
                TMARKET.title -> {
                    val filteredProducts = it.products?.filter { product ->
                        tMarketCategoryRegexes.any { regex ->
                            product.category?.contains(regex) == true
                        }
                    }
                    it.copy(products = filteredProducts)
                }
                KAUFLAND.title -> {
                    val filteredProducts = it.products?.filter { product ->
                        product.category?.contains(kauflandDrinksCategoryRegex) == true
                    }?.filter { product ->
                        alcoholInNameRegex.any { regex ->
                            product.name.contains(regex)
                        }
                    }
                    it.copy(products = filteredProducts)
                }
                else -> {
                    val filteredProducts = it.products?.filter { product ->
                        alcoholInNameRegex.any { regex ->
                            product.name.contains(regex)
                        }
                    }
                    it.copy(products = filteredProducts)
                }
            }

        }
    }
}