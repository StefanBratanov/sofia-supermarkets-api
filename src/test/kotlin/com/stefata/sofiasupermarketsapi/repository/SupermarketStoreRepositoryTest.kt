package com.stefata.sofiasupermarketsapi.repository

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.stefata.sofiasupermarketsapi.getProduct
import com.stefata.sofiasupermarketsapi.model.SupermarketStore
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.map.repository.config.EnableMapRepositories

@EnableMapRepositories
@SpringBootTest(classes = [SupermarketStoreRepository::class])
class SupermarketStoreRepositoryTest {

    @Autowired
    lateinit var underTest: SupermarketStoreRepository

    @Test
    fun `test saving and retrieving data`() {
        val foo = getProduct("foo")
        val bar = getProduct("bar")

        val myStoreEntry = SupermarketStore(supermarket = "myStore", products = listOf(foo, bar))

        underTest.saveIfProductsNotEmpty(myStoreEntry)

        var savedStoreEntry = underTest.findById("myStore")
        assertThat(savedStoreEntry.isPresent).isTrue()
        assertThat(savedStoreEntry.get()).isEqualTo(myStoreEntry)

        assertThat(underTest.findAll().toList()).hasSize(1)

        val emptyStoreEntry = SupermarketStore(supermarket = "myStore", products = emptyList())

        underTest.saveIfProductsNotEmpty(emptyStoreEntry)

        savedStoreEntry = underTest.findById("myStore")
        assertThat(savedStoreEntry.isPresent).isTrue()
        assertThat(savedStoreEntry.get()).isEqualTo(myStoreEntry)

        assertThat(underTest.findAll().toList()).hasSize(1)

        val anotherStoryEntry = SupermarketStore(supermarket = "anotherStore", products = listOf(foo, bar))

        underTest.saveIfProductsNotEmpty(anotherStoryEntry)

        val savedStoreEntry1 = underTest.findById("anotherStore")
        assertThat(savedStoreEntry1.isPresent).isTrue()
        assertThat(savedStoreEntry1.get()).isEqualTo(anotherStoryEntry)

        assertThat(underTest.findAll().toList()).hasSize(2)
    }
}