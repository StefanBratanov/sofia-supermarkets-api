package com.stefata.sofiasupermarketsapi.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
class ApiConfig {

    @Bean
    fun api() : Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.stefata.sofiasupermarketsapi"))
            .paths(PathSelectors.any())
            .build()
    }
}