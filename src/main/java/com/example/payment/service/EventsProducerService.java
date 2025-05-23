package com.example.payment.service;

import com.example.payment.config.AppProperties;
import com.example.payment.config.RedisValueCache;
import com.example.payment.dto.TransferRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EventsProducerService {

    private static final String UNSENT_MESSAGES_SET_NAME = "unsent_messages";

    private final AppProperties properties;
    private final KafkaTemplate kafkaTemplate;
    private final RedisValueCache<TransferRequest> unsentMessages;

    public void sendMessage(TransferRequest event) {
        sendMessage(event, String.valueOf(event.getSenderId()), UNSENT_MESSAGES_SET_NAME, unsentMessages,
            properties.getKafkaTopics().getTransactionCreated());
    }

    private <T> void sendMessage(T event, String key, String setName, RedisValueCache<T> redisCache, String topic) {
        try {
            kafkaTemplate.send(topic, key, event)
                .whenComplete((msg, ex) -> {
                    if (ex != null) {
                        log.error("Kafka message publishing failed, adding to local cache for retry. {}", event, ex);
                        try {
                            redisCache.addToSet(setName, event);
                        } catch (Exception e) {
                            log.error("Message lost trying to added to DLQ. Manual actions needed: {}", event);
                        }
                    }
                });
        } catch (Exception ex) {
            log.error("Kafka message publishing failed, adding to local cache for retry. {}", event, ex);
            try {
                redisCache.addToSet(setName, event);
            } catch (Exception e) {
                log.error("Message lost trying to added to DLQ. Manual actions needed: {}", event);
            }
        }
    }
}
