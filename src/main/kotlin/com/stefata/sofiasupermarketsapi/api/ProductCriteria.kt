package com.stefata.sofiasupermarketsapi.api

import io.swagger.v3.oas.annotations.Parameter
import org.springdoc.api.annotations.ParameterObject

@ParameterObject
data class ProductCriteria(
    @Parameter(description = "Supermarkets to get the products from")
    var supermarket: List<String>?,
    @Parameter(description = "Show only offers")
    var offers: Boolean = false
)
