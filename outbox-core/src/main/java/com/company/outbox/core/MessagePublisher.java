package com.company.outbox.core;

public interface MessagePublisher {
    void publish(OutboxEvent event);
}