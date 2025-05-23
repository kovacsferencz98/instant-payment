package com.example.payment.service;

import com.example.payment.state.TransactionContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionHandlerService {


    private final PlatformTransactionManager transactionManager;
    private final TransactionContext context;

    public boolean startTransaction() {
        try {
            var def = new DefaultTransactionDefinition();
            context.setTransactionStatus(transactionManager.getTransaction(def));
            context.setTransactionStarted(true);
            return true;
        } catch (Exception e) {
            log.error("Exception occurred trying to start transaction for context {}", context, e);
            context.setTransactionStarted(false);
            return false;
        }
    }

    public void endTransaction() {
        try {
            transactionManager.commit(context.getTransactionStatus());
        } catch (Exception e) {
            log.error("Exception occurred trying to commit transaction for context {}", context, e);
            tryRollbackTransaction(context);
        }
    }

    public void tryRollbackTransaction(TransactionContext context) {
        try {
            if (!context.isTransactionStartedAndStatusIsCompleted()) {
                transactionManager.rollback(context.getTransactionStatus());
            }
        } catch (Exception ignored) {
            log.warn("Exception occurred trying to rollback database transaction for context {}", context);
        }
    }



}
