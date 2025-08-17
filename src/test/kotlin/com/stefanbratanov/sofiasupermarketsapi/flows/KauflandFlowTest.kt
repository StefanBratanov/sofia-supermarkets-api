package com.stefanbratanov.sofiasupermarketsapi.flows

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.stefanbratanov.sofiasupermarketsapi.getProduct
import com.stefanbratanov.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefanbratanov.sofiasupermarketsapi.model.ProductStore
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket
import com.stefanbratanov.sofiasupermarketsapi.repository.ProductStoreRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import java.net.URI
import java.net.URL
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KauflandFlowTest {

  private val kauflandBaseUrl: URL = URI("http://foo.bar").toURL()

  @MockK lateinit var urlProductsExtractor: UrlProductsExtractor

  @MockK lateinit var productStoreRepository: ProductStoreRepository

  private lateinit var underTest: KauflandFlow

  @BeforeEach
  fun setUp() {
    MockKAnnotations.init(this)
    underTest = KauflandFlow(kauflandBaseUrl, urlProductsExtractor, productStoreRepository)
  }

  @Test
  fun `runs flow for Kaufland`() {

    val hello = getProduct("hello")
    val world = getProduct("world")
    val foo = getProduct("foo")
    val bar = getProduct("bar")

    every { urlProductsExtractor.extract(kauflandBaseUrl) } returns listOf(hello, world, foo, bar)

    every { productStoreRepository.saveIfProductsNotEmpty(any()) } returnsArgument 0

    underTest.runSafely()

    verify { urlProductsExtractor.extract(kauflandBaseUrl) }

    val expectedToSave =
      ProductStore(supermarket = "Kaufland", products = listOf(hello, world, foo, bar))
    verify {
      productStoreRepository.saveIfProductsNotEmpty(
        match {
          it.supermarket == expectedToSave.supermarket && it.products == expectedToSave.products
        }
      )
    }
  }

  @Test
  fun `gets correct supermarket name`() {
    assertThat(underTest.getSupermarket()).isEqualTo(Supermarket.KAUFLAND)
  }
}
