package com.stefata.sofiasupermarketsapi.api

import com.ninjasquad.springmockk.MockkBean
import com.stefata.sofiasupermarketsapi.getProduct
import com.stefata.sofiasupermarketsapi.model.SupermarketStore
import com.stefata.sofiasupermarketsapi.readResource
import com.stefata.sofiasupermarketsapi.repository.SupermarketStoreRepository
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
internal class SupermarketStoreControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var supermarketStoreRepository: SupermarketStoreRepository

    @Test
    fun `test getting data for supermarkets`() {
        every { supermarketStoreRepository.findAll() } returns listOf(
            SupermarketStore(supermarket = "foo", products = listOf(getProduct("hello", 1.1))),
            SupermarketStore(supermarket = "bar", products = listOf(getProduct("world", 1.2))),
        )

        val expectedJson = readResource("/api/expected-response.json")

        mockMvc.perform(get("/supermarkets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson))

    }

}