package com.stefanbratanov.sofiasupermarketsapi.repository

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.model.ProductStore
import java.util.Objects.isNull
import org.springframework.data.keyvalue.repository.KeyValueRepository
import org.springframework.stereotype.Repository

@Log
@Repository
interface ProductStoreRepository : KeyValueRepository<ProductStore, String> {

  fun saveIfProductsNotEmpty(entity: ProductStore): ProductStore {
    val toSave = entity.takeUnless { it.products.isNullOrEmpty() }
    if (isNull(toSave)) {
      log.warn("Products are empty for {}. Will not save.", entity.supermarket)
      return entity
    }
    return save(entity)
  }
}
