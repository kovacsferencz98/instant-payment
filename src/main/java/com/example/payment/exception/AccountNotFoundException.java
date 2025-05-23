package com.example.payment.exception;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccountNotFoundException extends RuntimeException {

    private final HttpStatusCode httpStatus;
    private final int code;
    private final String message;
    public final Error error;
}
