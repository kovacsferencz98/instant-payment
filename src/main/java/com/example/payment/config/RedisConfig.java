package com.example.payment.config;


import com.example.payment.dto.TransferRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisConfig {

    @Bean(name = "unsentMessages")
    public RedisValueCache<TransferRequest> unsentMessages(AppProperties properties,
                                                           RedisConnectionFactory factory,
                                                           ObjectMapper objectMapper) {
        return RedisValueCache.of(RedisUtils.redisTemplateWithoutCompression(factory,
                TransferRequest.class), properties.getRedis().getCacheNamePrefix(),
            "unsentMessages", properties.getRedis().getCache().get("unsentMessages"));
    }

}
