package com.example.payment.config;

import lombok.Data;

import java.time.Duration;


@Data
public class RedisCacheConfig {

    Duration timeToLive;
    Duration expireAfterAccess;
}