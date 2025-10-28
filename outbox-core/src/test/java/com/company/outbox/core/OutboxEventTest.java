package com.company.outbox.core;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {

    @Test
    void 생성자로_OutboxEvent를_만들_수_있다() {
        // given
        String aggregateId = "order-123";
        String type = "OrderCreated";
        String payload = "{\"orderId\":\"123\",\"amount\":1000}";

        // when
        OutboxEvent event = new OutboxEvent(aggregateId, type, payload);

        // then
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getType()).isEqualTo(type);
        assertThat(event.getPayload()).isEqualTo(payload);
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(event.getCreatedAt()).isNotNull();
        assertThat(event.getPublishedAt()).isNull();
    }

    @Test
    void markAsPublished를_호출하면_상태가_PUBLISHED로_변경된다() {
        // given
        OutboxEvent event = new OutboxEvent("order-123", "OrderCreated", "{}");
        LocalDateTime beforePublish = LocalDateTime.now();

        // when
        event.markAsPublished();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
        assertThat(event.getPublishedAt()).isAfter(beforePublish);
    }

    @Test
    void markAsFailed를_호출하면_상태가_FAILED로_변경된다() {
        // given
        OutboxEvent event = new OutboxEvent("order-123", "OrderCreated", "{}");

        // when
        event.markAsFailed();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getPublishedAt()).isNull();
    }

    @Test
    void setId로_ID를_설정할_수_있다() {
        // given
        OutboxEvent event = new OutboxEvent("order-123", "OrderCreated", "{}");
        Long id = 1L;

        // when
        event.setId(id);

        // then
        assertThat(event.getId()).isEqualTo(id);
    }
}