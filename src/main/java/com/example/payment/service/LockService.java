package com.example.payment.service;

import com.example.payment.config.AppProperties;
import com.example.payment.dto.TransferRequest;
import com.example.payment.state.TransactionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class LockService {

    private final LockRegistry lockRegistry;
    private final TransactionContext context;
    private final AppProperties properties;
    private static final String TRANSACTION_LOCK_KEY_PREFIX = "t";

    public boolean lockAccounts(TransferRequest transferRequest) {
        var accountIds = Stream.of(transferRequest.getRecipientId(), transferRequest.getSenderId())
            .sorted()
            .toList();
        var locks = context.getLocks();

        for (var accountId : accountIds) {
            try {
                var transactionLock = lockRegistry.obtain(prepareTransactionLockKey(accountId));
                if (!transactionLock.tryLock(properties.getLock().getTimeoutMs(), TimeUnit.MILLISECONDS)) {
                    releaseLocks();
                    return false;
                }
                locks.push(transactionLock);
            } catch (Exception e) {
                releaseLocks();
                return false;
            }
        }

        return true;
    }

    public void releaseLocks() {
        try {
            var locks = context.getLocks();
            if (locks != null) {
                while (!locks.isEmpty()) {
                    var lock = locks.pop();
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("Exception occurred trying to release lock for locks {}", context.getLocks(), e);
        }
    }

    private String prepareTransactionLockKey(Long accountId) {
        return String.join("-", TRANSACTION_LOCK_KEY_PREFIX, String.valueOf(accountId));
    }

}
