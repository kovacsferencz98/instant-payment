package com.example.payment.repository;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class AccountDao {

    private final JdbcClient jdbcClient;

    public AccountDao(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }


}