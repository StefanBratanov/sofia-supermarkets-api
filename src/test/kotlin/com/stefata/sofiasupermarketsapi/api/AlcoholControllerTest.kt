package com.stefata.sofiasupermarketsapi.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import com.stefata.sofiasupermarketsapi.interfaces.ImageSearch
import com.stefata.sofiasupermarketsapi.readResource
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(controllers = [AlcoholController::class])
internal class AlcoholControllerTest(@Autowired val mockMvc: MockMvc) {

    private val objectMapper = jacksonObjectMapper()

    @MockkBean
    private lateinit var productStoreController: ProductStoreController

    @MockkBean
    private lateinit var imageSearch: ImageSearch

    @Test
    fun `test getting alcohol products`() {

        val inputJson = readResource("/api/alcohol/input.json")
        val expectedJson = readResource("/api/alcohol/expected.json")

        every { imageSearch.search(any()) } returns "http://www.foo.bar"
        every { productStoreController.products(any()) } returns objectMapper.readValue(inputJson)

        mockMvc.perform(MockMvcRequestBuilders.get("/products/alcohol").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(expectedJson, false))

        val onlyBeer = readResource("/api/alcohol/only-beer.json")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/products/alcohol?category=beer")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(onlyBeer, false))

        val beerAndWhiskey = readResource("/api/alcohol/beer-and-whiskey.json")

        mockMvc.perform(
            MockMvcRequestBuilders.get("/products/alcohol?category=beer,whiskey")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(beerAndWhiskey, false))

    }
}