package com.example.payment.exception;

public class ValidationException extends RuntimeException {

    public final Error error;

    public ValidationException(Error error, String message) {
        super(message);
        this.error = error;
    }
}