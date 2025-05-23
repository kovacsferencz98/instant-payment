package com.example.payment.config;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

public final class RedisUtils {

    private RedisUtils() {
    }

    public static <T> RedisTemplate<String, T> redisTemplateWithoutCompression(
        RedisConnectionFactory factory, Class<T> clazz) {
        return redisTemplate(factory, new Jackson2JsonRedisSerializer<>(clazz));
    }

    private static <T> RedisTemplate<String, T> redisTemplate(RedisConnectionFactory factory,
                                                              RedisSerializer<T> redisSerializer) {
        RedisTemplate<String, T> t = new RedisTemplate<>();
        t.setDefaultSerializer(redisSerializer);
        t.setKeySerializer(RedisSerializer.string());
        t.setConnectionFactory(factory);
        t.afterPropertiesSet();
        return t;
    }
}