package com.stefata.sofiasupermarketsapi.repository

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.stefata.sofiasupermarketsapi.model.ProductImage
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class ProductImageRepositoryTest {

    @Autowired
    lateinit var underTest: ProductImageRepository

    @Test
    fun `test saving and retrieving data`() {

        val toSave = ProductImage("foo", "http://test.com")
        val toSave2 = ProductImage("bar", "http://test.com")

        underTest.save(toSave)
        underTest.save(toSave2)

        val retrieved = underTest.findById("foo")
        val missing = underTest.findById("avi")

        assertThat(retrieved.isPresent).isTrue()
        assertThat(retrieved.get()).isEqualTo(toSave)
        assertThat(missing.isPresent).isFalse()

    }
}