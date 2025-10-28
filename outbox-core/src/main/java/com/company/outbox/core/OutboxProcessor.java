package com.company.outbox.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class OutboxProcessor {
    private final OutboxRepository outboxRepository;
    private final KafkaPublisher kafkaPublisher;

    public void processOutboxEvents(String defaultTopic) {
        List<OutboxEvent> pendingEvents = outboxRepository.findPendingEvents();
        
        for (OutboxEvent event : pendingEvents) {
            try {
                kafkaPublisher.publish(event, defaultTopic);
                
                event.markAsPublished();
                outboxRepository.save(event);
                
                log.info("Successfully published outbox event: id={}, type={}", 
                        event.getId(), event.getType());
                
            } catch (Exception e) {
                event.markAsFailed();
                outboxRepository.save(event);
                
                log.error("Failed to publish outbox event: id={}, type={}, error={}", 
                        event.getId(), event.getType(), e.getMessage(), e);
            }
        }
    }
}