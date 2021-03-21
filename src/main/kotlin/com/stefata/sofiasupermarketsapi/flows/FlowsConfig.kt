package com.stefata.sofiasupermarketsapi.flows

import com.stefata.sofiasupermarketsapi.interfaces.SupermarketFlow
import com.stefata.sofiasupermarketsapi.model.Supermarket
import com.stefata.sofiasupermarketsapi.model.Supermarket.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlowsConfig(
    val billaFlow: BillaFlow,
    val kauflandFlow: KauflandFlow,
    val lidlFlow: LidlFlow,
    val fantasticoFlow: FantasticoFlow
) {

    @Bean
    fun flows(): Map<Supermarket, SupermarketFlow> {
        return mapOf(
            BILLA to billaFlow,
            KAUFLAND to kauflandFlow,
            LIDL to lidlFlow,
            FANTASTICO to fantasticoFlow
        )
    }
}