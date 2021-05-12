package com.stefata.sofiasupermarketsapi.scheduled

import assertk.assertThat
import assertk.assertions.containsOnly
import com.ninjasquad.springmockk.MockkBean
import com.stefata.sofiasupermarketsapi.image.GoogleImageSearch
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [ScheduledImagesVerifier::class,
        ScheduledImagesVerifierTest.TestCacheConfig::class]
)
internal class ScheduledImagesVerifierTest {

    @Configuration
    class TestCacheConfig {
        @Bean
        fun cacheManager(): CacheManager {
            return ConcurrentMapCacheManager("productImages")
        }
    }

    @MockkBean
    lateinit var googleImageSearch: GoogleImageSearch

    @Autowired
    lateinit var underTest: ScheduledImagesVerifier

    @Autowired
    lateinit var cacheManager: CacheManager

    @Test
    fun `test verifying images`() {
        val cache = cacheManager.getCache("productImages")?.nativeCache as MutableMap<String, String>
        cache["test"] = "https://p1.akcdn.net/full/652773636.bira-astika-ken-0-5-l.jpg"
        cache["another test"] = "https://bbc.co.uk"

        every { googleImageSearch.search("test", false) } returns "https://www.telegraph.co.uk/"

        underTest.verifyImages()
        underTest.verifyImages()

        assertThat(cache).containsOnly(
            Pair("test", "https://www.telegraph.co.uk/"),
            Pair("another test", "https://bbc.co.uk")
        )

        verify(exactly = 1) {
            googleImageSearch.search("test", false)
        }


    }
}