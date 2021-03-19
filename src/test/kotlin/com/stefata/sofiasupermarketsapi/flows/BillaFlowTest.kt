package com.stefata.sofiasupermarketsapi.flows

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.stefata.sofiasupermarketsapi.getProductWithName
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.model.Supermarket
import com.stefata.sofiasupermarketsapi.model.SupermarketData
import com.stefata.sofiasupermarketsapi.repository.SupermarketDataRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URL

@ExtendWith(MockKExtension::class)
internal class BillaFlowTest {

    @MockK
    lateinit var url: URL

    @MockK
    lateinit var urlProductsExtractor: UrlProductsExtractor

    @MockK
    lateinit var supermarketDataRepository: SupermarketDataRepository

    @InjectMockKs
    lateinit var underTest: BillaFlow

    @Test
    fun `runs flow for billa`() {

        val hello = getProductWithName("hello")

        every { urlProductsExtractor.extract(url) } returns listOf(hello)
        every { supermarketDataRepository.saveIfProductsNotEmpty(any()) } returnsArgument 0

        underTest.runSafely()

        verify { urlProductsExtractor.extract(url) }

        val expectedToSave = SupermarketData(supermarket = "Billa", products = listOf(hello))
        verify {
            supermarketDataRepository.saveIfProductsNotEmpty(match {
                it.supermarket == expectedToSave.supermarket &&
                        it.products == expectedToSave.products
            })
        }
    }

    @Test
    fun `gets correct supermarket name`() {
        assertThat(underTest.getSupermarket()).isEqualTo(Supermarket.BILLA)
    }

}