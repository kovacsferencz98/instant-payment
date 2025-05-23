package com.example.payment.service;

import com.example.payment.config.RedisValueCache;
import com.example.payment.dto.TransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.LongStream;

@Service
@Slf4j
public class DeadLetterQueueHandler {

    private static final String UNSENT_MESSAGES_SET_NAME = "unsent_messages";
    private static final String UNSENT_MESSAGES_V3_SET_NAME = "unsent_messages_v3";
    private static final long UNSENT_MESSAGE_POP_COUNT_PER_CALL = 10L;

    private final RedisValueCache<TransferRequest> unsentMessages;
    private final EventsProducerService eventsProducerService;

    public DeadLetterQueueHandler(RedisValueCache<TransferRequest> unsentMessages,
                                  EventsProducerService eventsProducerService) {
        this.unsentMessages = unsentMessages;
        this.eventsProducerService = eventsProducerService;
    }

    @Scheduled(fixedDelayString = "${app.dead-letter.resend-interval}")
    public void deadLetterRetryTask() {
        resendMessagesFromQueue(unsentMessages, UNSENT_MESSAGES_SET_NAME, eventsProducerService::sendMessage);
    }

    private <T> void resendMessagesFromQueue(RedisValueCache<T> unsentMessagesCache, String setName,
                                             Consumer<T> sendMessageFunciton) {
        Optional.of(unsentMessagesCache)
            .stream()
            .mapToLong(unsentMessages -> unsentMessages.getSetSize(setName))
            .filter(n -> n > 0)
            .map(n -> {
                log.info("Trying to send {} messages from the dead letter queue", n);
                return (n + UNSENT_MESSAGE_POP_COUNT_PER_CALL) / UNSENT_MESSAGE_POP_COUNT_PER_CALL;
            })
            .findFirst()
            .ifPresent(neededPopCalls -> {
                LongStream.range(0L, neededPopCalls)
                    .forEach(i ->
                        unsentMessagesCache.popSetItems(setName, UNSENT_MESSAGE_POP_COUNT_PER_CALL)
                            .forEach(sendMessageFunciton)
                    );
                log.info("Messages sent from the dead letter queue");
            });
    }
}
