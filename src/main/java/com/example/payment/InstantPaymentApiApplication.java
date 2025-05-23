package com.example.payment;

import com.example.payment.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class InstantPaymentApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(InstantPaymentApiApplication.class, args);
    }
}
