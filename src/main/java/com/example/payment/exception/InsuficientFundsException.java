package com.example.payment.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class InsuficientFundsException extends RuntimeException {

    private final HttpStatusCode httpStatus;
    private final int code;
    private final String message;
    public final Error error;

    public static InsuficientFundsException from(Error e) {
        return new InsuficientFundsException(e.httpStatus, e.code, e.message, e);
    }

    public ErrorResponse toErrorResponse() {
        return new ErrorResponse(code, message);
    }

    public ResponseEntity<ErrorResponse> toErrorResponseEntity() {
        return ResponseEntity.status(httpStatus).body(toErrorResponse());
    }
}
