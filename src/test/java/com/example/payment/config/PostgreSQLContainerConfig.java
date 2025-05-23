package com.example.payment.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
@Slf4j
public class PostgreSQLContainerConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("payment")
        .withUsername("postgres")
        .withPassword("postgres")
        .withInitScript("db/init.sql"); // Initialize DB with script

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        POSTGRESQL_CONTAINER.start();

        TestPropertyValues.of(
            "spring.datasource.url=" + POSTGRESQL_CONTAINER.getJdbcUrl(),
            "spring.datasource.username=" + POSTGRESQL_CONTAINER.getUsername(),
            "spring.datasource.password=" + POSTGRESQL_CONTAINER.getPassword(),
            "spring.datasource.driver-class-name=org.postgresql.Driver",
            "spring.jpa.hibernate.ddl-auto=validate" // Changed from create-drop to validate
        ).applyTo(applicationContext.getEnvironment());
    }
}