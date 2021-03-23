package com.stefata.sofiasupermarketsapi.api

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel
data class ProductCriteria(
    @ApiModelProperty(value = "list of supermarkets to get the products from", position = 1)
    var supermarkets: List<String>?,
    @ApiModelProperty(value = "Show only offers", position = 2)
    var offers: Boolean = false
)
