package com.stefata.sofiasupermarketsapi.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import com.stefata.sofiasupermarketsapi.model.Supermarket
import com.stefata.sofiasupermarketsapi.readResource
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(controllers = [FlatProductController::class])
internal class FlatProductControllerTest(@Autowired val mockMvc: MockMvc) {

    private val objectMapper = jacksonObjectMapper()

    @MockkBean
    private lateinit var alcoholController: AlcoholController

    @MockkBean
    private lateinit var supermarketController: SupermarketController

    @Test
    fun `test getting alcohol products`() {

        val alcohols = readResource("/api/alcohol/expected.json")
        val expectedJson = readResource("/api/flat/expected.json")

        val supermarketStaticData = Supermarket.values().map {
            SupermarketController.SupermarketStaticData(
                it.title, "http://www.test.bg", "http://${it.title.toLowerCase()}.bg"
            )
        }

        every { alcoholController.alcohol(any()) } returns objectMapper.readValue(alcohols)
        every { supermarketController.supermarkets() } returns supermarketStaticData

        mockMvc.perform(MockMvcRequestBuilders.get("/products/flat/alcohol").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(expectedJson, false))
    }
}