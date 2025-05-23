package com.example.payment.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration
@Slf4j
public class RedisContainerConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>("redis:7.2-alpine")
        .withExposedPorts(6379);


    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        REDIS_CONTAINER.start();

        TestPropertyValues.of(
            "spring.data.redis.host=" + REDIS_CONTAINER.getHost(),
            "spring.data.redis.port=" + REDIS_CONTAINER.getMappedPort(6379)
        ).applyTo(applicationContext.getEnvironment());
    }
}