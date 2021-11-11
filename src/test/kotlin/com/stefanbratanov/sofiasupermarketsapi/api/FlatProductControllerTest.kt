package com.stefanbratanov.sofiasupermarketsapi.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket
import com.stefanbratanov.sofiasupermarketsapi.readResource
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(
    controllers = [FlatProductController::class],
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [ApiConfig::class])]
)
internal class FlatProductControllerTest(@Autowired val mockMvc: MockMvc) {

    private val objectMapper = jacksonObjectMapper()

    @MockkBean
    private lateinit var alcoholController: AlcoholController

    @MockkBean
    private lateinit var supermarketController: SupermarketController

    @Test
    fun `test getting alcohol products`() {

        val alcohols = readResource("/api/alcohol/expected.json")
        val beer = readResource("/api/alcohol/only-beer.json")
        val expectedJson = readResource("/api/flat/expected.json")
        val expectedBeer = readResource("/api/flat/expected-beer.json")

        val supermarketStaticData = Supermarket.values().map {
            SupermarketController.SupermarketStaticData(
                it.title, "http://www.test.bg", "http://${it.title.toLowerCase()}.bg"
            )
        }

        every { alcoholController.alcohol(any(), null, true) } returns objectMapper.readValue(alcohols)
        every { alcoholController.alcohol(any(), listOf("beer"), true) } returns objectMapper.readValue(beer)
        every { supermarketController.supermarkets() } returns supermarketStaticData

        mockMvc.perform(MockMvcRequestBuilders.get("/products/flat/alcohol").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(expectedJson, false))

        mockMvc.perform(MockMvcRequestBuilders.get("/products/flat/alcohol").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(expectedJson, false))

        mockMvc.perform(
            MockMvcRequestBuilders.get("/products/flat/alcohol?category=beer")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(expectedBeer, false))

        val alcohols2 = readResource("/api/alcohol/expected-with-duplicates.json")
        every { alcoholController.alcohol(any(), null, true) } returns objectMapper.readValue(alcohols2)

        mockMvc.perform(MockMvcRequestBuilders.get("/products/flat/alcohol").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(expectedJson, false))

    }
}