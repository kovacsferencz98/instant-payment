package com.example.payment.exception;

import com.example.payment.service.LockService;
import com.example.payment.service.TransactionHandlerService;
import com.example.payment.state.TransactionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@RequiredArgsConstructor
@Component
@Slf4j
public class GlobalControllerExceptionHandler {

    private final TransactionContext context;
    private final TransactionHandlerService transactionHandlerService;
    private final LockService lockService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse internalUnhandledServerError(Exception ex) {
        return handleException(ex, Error.SERVER_ERROR, Error.SERVER_ERROR.message);
    }

    @ExceptionHandler(InternalApplicationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse internalHandledApplicationError(InternalApplicationException ex) {
        return handleException(ex, ex.error, ex.getMessage());
    }

    @ExceptionHandler(InsuficientFundsException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse insufficientFundsException(InsuficientFundsException ex) {
        return handleException(ex, ex.error, ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationError(ValidationException ex) {
        return handleException(ex, ex.error, ex.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse accountNotFoundError(AccountNotFoundException ex) {
        return handleException(ex, ex.error, ex.getMessage());
    }

    private ErrorResponse handleException(Exception e, Error error, String detailedMessage) {
        var errorCode = error.code;
        var errorMessage = String.join(": ", error.message, e.getMessage());
        log.error("Exception: {}", errorMessage, e);

        transactionHandlerService.tryRollbackTransaction(context);
        lockService.releaseLocks();

        return new ErrorResponse(errorCode, error.message);
    }
}