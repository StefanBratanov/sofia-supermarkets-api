package com.stefata.sofiasupermarketsapi.flows

import com.stefata.sofiasupermarketsapi.getProductWithName
import com.stefata.sofiasupermarketsapi.interfaces.UrlProductsExtractor
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

    @InjectMockKs
    lateinit var underTest: BillaFlow

    @Test
    fun `runs flow for billa`() {

        val hello = getProductWithName("hello")

        every { urlProductsExtractor.extract(url) } returns listOf(hello)

        underTest.runSafely()

        verify { urlProductsExtractor.extract(url) }
    }

}