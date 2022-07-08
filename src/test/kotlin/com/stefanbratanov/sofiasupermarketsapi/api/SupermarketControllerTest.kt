package com.stefanbratanov.sofiasupermarketsapi.api

import com.stefanbratanov.sofiasupermarketsapi.readResource
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
    controllers = [SupermarketController::class],
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [ApiConfig::class])]
)
internal class SupermarketControllerTest(@Autowired val mockMvc: MockMvc) {

    @Test
    fun `test getting data for supermarkets`() {
        val expectedJson = readResource("/api/expected-supermarkets.json")

        mockMvc.perform(MockMvcRequestBuilders.get("/supermarkets").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().json(expectedJson, false))
    }
}
