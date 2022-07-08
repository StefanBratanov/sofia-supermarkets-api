package com.stefanbratanov.sofiasupermarketsapi.image

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ninjasquad.springmockk.MockkBean
import com.stefanbratanov.sofiasupermarketsapi.model.ProductImage
import com.stefanbratanov.sofiasupermarketsapi.readResource
import com.stefanbratanov.sofiasupermarketsapi.repository.ProductImageRepository
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType
import org.mockserver.model.Parameter
import org.mockserver.springtest.MockServerTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import java.util.*

@SpringBootTest(classes = [GoogleImageSearch::class])
@TestPropertySource(properties = ["google.image.search.url=http://foo.bar"])
@MockServerTest("google.custom.search.url=http://localhost:\${mockServerPort}/search?test=test")
internal class GoogleImageSearchTest {

    @MockkBean
    private lateinit var productImageRepository: ProductImageRepository

    @Autowired
    private lateinit var underTest: GoogleImageSearch

    private lateinit var mockServerClient: MockServerClient

    @Test
    fun `calls google search api`() {
        val searchResponseJson = readResource("/image/google-search-response.json")

        mockServerClient.`when`(
            request()
                .withPath("/search")
                .withQueryStringParameter(
                    Parameter.param("q", "JAMESON Ирландско уиски 0,7 л")
                )
        ).respond(
            response()
                .withStatusCode(200)
                .withContentType(MediaType.APPLICATION_JSON_UTF_8)
                .withBody(searchResponseJson)
        )

        mockServerClient.`when`(
            request()
                .withPath("/search")
                .withQueryStringParameter(
                    Parameter.param("q", "fail")
                )
        ).respond(
            response()
                .withStatusCode(400)
        )

        every { productImageRepository.findById(any()) } returns Optional.empty()
        every { productImageRepository.findById("foo") } returns Optional.of(
            ProductImage("foo", "bar.com")
        )

        // return does not matter since not used
        every { productImageRepository.save(any()) } returns ProductImage("foo", "bar")

        val expectedImage = ProductImage(
            "JAMESON Ирландско уиски 0,7 л",
            "https://cdncloudcart.com/16474/products/images/2518/jameson-black-barrel-s-dve-casi-700ml-image_5fbd72066e05c_800x800.jpeg?1606251032"
        )

        val result = underTest.search(expectedImage.product)
        assertThat(result).isEqualTo(expectedImage.url)

        val result1 = underTest.search("foo")
        assertThat(result1).isEqualTo("bar.com")

        val failedResult = underTest.search("fail")
        assertThat(failedResult).isNull()

        verify(exactly = 1) {
            productImageRepository.save(expectedImage)
        }
    }
}
