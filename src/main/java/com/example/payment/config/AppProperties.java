package com.example.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;


@ConfigurationProperties("app")
@Data
public class AppProperties {

    private Lock lock = new Lock();
    private KafkaTopics kafkaTopics = new KafkaTopics();
    private Redis redis;

    @Data
    public static class Lock {
        private long timeoutMs;
    }


    @Data
    public static class KafkaTopics {
        private String transactionCreated;
    }

    @Data
    public static class Redis {
        private String cacheNamePrefix;
        private Map<String, RedisCacheConfig> cache;
    }
}
