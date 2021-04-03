package com.stefata.sofiasupermarketsapi.api

import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
class ApiConfig(
    val buildProperties: BuildProperties
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
    }

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.stefata.sofiasupermarketsapi"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo())
    }

    private fun apiInfo(): ApiInfo {
        return ApiInfoBuilder()
            .title("Sofia Supermarkets API Documentation")
            .description("An API to retrieve products information from supermarkets in Sofia, Bulgaria")
            .version(buildProperties.version)
            .license(ApiInfo.DEFAULT.license)
            .licenseUrl(ApiInfo.DEFAULT.licenseUrl)
            .termsOfServiceUrl(ApiInfo.DEFAULT.termsOfServiceUrl)
            .contact(Contact("","","stefan.bratanov93@gmail.com"))
            .build()
    }

}