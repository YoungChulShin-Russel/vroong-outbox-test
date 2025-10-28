package examples;

import com.company.outbox.core.MessagePublisher;
import com.company.outbox.core.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka 메시지 발행을 위한 예제 구현체
 * 각 서비스에서 이와 같은 형태로 MessagePublisher를 구현해서 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessagePublisher implements MessagePublisher {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Override
    public void publish(OutboxEvent event) {
        try {
            // Topic 결정 로직 (이벤트 타입 기반)
            String topic = determineTopicFromEventType(event.getType());
            
            // Key는 aggregateId 사용 (파티션 분산)
            String key = event.getAggregateId();
            
            // Message는 payload 그대로 사용
            String message = event.getPayload();
            
            // Kafka로 발행
            kafkaTemplate.send(topic, key, message);
            
            log.debug("Published message to Kafka - topic: {}, key: {}, eventType: {}", 
                     topic, key, event.getType());
                     
        } catch (Exception e) {
            log.error("Failed to publish message to Kafka - eventId: {}, eventType: {}, error: {}", 
                     event.getId(), event.getType(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 이벤트 타입에 따른 토픽 결정 로직
     * 각 서비스의 정책에 따라 구현
     */
    private String determineTopicFromEventType(String eventType) {
        return switch (eventType) {
            case "UserCreated", "UserUpdated", "UserDeleted" -> "user-events";
            case "OrderCreated", "OrderUpdated", "OrderCancelled" -> "order-events";
            case "PaymentCompleted", "PaymentFailed" -> "payment-events";
            default -> "default-events";  // 기본 토픽
        };
    }
}