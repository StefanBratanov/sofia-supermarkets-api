package com.stefanbratanov.sofiasupermarketsapi.flows

import com.stefanbratanov.sofiasupermarketsapi.interfaces.SupermarketFlow
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket.BILLA
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket.FANTASTICO
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket.KAUFLAND
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket.LIDL
import com.stefanbratanov.sofiasupermarketsapi.model.Supermarket.TMARKET
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlowsConfig(
  val billaFlow: BillaFlow,
  val kauflandFlow: KauflandFlow,
  val lidlFlow: LidlFlow,
  val fantasticoFlow: FantasticoFlow,
  val tMarketFlow: TMarketFlow,
) {

  @Bean
  fun flows(): Map<Supermarket, SupermarketFlow> {
    return mapOf(
      BILLA to billaFlow,
      KAUFLAND to kauflandFlow,
      LIDL to lidlFlow,
      FANTASTICO to fantasticoFlow,
      TMARKET to tMarketFlow,
    )
  }
}
