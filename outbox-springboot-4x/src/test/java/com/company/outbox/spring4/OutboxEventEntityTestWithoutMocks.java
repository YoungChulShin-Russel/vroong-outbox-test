package com.company.outbox.spring4;

import com.company.outbox.core.OutboxEvent;
import com.company.outbox.core.OutboxEventStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventEntityTestWithoutMocks {

    @Test
    void 생성자로_OutboxEventEntity를_만들_수_있다() {
        // given
        String aggregateId = "order-123";
        String type = "OrderCreated";
        String payload = "{\"orderId\":\"123\",\"amount\":1000}";

        // when
        OutboxEventEntity entity = new OutboxEventEntity(aggregateId, type, payload);

        // then
        assertThat(entity.getAggregateId()).isEqualTo(aggregateId);
        assertThat(entity.getType()).isEqualTo(type);
        assertThat(entity.getPayload()).isEqualTo(payload);
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getPublishedAt()).isNull();
    }

    @Test
    void from_메서드로_OutboxEvent에서_Entity로_변환할_수_있다() {
        // given
        OutboxEvent outboxEvent = new OutboxEvent("order-123", "OrderCreated", "{\"orderId\":\"123\"}");
        outboxEvent.setId(1L);
        outboxEvent.markAsPublished();

        // when
        OutboxEventEntity entity = OutboxEventEntity.from(outboxEvent);

        // then
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getAggregateId()).isEqualTo("order-123");
        assertThat(entity.getType()).isEqualTo("OrderCreated");
        assertThat(entity.getPayload()).isEqualTo("{\"orderId\":\"123\"}");
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(entity.getPublishedAt()).isNotNull();
    }

    @Test
    void toDomain_메서드로_Entity에서_OutboxEvent로_변환할_수_있다() {
        // given
        OutboxEventEntity entity = new OutboxEventEntity("order-123", "OrderCreated", "{\"orderId\":\"123\"}");
        entity.setId(1L);
        entity.markAsPublished();

        // when
        OutboxEvent domainEvent = entity.toDomain();

        // then
        assertThat(domainEvent.getId()).isEqualTo(1L);
        assertThat(domainEvent.getAggregateId()).isEqualTo("order-123");
        assertThat(domainEvent.getType()).isEqualTo("OrderCreated");
        assertThat(domainEvent.getPayload()).isEqualTo("{\"orderId\":\"123\"}");
        assertThat(domainEvent.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
    }

    @Test
    void markAsPublished를_호출하면_상태가_PUBLISHED로_변경된다() {
        // given
        OutboxEventEntity entity = new OutboxEventEntity("order-123", "OrderCreated", "{}");
        LocalDateTime beforePublish = LocalDateTime.now();

        // when
        entity.markAsPublished();

        // then
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(entity.getPublishedAt()).isNotNull();
        assertThat(entity.getPublishedAt()).isAfter(beforePublish);
    }

    @Test
    void markAsFailed를_호출하면_상태가_FAILED로_변경된다() {
        // given
        OutboxEventEntity entity = new OutboxEventEntity("order-123", "OrderCreated", "{}");

        // when
        entity.markAsFailed();

        // then
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(entity.getPublishedAt()).isNull();
    }
}