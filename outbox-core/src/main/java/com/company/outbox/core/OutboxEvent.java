package com.company.outbox.core;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class OutboxEvent {
    private Long id;
    private String aggregateId;
    private String type;
    private byte[] payload;
    private OutboxEventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    public OutboxEvent(Long id, String aggregateId, String type, byte[] payload) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.status = OutboxEventStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = OutboxEventStatus.FAILED;
    }

}