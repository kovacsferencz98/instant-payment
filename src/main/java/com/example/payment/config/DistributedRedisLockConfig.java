package com.example.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;

@Configuration
public class DistributedRedisLockConfig {

    @Bean(destroyMethod = "destroy")
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory,
                                               @Value("${spring.application.name}") String applicationName) {
        return new RedisLockRegistry(redisConnectionFactory, applicationName + "-lock");
    }
}
