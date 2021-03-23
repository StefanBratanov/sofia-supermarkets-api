package com.stefata.sofiasupermarketsapi.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.stefata.sofiasupermarketsapi.common.readResource
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Api(tags = ["Supermarket"], description = "All operations for supermarkets")
@RestController
class SupermarketController {

    private val objectMapper = jacksonObjectMapper()

    @ApiOperation("Get information for supermarkets")
    @GetMapping("/supermarkets")
    fun supermarkets(): List<SupermarketStaticData> {
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val resource = readResource("/static-data/supermarkets.json")
        val staticData: List<SupermarketStaticData> = objectMapper.readValue(resource)

        return staticData.map {
            it.copy(logo = baseUrl + it.logo)
        }
    }

    data class SupermarketStaticData(val name: String, val website: String, val logo: String)

}