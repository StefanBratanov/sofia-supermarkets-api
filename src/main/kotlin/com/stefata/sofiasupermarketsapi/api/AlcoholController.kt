package com.stefata.sofiasupermarketsapi.api

import com.stefata.sofiasupermarketsapi.api.AlcoholController.AlcoholCategory.*
import com.stefata.sofiasupermarketsapi.interfaces.ImageSearch
import com.stefata.sofiasupermarketsapi.model.Product
import com.stefata.sofiasupermarketsapi.model.ProductStore
import com.stefata.sofiasupermarketsapi.model.Supermarket.KAUFLAND
import com.stefata.sofiasupermarketsapi.model.Supermarket.TMARKET
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.apache.logging.log4j.util.Strings
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Objects.isNull
import kotlin.text.RegexOption.IGNORE_CASE

@Api(tags = ["Product"], description = "All operations for supermarket products")
@RestController
class AlcoholController(
    val productStoreController: ProductStoreController,
    val imageSearch: ImageSearch
) {

    private val bira = "Бира"
    private val vino = "Вино"

    private val tMarketCategoryRegexes = listOf(bira, vino, "(?<!без)алкохолни").map {
        it.toRegex(IGNORE_CASE)
    }

    private val tMarketCategoryResolver = mapOf(
        Beer to bira,
        Wine to vino
    ).mapValues { it.value.toRegex(IGNORE_CASE) }

    private val kauflandDrinksCategoryRegex = "Алкохол".toRegex(IGNORE_CASE)

    private val alcoholCategoryResolvers = mapOf(
        Beer to listOf("бира", "beer", "^пиво\\s+", "\\s+пиво\\s+", "\\s+пиво\$"),
        Wine to listOf(
            "вино", "^пино\\s+", "\\s+пино\\s+", "\\s+пино\$", "каберне", "мерло",
            "шардоне", "^бордо\\s+", "\\s+бордо\\s+", "\\s+бордо\$", "^розе\\s+", "\\s+розе\\s+", "\\s+розе\$",
            "винена\\s+основа"
        ),
        Rakia to listOf("ракия", "спиртна"),
        Vodka to listOf("водка", "vodka"),
        Whiskey to listOf("уиски", "whiskey", "jack\\s+daniels", "bushmills", "бърбън"),
        Other to listOf(
            "(?<!\\sс\\s)узо", "\\s+мента", "мента\\s+",
            "ликьор", "^ром\\s+", "\\s+ром\\s+", "\\s+ром\$", "текила", "бренди", "коняк", "абсент",
            "(?<!вър)джин", "Пастис"
        )
    ).mapValues {
        it.value.map { regex ->
            regex.toRegex(IGNORE_CASE)
        }
    }

    private val ignoreContains = listOf("бонбони", "шоколад").map {
        it.toRegex(IGNORE_CASE)
    }

    @ApiOperation(value = "Get all alcohol products from supermarkets")
    @GetMapping("/products/alcohol")
    fun alcohol(
        productCriteria: ProductCriteria,
        @ApiParam(value = "Get only certain category/ies") @RequestParam(required = false)
        category: List<String>?
    ): List<ProductStore> {

        return productStoreController.products(productCriteria).map {

            when (it.supermarket) {
                TMARKET.title -> {
                    val filteredProducts = it.products?.filter { product ->
                        tMarketCategoryRegexes.any { regex ->
                            product.category?.contains(regex) == true
                        }
                    }
                    val categorizedProducts = filteredProducts?.mapNotNull { product ->
                        val maybeCategory = tMarketCategoryResolver.entries.firstOrNull { entry ->
                            product.name.contains(entry.value)
                        }?.key
                        if (isNull(maybeCategory)) {
                            alcoholProductOrNull(product)
                        } else {
                            product.copy(category = maybeCategory?.name)
                        }
                    }
                    it.copy(products = categorizedProducts)
                }
                KAUFLAND.title -> {
                    val filteredProducts = it.products?.filter { product ->
                        product.category?.contains(kauflandDrinksCategoryRegex) == true
                    }?.mapNotNull { product ->
                        alcoholProductOrNull(product)
                    }

                    it.copy(products = filteredProducts)
                }
                else -> {
                    val filteredProducts = it.products?.mapNotNull { product ->
                        alcoholProductOrNull(product)
                    }
                    it.copy(products = filteredProducts)
                }
            }

        }.map {
            val filteredProducts = it.products?.filter { product ->
                if (category.isNullOrEmpty()) {
                    true
                } else {
                    category.any { c ->
                        c.equals(product.category, ignoreCase = true)
                    }
                }
            }?.filter {
                ignoreContains.none { regex ->
                    it.name.contains(regex)
                }
            }
            it.copy(products = filteredProducts)
        }.map {
            val productsWithPics = it.products?.map { product ->
                if (Strings.isBlank(product.picUrl)) {
                    val picUrl = imageSearch.search("${product.name} ${product.quantity}")
                    product.copy(picUrl = picUrl)
                } else {
                    product
                }
            }
            it.copy(products = productsWithPics)
        }

    }

    private fun alcoholProductOrNull(product: Product): Product? {
        val maybeCategory = alcoholCategoryResolvers.entries.firstOrNull { categoryResolver ->
            categoryResolver.value.any { regex ->
                product.name.contains(regex)
            }
        }?.key
        return if (isNull(maybeCategory)) {
            null
        } else {
            product.copy(category = maybeCategory?.name)
        }
    }

    private enum class AlcoholCategory {
        Beer, Wine, Rakia, Vodka, Whiskey, Other
    }
}