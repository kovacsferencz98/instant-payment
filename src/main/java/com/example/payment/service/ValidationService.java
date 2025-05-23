package com.example.payment.service;

import com.example.payment.dto.TransferRequest;
import com.example.payment.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.example.payment.exception.Error.INVALID_PARAMS;

@Service
public class ValidationService {

    public void validateTransferRequest(TransferRequest request) {
        if (request.getSenderId() == null) {
            throw new ValidationException(INVALID_PARAMS, "Sender ID cannot be null.");
        }
        if (request.getRecipientId() == null) {
            throw new ValidationException(INVALID_PARAMS, "Recipient ID cannot be null.");
        }
        if (request.getAmount() == null) {
            throw new ValidationException(INVALID_PARAMS, "Amount cannot be null.");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(INVALID_PARAMS, "Amount must be greater than zero.");
        }
    }
}
