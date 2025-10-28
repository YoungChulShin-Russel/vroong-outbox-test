package com.company.outbox.core;

public interface KafkaPublisher {
    void publish(String topic, String key, String message);
    
    void publish(OutboxEvent event, String topic);
}