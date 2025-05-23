package com.example.payment.controller;

import com.example.payment.dto.TransferRequest;
import com.example.payment.service.PaymentService;
import com.example.payment.service.ValidationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ValidationService validationService;

    @PostMapping
    @Operation(summary = "Transfer funds between accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient funds")
    })
    public ResponseEntity transfer(@RequestBody TransferRequest transferRequest) {
        validationService.validateTransferRequest(transferRequest);
        paymentService.transfer(transferRequest);
        return ResponseEntity.ok().build();
    }
}
