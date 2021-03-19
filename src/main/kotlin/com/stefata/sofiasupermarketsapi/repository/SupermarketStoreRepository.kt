package com.stefata.sofiasupermarketsapi.repository

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.model.SupermarketStore
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Objects.isNull

@Log
@Repository
interface SupermarketStoreRepository : CrudRepository<SupermarketStore, String> {

    @JvmDefault
    fun saveIfProductsNotEmpty(entity: SupermarketStore): SupermarketStore {
        val toSave = entity.takeUnless {
            it.products.isNullOrEmpty()
        }
        if (isNull(toSave)) {
            log.info("Products are empty for {}. Will not save.", entity.supermarket)
            return entity
        }
        return save(entity)
    }

}