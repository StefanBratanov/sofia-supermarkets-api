package com.stefanbratanov.sofiasupermarketsapi.api

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ApiConfig(
    val buildProperties: BuildProperties
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }

    @Bean
    fun openAPI(@Value("\${api.server.url}") apiServerUrl: String): OpenAPI {
        return OpenAPI()
            .addServersItem(Server().url(apiServerUrl))
            .info(
                Info().title("Sofia Supermarkets API Documentation")
                    .description("An API to retrieve products information from supermarkets in Sofia, Bulgaria")
                    .version(buildProperties.version)
                    .contact(Contact().email("stefan.bratanov93@gmail.com"))
                    .license(
                        License().name("Apache 2.0")
                            .url("http://www.apache.org/licenses/LICENSE-2.0")
                    )
                    .termsOfService("urn:tos")
            )
    }
}
