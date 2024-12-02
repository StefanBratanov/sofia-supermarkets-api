package com.stefanbratanov.sofiasupermarketsapi.flows

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.stefanbratanov.sofiasupermarketsapi.getProduct
import com.stefanbratanov.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefanbratanov.sofiasupermarketsapi.links.TMarketSublinksScraper
import com.stefanbratanov.sofiasupermarketsapi.model.ProductStore
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket
import com.stefanbratanov.sofiasupermarketsapi.repository.ProductStoreRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.mockk.verifyAll
import java.net.URL
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class TMarketFlowTest {

  @MockK lateinit var tmarketSublinksScraper: TMarketSublinksScraper

  @MockK lateinit var urlProductsExtractor: UrlProductsExtractor

  @MockK lateinit var productStoreRepository: ProductStoreRepository

  @InjectMockKs lateinit var underTest: TMarketFlow

  @Test
  fun `runs flow for TMarket`() {
    val url1 = URL("http://stefan.com")
    val url2 = URL("http://aivaras.com")
    val url3 = URL("http://bogdan.com")

    every { tmarketSublinksScraper.getSublinks() } returns listOf(url1, url2, url3)

    val hello = getProduct("hello")
    val world = getProduct("world")
    val foo = getProduct("foo")
    val bar = getProduct("bar")

    every { urlProductsExtractor.extract(url1) } returns listOf(hello)
    every { urlProductsExtractor.extract(url2) } returns listOf(world)
    every { urlProductsExtractor.extract(url3) } returns listOf(foo, bar)

    every { productStoreRepository.saveIfProductsNotEmpty(any()) } returnsArgument 0

    underTest.runSafely()

    verifyAll {
      urlProductsExtractor.extract(url1)
      urlProductsExtractor.extract(url2)
      urlProductsExtractor.extract(url3)
    }

    val expectedToSave =
      ProductStore(supermarket = "T-Market", products = listOf(hello, world, foo, bar))
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
    assertThat(underTest.getSupermarket()).isEqualTo(Supermarket.TMARKET)
  }
}
