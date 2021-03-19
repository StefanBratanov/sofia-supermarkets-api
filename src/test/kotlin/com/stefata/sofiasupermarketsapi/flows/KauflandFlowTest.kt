package com.stefata.sofiasupermarketsapi.flows

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.stefata.sofiasupermarketsapi.getProductWithName
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
import com.stefata.sofiasupermarketsapi.links.KauflandSublinksScraper
import com.stefata.sofiasupermarketsapi.model.Supermarket
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verifyAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URL

@ExtendWith(MockKExtension::class)
internal class KauflandFlowTest {

    @MockK
    lateinit var kauflandSublinksScraper: KauflandSublinksScraper

    @MockK
    lateinit var urlProductsExtractor: UrlProductsExtractor

    @InjectMockKs
    lateinit var underTest: KauflandFlow

    @Test
    fun `runs flow for Kaufland`() {

        val url1 = URL("http://stefan.com")
        val url2 = URL("http://aivaras.com")
        val url3 = URL("http://bogdan.com")

        every { kauflandSublinksScraper.getSublinks() } returns listOf(url1, url2, url3)

        val hello = getProductWithName("hello")
        val world = getProductWithName("world")
        val foo = getProductWithName("foo")
        val bar = getProductWithName("bar")

        every { urlProductsExtractor.extract(url1) } returns listOf(hello)
        every { urlProductsExtractor.extract(url2) } returns listOf(world)
        every { urlProductsExtractor.extract(url3) } returns listOf(foo, bar)

        underTest.runSafely()

        verifyAll {
            urlProductsExtractor.extract(url1)
            urlProductsExtractor.extract(url2)
            urlProductsExtractor.extract(url3)
        }
    }

    @Test
    fun `gets correct supermarket name`() {
        assertThat(underTest.getSupermarket()).isEqualTo(Supermarket.KAUFLAND)
    }
}