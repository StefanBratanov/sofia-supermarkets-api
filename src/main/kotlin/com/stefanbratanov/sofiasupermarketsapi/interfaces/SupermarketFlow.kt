package com.stefanbratanov.sofiasupermarketsapi.interfaces

import com.stefanbratanov.sofiasupermarketsapi.common.Log
import com.stefanbratanov.sofiasupermarketsapi.common.Log.Companion.log
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket

@Log
interface SupermarketFlow {

  fun run()

  fun runSafely() {
    try {
      log.info("Starting flow for ${getSupermarket().title}")
      run()
      log.info("Finished flow for ${getSupermarket().title}")
    } catch (ex: Exception) {
      log.error("Error happened while running flow for ${getSupermarket().title}", ex)
    }
  }

  fun getSupermarket(): Supermarket
}
