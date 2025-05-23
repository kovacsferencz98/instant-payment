package com.example.payment.exception;

import org.springframework.http.HttpStatus;

public enum Error {
    INVALID_PARAMS(HttpStatus.BAD_REQUEST, 4001, "Invalid request parameters"),
    INSUFFICIENT_FUNDS(HttpStatus.FORBIDDEN, 4031, "Insufficient funds"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 4041, "User not found"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Internal error");

    public final HttpStatus httpStatus;
    public final int code;
    public final String message;

    Error(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}