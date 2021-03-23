package com.stefata.sofiasupermarketsapi.api

import com.ninjasquad.springmockk.MockkBean
import com.stefata.sofiasupermarketsapi.getProduct
import com.stefata.sofiasupermarketsapi.model.Product
import com.stefata.sofiasupermarketsapi.model.ProductStore
import com.stefata.sofiasupermarketsapi.readResource
import com.stefata.sofiasupermarketsapi.repository.ProductStoreRepository
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
internal class ProductStoreControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var productStoreRepository: ProductStoreRepository

    @Test
    fun `test getting data for products`() {
        val offerProduct = Product(name = "bitcoin", price = 1.12, oldPrice = 0.89)

        every { productStoreRepository.findAll() } returns listOf(
            ProductStore(supermarket = "foo", products = listOf(getProduct("hello", 1.1), offerProduct)),
            ProductStore(supermarket = "bar", products = listOf(getProduct("world", 1.2))),
        )

        val expectedJson = readResource("/api/expected-response.json")
        val expectedJson2 = readResource("/api/expected-response-2.json")
        val expectedJsonOffers = readResource("/api/expected-response-offers.json")

        mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson))

        mockMvc.perform(get("/products?supermarkets=foo").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson2))

        mockMvc.perform(get("/products?supermarkets=foo,bar").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson))

        mockMvc.perform(get("/products?offers=false").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJson))

        mockMvc.perform(get("/products?offers=true").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expectedJsonOffers))

    }

}