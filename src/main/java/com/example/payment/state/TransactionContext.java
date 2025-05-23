package com.example.payment.state;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.context.annotation.RequestScope;

import java.util.*;
import java.util.concurrent.locks.Lock;

@Getter
@RequestScope
@Component
@Slf4j
public class TransactionContext  {
    @Setter
    private boolean transactionStarted;

    private final ArrayDeque<Lock> locks = new ArrayDeque<>();

    @Setter
    private TransactionStatus transactionStatus;

    public boolean isTransactionStartedAndStatusIsCompleted() {
        return transactionStatus != null && transactionStatus.isCompleted();
    }

}
