package com.example.payment.service;

import com.example.payment.dto.TransferRequest;
import com.example.payment.exception.InsuficientFundsException;
import com.example.payment.exception.AccountNotFoundException;
import com.example.payment.exception.InternalApplicationException;
import com.example.payment.model.Account;
import com.example.payment.model.Transaction;
import com.example.payment.repository.AccountRepository;
import com.example.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.payment.exception.Error.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;
    private final TransactionHandlerService transactionHandlerService;
    private final LockService lockService;
    private final EventsProducerService eventsProducerService;

    private final static ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public void transfer(TransferRequest request) {
        if (prepareTransaction(request)) {
            try {
                updateAccountBalances(request);
                storeTransaction(request);
                sendNotification(request);
            } finally {
                cleanup(); // Ensures transaction is ended (committed or rolled back) and locks are released.
            }
        } else {
            throw new InternalApplicationException(SERVER_ERROR);

        }
    }

    private boolean prepareTransaction(TransferRequest request) {
        return lockService.lockAccounts(request) && transactionHandlerService.startTransaction();
    }

    private void storeTransaction(TransferRequest request) {
        Transaction tx = new Transaction();
        tx.setSenderId(request.getSenderId());
        tx.setRecipientId(request.getRecipientId());
        tx.setAmount(request.getAmount());
        transactionRepo.save(tx);
    }

    private void updateAccountBalances(TransferRequest request) {
        Account sender = accountRepo.findById(request.getSenderId())
            .orElseThrow(() -> new AccountNotFoundException(USER_NOT_FOUND.httpStatus, USER_NOT_FOUND.code,
                USER_NOT_FOUND.message, USER_NOT_FOUND));
        Account recipient = accountRepo.findById(request.getRecipientId())
            .orElseThrow(() -> new AccountNotFoundException(USER_NOT_FOUND.httpStatus, USER_NOT_FOUND.code,
                USER_NOT_FOUND.message, USER_NOT_FOUND));

        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            throw InsuficientFundsException.from(INSUFFICIENT_FUNDS);
        }

        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        recipient.setBalance(recipient.getBalance().add(request.getAmount()));

        accountRepo.save(sender);
        accountRepo.save(recipient);
    }

    private void cleanup() {
        transactionHandlerService.endTransaction();
        lockService.releaseLocks();
    }

    private void sendNotification(TransferRequest request) {
        executor.submit(() -> {
            eventsProducerService.sendMessage(request);
            return true;
        });
    }
}
