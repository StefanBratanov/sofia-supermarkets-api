package com.stefata.sofiasupermarketsapi.api

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ninjasquad.springmockk.MockkBean
import com.stefata.sofiasupermarketsapi.readResource
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest
internal class AlcoholControllerTest(@Autowired val mockMvc: MockMvc) {

    private val objectMapper = jacksonObjectMapper()

    @MockkBean
    private lateinit var productStoreController: ProductStoreController

    @Test
    fun `test getting alcohol products`() {

        val inputJson = readResource("/api/alcohol/input.json")
        val expectedJson = readResource("/api/alcohol/expected.json")

        every { productStoreController.products(any()) } returns objectMapper.readValue(inputJson)

        mockMvc.perform(MockMvcRequestBuilders.get("/products/alcohol").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(expectedJson, false))

    }
}