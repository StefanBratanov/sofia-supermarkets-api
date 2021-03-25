package com.stefata.sofiasupermarketsapi.image

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.ninjasquad.springmockk.MockkBean
import com.stefata.sofiasupermarketsapi.model.ProductImage
import com.stefata.sofiasupermarketsapi.readResource
import com.stefata.sofiasupermarketsapi.repository.ProductImageRepository
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
import java.util.*

@SpringBootTest(classes = [GoogleImageSearch::class])
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
                    Parameter.param("q", "JAMESON Ирландско уиски")
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

        //return does not matter since not used
        every { productImageRepository.save(any()) } returns ProductImage("foo", "bar")

        val expectedImage = ProductImage(
            "JAMESON Ирландско уиски",
            "https://whiskystore.bg/userfiles/productlargeimages/product_2376.jpg"
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