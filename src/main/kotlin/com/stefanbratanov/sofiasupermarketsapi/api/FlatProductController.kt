package com.stefanbratanov.sofiasupermarketsapi.api

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import java.time.LocalDate
import java.util.Objects.isNull
import kotlin.math.absoluteValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Product", description = "All operations for supermarket products")
@RestController
@Log
class FlatProductController(val alcoholController: AlcoholController) {

  private val acceptableDiscount = 75

  @Operation(summary = "Get all alcohol products from supermarkets in a flat format")
  @GetMapping("/products/flat/alcohol")
  fun alcoholFlat(
    productCriteria: ProductCriteria,
    @Parameter(description = "Get only certain category/ies")
    @RequestParam(required = false)
    category: List<String>?,
    @Parameter(description = "Getting the cdn url of the custom searched images")
    @RequestParam(required = false, defaultValue = "true")
    useCdn: Boolean,
  ): List<FlatProduct> {
    val alcoholProductStore = alcoholController.alcohol(productCriteria, category, useCdn)

    return alcoholProductStore.flatMap {
      it.products
        ?.map { product ->
          FlatProduct(
            supermarket = it.supermarket,
            name = product.name,
            quantity = product.quantity,
            price = product.price,
            oldPrice = product.oldPrice,
            discount = getDiscount(product.price, product.oldPrice),
            category = product.category,
            picUrl = product.picUrl,
            validFrom = product.validFrom,
            validUntil = product.validUntil,
          )
        }
        .orEmpty()
        .filter { product ->
          val isAcceptableDiscount =
            product.oldPrice == null ||
              product.discount?.let { discount -> discount < acceptableDiscount } == true
          if (!isAcceptableDiscount) {
            log.warn(
              "{} is not an acceptable discount. Will ignore {}",
              product.discount,
              product.name,
            )
          }
          isAcceptableDiscount
        }
        .distinct()
        .toList()
    }
  }

  data class FlatProduct(
    val supermarket: String?,
    val name: String,
    val quantity: String?,
    val price: Double?,
    val oldPrice: Double?,
    val discount: Int?,
    val category: String?,
    val picUrl: String?,
    @JsonDeserialize(using = LocalDateDeserializer::class)
    @JsonSerialize(using = LocalDateSerializer::class)
    val validFrom: LocalDate? = null,
    @JsonDeserialize(using = LocalDateDeserializer::class)
    @JsonSerialize(using = LocalDateSerializer::class)
    val validUntil: LocalDate? = null,
  )

  fun getDiscount(price: Double?, oldPrice: Double?): Int? {
    return price
      ?.takeUnless { isNull(oldPrice) }
      ?.let { ((1 - (price.div(oldPrice!!))) * 100).toInt().absoluteValue }
  }
}
