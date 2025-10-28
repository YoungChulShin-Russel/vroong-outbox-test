package com.company.outbox.spring4;

import com.company.outbox.core.OutboxProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringOutboxScheduler {
    
    private final OutboxProcessor outboxProcessor;
    
    @Value("${outbox.kafka.default-topic:outbox-events}")
    private String defaultTopic;
    
    @Scheduled(fixedDelayString = "${outbox.scheduler.fixed-delay:5000}")
    public void processOutboxEvents() {
        try {
            log.debug("Processing outbox events...");
            outboxProcessor.processOutboxEvents(defaultTopic);
        } catch (Exception e) {
            log.error("Error processing outbox events: {}", e.getMessage(), e);
        }
    }
}