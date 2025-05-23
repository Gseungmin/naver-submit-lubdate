package com.example.naver.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RedisConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RedisTemplate<String, Object> objectTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, Object> template =
                new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer =
                new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer JsonSerializer =
                new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(JsonSerializer);
        template.setHashValueSerializer(JsonSerializer);

        return template;
    }

    @Bean
    public RedisTemplate<String, String> stringTemplate(
            RedisConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, String> template =
                new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer serializer =
                new StringRedisSerializer();

        template.setKeySerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.setDefaultSerializer(serializer);

        return template;
    }
}
