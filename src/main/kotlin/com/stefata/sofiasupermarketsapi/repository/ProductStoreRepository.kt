package com.stefata.sofiasupermarketsapi.repository

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.model.ProductStore
import org.springframework.data.keyvalue.repository.KeyValueRepository
import org.springframework.stereotype.Repository
import java.util.Objects.isNull

@Log
@Repository
interface ProductStoreRepository : KeyValueRepository<ProductStore, String> {

    @JvmDefault
    fun saveIfProductsNotEmpty(entity: ProductStore): ProductStore {
        val toSave = entity.takeUnless {
            it.products.isNullOrEmpty()
        }
        if (isNull(toSave)) {
            log.warn("Products are empty for {}. Will not save.", entity.supermarket)
            return entity
        }
        return save(entity)
    }

}