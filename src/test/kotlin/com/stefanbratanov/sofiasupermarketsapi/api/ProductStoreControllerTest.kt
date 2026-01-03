package com.stefanbratanov.sofiasupermarketsapi.api

import com.ninjasquad.springmockk.MockkBean
import com.stefanbratanov.sofiasupermarketsapi.getProduct
import com.stefanbratanov.sofiasupermarketsapi.model.Product
import com.stefanbratanov.sofiasupermarketsapi.model.ProductStore
import com.stefanbratanov.sofiasupermarketsapi.readResource
import com.stefanbratanov.sofiasupermarketsapi.repository.ProductStoreRepository
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
  controllers = [ProductStoreController::class],
  excludeFilters =
    [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [ApiConfig::class])],
)
internal class ProductStoreControllerTest(@Autowired val mockMvc: MockMvc) {

  @MockkBean private lateinit var productStoreRepository: ProductStoreRepository

  @Test
  fun `test getting data for products`() {
    val offerProduct = Product(name = "bitcoin", price = 0.89, oldPrice = 1.12)

    val smallerOldPrice = Product(name = "error", price = 13.99, oldPrice = 1.19)

    val nullPrices = Product(name = "null", price = null, oldPrice = null)
    val oneNullPrice = Product(name = "null2", price = 13.99, oldPrice = null)

    every { productStoreRepository.findAll() } returns
      listOf(
        ProductStore(
          supermarket = "foo",
          products =
            listOf(
              getProduct("hello", 1.1),
              offerProduct,
              smallerOldPrice,
              nullPrices,
              oneNullPrice,
            ),
        ),
        ProductStore(supermarket = "bar", products = listOf(getProduct("world", 1.2))),
      )

    val expectedJson = readResource("/api/expected-response.json")
    val expectedJson2 = readResource("/api/expected-response-2.json")
    val expectedJsonOffers = readResource("/api/expected-response-offers.json")

    mockMvc
      .perform(get("/products").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(content().json(expectedJson))

    mockMvc
      .perform(get("/products?supermarket=foo").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(content().json(expectedJson2))

    mockMvc
      .perform(get("/products?supermarket=foo,bar").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(content().json(expectedJson))

    mockMvc
      .perform(get("/products?offers=false").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(content().json(expectedJson))

    mockMvc
      .perform(get("/products?offers=true").accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk)
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(content().json(expectedJsonOffers))
  }
}
