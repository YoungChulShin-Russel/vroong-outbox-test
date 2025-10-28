package com.company.outbox.spring4;

import com.company.outbox.core.OutboxEvent;
import com.company.outbox.core.OutboxEventStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;
    
    @Column(name = "type", nullable = false, length = 128)
    private String type;
    
    @Column(name = "payload", nullable = false, columnDefinition = "JSON")
    private String payload;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxEventStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    public OutboxEventEntity(String aggregateId, String type, String payload) {
        this.aggregateId = aggregateId;
        this.type = type;
        this.payload = payload;
        this.status = OutboxEventStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
    
    public static OutboxEventEntity from(OutboxEvent outboxEvent) {
        OutboxEventEntity entity = new OutboxEventEntity(
            outboxEvent.getAggregateId(),
            outboxEvent.getType(),
            outboxEvent.getPayload()
        );
        entity.id = outboxEvent.getId();
        entity.status = outboxEvent.getStatus();
        entity.createdAt = outboxEvent.getCreatedAt();
        entity.publishedAt = outboxEvent.getPublishedAt();
        return entity;
    }
    
    public OutboxEvent toDomain() {
        OutboxEvent event = new OutboxEvent(aggregateId, type, payload);
        event.setId(id);
        if (status == OutboxEventStatus.PUBLISHED) {
            event.markAsPublished();
        } else if (status == OutboxEventStatus.FAILED) {
            event.markAsFailed();
        }
        return event;
    }
    
    public void markAsPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
    
    public void markAsFailed() {
        this.status = OutboxEventStatus.FAILED;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
}