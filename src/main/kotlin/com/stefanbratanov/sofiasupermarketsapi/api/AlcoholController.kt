package com.stefanbratanov.sofiasupermarketsapi.api

import com.stefanbratanov.sofiasupermarketsapi.api.AlcoholController.AlcoholCategory.*
import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.interfaces.CdnUploader
import com.stefanbratanov.sofiasupermarketsapi.interfaces.ImageSearch
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import com.stefanbratanov.sofiasupermarketsapi.model.ProductStore
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket.KAUFLAND
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket.TMARKET
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.Objects.isNull
import java.util.Objects.nonNull
import kotlin.text.RegexOption.IGNORE_CASE
import org.apache.logging.log4j.util.Strings
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Log
@Tag(name = "Product", description = "All operations for supermarket products")
@RestController
class AlcoholController(
  val productStoreController: ProductStoreController,
  val imageSearch: ImageSearch,
  val cdnUploader: CdnUploader,
) {

  private val bira = "Бира"
  private val vino = "Вино"
  private val cider = "Сайдер"

  private val tMarketCategoryRegexes =
    listOf(bira, vino, "(?<!без)алкохолни", cider).map { it.toRegex(IGNORE_CASE) }

  private val tMarketCategoryResolver =
    mapOf(
        Beer to bira,
        Wine to vino,
        Other to cider,
      )
      .mapValues { it.value.toRegex(IGNORE_CASE) }

  private val kauflandDrinksCategoryRegex = "Алкохол".toRegex(IGNORE_CASE)

  private val alcoholCategoryResolvers =
    mapOf(
        Beer to listOf("бира", "beer", "^пиво\\s+", "\\s+пиво\\s+", "\\s+пиво\$"),
        Wine to
          listOf(
            "вино",
            "^пино\\s+",
            "\\s+пино\\s+",
            "\\s+пино\$",
            "каберне",
            "мерло",
            "шардоне",
            "^бордо\\s+",
            "\\s+бордо\\s+",
            "\\s+бордо\$",
            "^розе\\s+",
            "\\s+розе\\s+",
            "\\s+розе\$",
            "винена\\s+основа",
          ),
        Rakia to listOf("(?<!т)ракия", "спиртна"),
        Vodka to listOf("водка", "vodka"),
        Whiskey to listOf("уиски", "whiskey", "jack\\s+daniels", "bushmills", "бърбън"),
        Other to
          listOf(
            "(?<!\\sс\\s)узо",
            "\\s+мента",
            "мента\\s+",
            "ликьор",
            "^ром\\s+",
            "\\s+ром\\s+",
            "\\s+ром\$",
            "текила",
            "бренди",
            "коняк",
            "абсент",
            "(?<!вър)джин(?!джи)",
            "Пастис",
            "анасон.*напитк",
            "мастика",
            "сайдер",
            "somersby",
            "вермут",
            "martini",
            "мартини",
          ),
      )
      .mapValues { it.value.map { regex -> regex.toRegex(IGNORE_CASE) } }

  private val ignoreContains =
    listOf(
        "бонбони",
        "шоколад",
        "чаш(а|и) (.+)?за\\s+",
        "халба",
        "дезинфектант",
        "чай\\s+",
        "\\s+чай",
        "\\p{IsCyrillic}+бира",
        "бира\\p{IsCyrillic}+",
        "подправка",
        "cappy",
        "препарат",
        "диспенсър",
        "асортимента"
      )
      .map { it.toRegex(IGNORE_CASE) }

  @Operation(summary = "Get all alcohol products from supermarkets")
  @GetMapping("/products/alcohol")
  fun alcohol(
    productCriteria: ProductCriteria,
    @Parameter(description = "Get only certain category/ies")
    @RequestParam(required = false)
    category: List<String>?,
    @Parameter(description = "Getting the cdn url of the custom searched images")
    @RequestParam(required = false, defaultValue = "true")
    useCdn: Boolean,
  ): List<ProductStore> {
    return productStoreController
      .products(productCriteria)
      .map {
        when (it.supermarket) {
          TMARKET.title -> {
            processTMarketProducts(it)
          }
          KAUFLAND.title -> {
            processKauflandProducts(it)
          }
          else -> {
            val filteredProducts =
              it.products?.mapNotNull { product -> alcoholProductOrNull(product) }
            it.copy(products = filteredProducts)
          }
        }
      }
      .map {
        val filteredAndDistinctProducts =
          it.products
            // filtered by category query parameter
            ?.filter { product ->
              if (category.isNullOrEmpty()) {
                true
              } else {
                category.any { c -> c.equals(product.category, ignoreCase = true) }
              }
            }
            // filter regexes
            ?.filter { ignoreContains.none { regex -> it.name.contains(regex) } }
            // remove same products
            ?.distinctBy { pr ->
              pr.copy(
                validFrom = null,
                validUntil = null,
                picUrl = null,
              )
            }
        it.copy(products = filteredAndDistinctProducts)
      }
      .map { addPics(it, useCdn) }
  }

  private fun processTMarketProducts(productStore: ProductStore): ProductStore {
    val filteredProducts =
      productStore.products?.filter { product ->
        tMarketCategoryRegexes.any { regex -> product.category?.contains(regex) == true }
      }
    val categorizedProducts =
      filteredProducts?.mapNotNull { product ->
        val maybeCategory =
          tMarketCategoryResolver.entries
            .firstOrNull { entry -> product.name.contains(entry.value) }
            ?.key
        if (isNull(maybeCategory)) {
          alcoholProductOrNull(product, defaultCategory = Other)
        } else {
          product.copy(category = maybeCategory?.name)
        }
      }
    return productStore.copy(products = categorizedProducts)
  }

  private fun processKauflandProducts(productStore: ProductStore): ProductStore {
    val filteredProducts =
      productStore.products
        ?.filter { product ->
          product.category?.contains(kauflandDrinksCategoryRegex) == true ||
            // products in additional pages have blank category
            product.category?.isBlank() == true
        }
        ?.mapNotNull { product -> alcoholProductOrNull(product) }

    return productStore.copy(products = filteredProducts)
  }

  private fun addPics(productStore: ProductStore, useCdn: Boolean): ProductStore {
    val productsWithPics =
      productStore.products?.map { product ->
        if (Strings.isBlank(product.picUrl)) {
          val quantityOrEmpty = product.quantity.orEmpty()
          val productKey = "${product.name} $quantityOrEmpty".trim()
          val picUrl = imageSearch.search(productKey)
          if (useCdn && nonNull(picUrl)) {
            try {
              val cdnUrl = cdnUploader.upload(productKey, picUrl!!)
              product.copy(picUrl = cdnUrl)
            } catch (ex: Exception) {
              log.error("Error while uploading to CDN. Will fallback to Google search result", ex)
              product.copy(picUrl = picUrl)
            }
          } else {
            product.copy(picUrl = picUrl)
          }
        } else {
          product
        }
      }
    return productStore.copy(products = productsWithPics)
  }

  private fun alcoholProductOrNull(
    product: Product,
    defaultCategory: AlcoholCategory? = null,
  ): Product? {
    val maybeCategory =
      alcoholCategoryResolvers.entries
        .firstOrNull { categoryResolver ->
          categoryResolver.value.any { regex -> product.name.contains(regex) }
        }
        ?.key
    return if (isNull(maybeCategory)) {
      defaultCategory?.let { product.copy(category = it.name) }
    } else {
      product.copy(category = maybeCategory?.name)
    }
  }

  private enum class AlcoholCategory {
    Beer,
    Wine,
    Rakia,
    Vodka,
    Whiskey,
    Other
  }
}
