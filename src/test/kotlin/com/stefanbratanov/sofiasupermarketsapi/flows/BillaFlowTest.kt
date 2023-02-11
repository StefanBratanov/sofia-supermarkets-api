package com.stefanbratanov.sofiasupermarketsapi.flows

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.stefanbratanov.sofiasupermarketsapi.getProduct
import com.stefanbratanov.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefanbratanov.sofiasupermarketsapi.links.BillaSublinksScraper
import com.stefanbratanov.sofiasupermarketsapi.model.ProductStore
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket
import com.stefanbratanov.sofiasupermarketsapi.repository.ProductStoreRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import io.mockk.verify
import java.net.URL
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class BillaFlowTest {

  @MockK lateinit var billaSublinksScraper: BillaSublinksScraper

  @MockK lateinit var urlProductsExtractor: UrlProductsExtractor

  @MockK lateinit var productStoreRepository: ProductStoreRepository

  @InjectMockKs lateinit var underTest: BillaFlow

  @Test
  fun `runs flow for billa`() {
    val hello = getProduct("hello")
    val world = getProduct("world")

    val url1 = mockkClass(URL::class)
    val url2 = mockkClass(URL::class)

    every { billaSublinksScraper.getSublinks() } returns listOf(url1, url2)

    every { urlProductsExtractor.extract(url1) } returns listOf(hello)
    every { urlProductsExtractor.extract(url2) } returns listOf(world)

    every { productStoreRepository.saveIfProductsNotEmpty(any()) } returnsArgument 0

    underTest.runSafely()

    verify { urlProductsExtractor.extract(url1) }
    verify { urlProductsExtractor.extract(url2) }

    val expectedToSave = ProductStore(supermarket = "Billa", products = listOf(hello, world))
    verify {
      productStoreRepository.saveIfProductsNotEmpty(
        match {
          it.supermarket == expectedToSave.supermarket && it.products == expectedToSave.products
        },
      )
    }
  }

  @Test
  fun `gets correct supermarket name`() {
    assertThat(underTest.getSupermarket()).isEqualTo(Supermarket.BILLA)
  }
}
