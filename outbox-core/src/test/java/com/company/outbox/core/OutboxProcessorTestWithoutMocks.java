package com.company.outbox.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxProcessorTestWithoutMocks {

    private TestOutboxRepository testRepository;
    private TestKafkaPublisher testPublisher;
    private OutboxProcessor outboxProcessor;

    @BeforeEach
    void setUp() {
        testRepository = new TestOutboxRepository();
        testPublisher = new TestKafkaPublisher();
        outboxProcessor = new OutboxProcessor(testRepository, testPublisher);
    }

    @Test
    void 대기중인_이벤트가_없으면_아무것도_처리하지_않는다() {
        // given - no pending events

        // when
        outboxProcessor.processOutboxEvents("test-topic");

        // then
        assertThat(testPublisher.getPublishedEvents()).isEmpty();
        assertThat(testRepository.getSavedEvents()).isEmpty();
    }

    @Test
    void 대기중인_이벤트를_Kafka로_발행하고_상태를_PUBLISHED로_변경한다() {
        // given
        OutboxEvent event = new OutboxEvent("order-123", "OrderCreated", "{\"orderId\":\"123\"}");
        event.setId(1L);
        testRepository.addPendingEvent(event);

        // when
        outboxProcessor.processOutboxEvents("test-topic");

        // then
        assertThat(testPublisher.getPublishedEvents()).hasSize(1);
        assertThat(testPublisher.getPublishedEvents().get(0).getAggregateId()).isEqualTo("order-123");
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
    }

    @Test
    void Kafka_발행에_실패하면_상태를_FAILED로_변경한다() {
        // given
        OutboxEvent event = new OutboxEvent("order-123", "OrderCreated", "{\"orderId\":\"123\"}");
        event.setId(1L);
        testRepository.addPendingEvent(event);
        testPublisher.setShouldFail(true);

        // when
        outboxProcessor.processOutboxEvents("test-topic");

        // then
        assertThat(testPublisher.getPublishedEvents()).isEmpty();
        assertThat(event.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(event.getPublishedAt()).isNull();
    }

    // Test doubles
    static class TestOutboxRepository implements OutboxRepository {
        private final List<OutboxEvent> pendingEvents = new ArrayList<>();
        private final List<OutboxEvent> savedEvents = new ArrayList<>();

        public void addPendingEvent(OutboxEvent event) {
            pendingEvents.add(event);
        }

        public List<OutboxEvent> getSavedEvents() {
            return savedEvents;
        }

        @Override
        public OutboxEvent save(OutboxEvent event) {
            savedEvents.add(event);
            return event;
        }

        @Override
        public Optional<OutboxEvent> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public List<OutboxEvent> findByStatus(OutboxEventStatus status) {
            return Collections.emptyList();
        }

        @Override
        public List<OutboxEvent> findPendingEvents() {
            return new ArrayList<>(pendingEvents);
        }

        @Override
        public void updateStatus(Long id, OutboxEventStatus status) {
        }
    }

    static class TestKafkaPublisher implements KafkaPublisher {
        private final List<OutboxEvent> publishedEvents = new ArrayList<>();
        private boolean shouldFail = false;

        public List<OutboxEvent> getPublishedEvents() {
            return publishedEvents;
        }

        public void setShouldFail(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public void publish(String topic, String key, String message) {
            if (shouldFail) {
                throw new RuntimeException("Kafka 연결 실패");
            }
        }

        @Override
        public void publish(OutboxEvent event, String topic) {
            if (shouldFail) {
                throw new RuntimeException("Kafka 연결 실패");
            }
            publishedEvents.add(event);
        }
    }
}