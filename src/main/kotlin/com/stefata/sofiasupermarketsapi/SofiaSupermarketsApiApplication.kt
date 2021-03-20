package com.stefata.sofiasupermarketsapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.map.repository.config.EnableMapRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableMapRepositories
@EnableScheduling
class SofiaSupermarketsApiApplication

fun main(args: Array<String>) {
    runApplication<SofiaSupermarketsApiApplication>(*args)
}
