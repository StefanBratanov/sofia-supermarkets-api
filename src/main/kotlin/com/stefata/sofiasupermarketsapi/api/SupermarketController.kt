package com.stefata.sofiasupermarketsapi.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Tag(name = "Supermarket", description = "All operations for supermarkets")
@RestController
class SupermarketController {

    private val objectMapper = jacksonObjectMapper()

    @Operation(summary = "Get information for supermarkets", deprecated = true)
    @GetMapping("/supermarkets")
    fun supermarkets(): List<SupermarketStaticData> {
        val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val resource = ClassPathResource("/static-data/supermarkets.json")

        return resource.inputStream.use { inputStream ->
            val staticData: List<SupermarketStaticData> = objectMapper.readValue(inputStream)

            staticData.map {
                it.copy(logo = baseUrl + it.logo)
            }
        }
    }

    data class SupermarketStaticData(val name: String, val website: String, val logo: String)

}