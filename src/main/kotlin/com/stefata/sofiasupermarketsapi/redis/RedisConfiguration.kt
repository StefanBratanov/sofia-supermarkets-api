package com.stefata.sofiasupermarketsapi.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class RedisConfiguration {

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        return JedisConnectionFactory()
    }

    @Bean
    fun redisTemplate(jedisConnectionFactory: JedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(jedisConnectionFactory)
        return template
    }
}