package com.stefata.sofiasupermarketsapi.repository

import com.stefata.sofiasupermarketsapi.common.Log
import com.stefata.sofiasupermarketsapi.common.Log.Companion.log
import com.stefata.sofiasupermarketsapi.model.SupermarketData
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Objects.isNull

@Log
@Repository
interface SupermarketDataRepository : CrudRepository<SupermarketData, String> {

    @JvmDefault
    fun saveIfProductsNotEmpty(entity: SupermarketData): SupermarketData {
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