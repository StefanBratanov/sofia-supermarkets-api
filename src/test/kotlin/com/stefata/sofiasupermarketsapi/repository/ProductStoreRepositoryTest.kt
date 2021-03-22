package com.stefata.sofiasupermarketsapi.repository

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.stefata.sofiasupermarketsapi.getProduct
import com.stefata.sofiasupermarketsapi.model.ProductStore
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.map.repository.config.EnableMapRepositories

@EnableMapRepositories
@SpringBootTest(classes = [ProductStoreRepository::class])
class ProductStoreRepositoryTest {

    @Autowired
    lateinit var underTest: ProductStoreRepository

    @Test
    fun `test saving and retrieving data`() {
        val foo = getProduct("foo")
        val bar = getProduct("bar")

        val myStoreEntry = ProductStore(supermarket = "myStore", products = listOf(foo, bar))

        underTest.saveIfProductsNotEmpty(myStoreEntry)

        var savedStoreEntry = underTest.findById("myStore")
        assertThat(savedStoreEntry.isPresent).isTrue()
        assertThat(savedStoreEntry.get()).isEqualTo(myStoreEntry)

        assertThat(underTest.findAll().toList()).hasSize(1)

        val emptyStoreEntry = ProductStore(supermarket = "myStore", products = emptyList())

        underTest.saveIfProductsNotEmpty(emptyStoreEntry)

        savedStoreEntry = underTest.findById("myStore")
        assertThat(savedStoreEntry.isPresent).isTrue()
        assertThat(savedStoreEntry.get()).isEqualTo(myStoreEntry)

        assertThat(underTest.findAll().toList()).hasSize(1)

        val anotherStoryEntry = ProductStore(supermarket = "anotherStore", products = listOf(foo, bar))

        underTest.saveIfProductsNotEmpty(anotherStoryEntry)

        val savedStoreEntry1 = underTest.findById("anotherStore")
        assertThat(savedStoreEntry1.isPresent).isTrue()
        assertThat(savedStoreEntry1.get()).isEqualTo(anotherStoryEntry)

        assertThat(underTest.findAll().toList()).hasSize(2)
    }
}