package com.example.payment.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InternalApplicationException extends RuntimeException {

    public final Error error;
}
