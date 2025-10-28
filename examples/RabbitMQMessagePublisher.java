package examples;

import com.company.outbox.core.MessagePublisher;
import com.company.outbox.core.OutboxEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 메시지 발행을 위한 예제 구현체
 * 각 서비스에서 필요에 따라 다른 메시징 시스템으로 구현 가능
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQMessagePublisher implements MessagePublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    public void publish(OutboxEvent event) {
        try {
            // Exchange와 Routing Key 결정
            String exchange = determineExchange(event.getType());
            String routingKey = determineRoutingKey(event);
            
            // Message는 payload 그대로 사용
            String message = event.getPayload();
            
            // RabbitMQ로 발행
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            
            log.debug("Published message to RabbitMQ - exchange: {}, routingKey: {}, eventType: {}", 
                     exchange, routingKey, event.getType());
                     
        } catch (Exception e) {
            log.error("Failed to publish message to RabbitMQ - eventId: {}, eventType: {}, error: {}", 
                     event.getId(), event.getType(), e.getMessage(), e);
            throw e;
        }
    }
    
    private String determineExchange(String eventType) {
        return switch (eventType) {
            case "UserCreated", "UserUpdated", "UserDeleted" -> "user.exchange";
            case "OrderCreated", "OrderUpdated", "OrderCancelled" -> "order.exchange";
            default -> "default.exchange";
        };
    }
    
    private String determineRoutingKey(OutboxEvent event) {
        // 이벤트 타입과 서비스명을 조합한 라우팅 키
        return event.getType().toLowerCase() + ".event";
    }
}