package com.company.outbox.spring4;

import com.company.outbox.core.KafkaPublisher;
import com.company.outbox.core.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringKafkaPublisher implements KafkaPublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Override
    public void publish(String topic, String key, String message) {
        try {
            kafkaTemplate.send(topic, key, message);
            log.debug("Published message to topic: {}, key: {}", topic, key);
        } catch (Exception e) {
            log.error("Failed to publish message to topic: {}, key: {}, error: {}", 
                     topic, key, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public void publish(OutboxEvent event, String topic) {
        String key = event.getAggregateId();
        String message = event.getPayload();
        publish(topic, key, message);
    }
}