# Outbox 공통 모듈

MSA 환경에서 트랜잭션 안전성을 보장하면서 Kafka 이벤트를 발행하는 Outbox 패턴 공통 모듈입니다.

## 모듈 구조

- `outbox-core`: 순수 Java 기반 핵심 로직
- `outbox-springboot-4x`: Spring Boot 4.x 호환 어댑터

## 사용법

### 1. 의존성 추가

```gradle
dependencies {
    implementation 'com.company:outbox-springboot-4x:1.0.0'
}
```

### 2. 데이터베이스 테이블 생성

```sql
CREATE TABLE outbox_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_id VARCHAR(64) NOT NULL,
    type VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME NULL
);

-- 인덱스
CREATE INDEX idx_outbox_events_status_created_at ON outbox_events(status, created_at);
CREATE INDEX idx_outbox_events_aggregate_id ON outbox_events(aggregate_id);
CREATE INDEX idx_outbox_events_type ON outbox_events(type);
```

### 3. 설정

```yaml
# application.yml
outbox:
  kafka:
    default-topic: my-service-events
  scheduler:
    fixed-delay: 5000  # 5초마다 처리
```

### 4. 사용 예시

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    
    @Transactional
    public void createOrder(Order order) {
        // 1. 비즈니스 데이터 저장
        orderRepository.save(order);
        
        // 2. Outbox 이벤트 저장 (같은 트랜잭션)
        OutboxEvent event = new OutboxEvent(
            order.getId(),
            "OrderCreated", 
            "{\"orderId\":\"" + order.getId() + "\",\"amount\":" + order.getAmount() + "}"
        );
        outboxRepository.save(event);
    }
}
```

## 동작 원리

1. 비즈니스 트랜잭션 내에서 `outbox_events` 테이블에 이벤트 저장
2. 스케줄러가 주기적으로 `PENDING` 상태 이벤트 조회
3. Kafka로 이벤트 발행
4. 성공 시 상태를 `PUBLISHED`로 변경
5. 실패 시 상태를 `FAILED`로 변경

## 특징

- **트랜잭션 안전성**: 비즈니스 데이터와 이벤트가 같은 트랜잭션에서 처리
- **재시도 지원**: 실패한 이벤트는 `FAILED` 상태로 저장되어 추후 처리 가능
- **순서 보장**: `created_at` 기준으로 순차 처리
- **유연한 페이로드**: TEXT 타입으로 큰 이벤트도 저장 가능

## 다른 프로젝트에 적용하기

### 1. Local Repository 배포 (개발 테스트용)

```bash
# 로컬 Maven 저장소에 배포
./gradlew publishToMavenLocal

# 또는 특정 모듈만 배포
./gradlew :outbox-springboot-4x:publishToMavenLocal
```

### 2. 다른 프로젝트에서 사용

```gradle
// build.gradle
repositories {
    mavenLocal()  // 로컬 Maven 저장소 추가
    mavenCentral()
}

dependencies {
    implementation 'com.company:outbox-springboot-4x:1.0.0'
}
```

### 3. Spring Boot 자동 설정 활성화

모듈을 추가하면 자동으로 활성화되지만, 명시적으로 설정하려면:

```java
@SpringBootApplication
@EnableJpaRepositories  // JPA 사용 시
@EnableScheduling       // 스케줄러 사용 시
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 4. 설정 커스터마이징

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: user
    password: password
  
  jpa:
    hibernate:
      ddl-auto: validate  # 스키마 자동 생성 비활성화 권장

outbox:
  kafka:
    default-topic: ${spring.application.name}-events
  scheduler:
    fixed-delay: 3000  # 3초마다 처리 (기본값: 5초)

# Kafka 설정
spring:
  kafka:
    producer:
      bootstrap-servers: localhost:9092
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
```

### 5. 실제 사용 예시

```java
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final JsonSerializer jsonSerializer;  // 선택적으로 사용

    public void createUser(CreateUserRequest request) {
        // 1. 비즈니스 로직 실행
        User user = new User(request.getName(), request.getEmail());
        User savedUser = userRepository.save(user);
        
        // 2. 이벤트 페이로드 생성
        UserCreatedEvent eventPayload = new UserCreatedEvent(
            savedUser.getId(), 
            savedUser.getName(), 
            savedUser.getEmail()
        );
        
        // 3. Outbox 이벤트 저장 (같은 트랜잭션)
        OutboxEvent outboxEvent = new OutboxEvent(
            savedUser.getId().toString(),
            "UserCreated",
            jsonSerializer.serialize(eventPayload)  // 또는 직접 JSON 문자열
        );
        
        outboxRepository.save(outboxEvent);
    }
}

// 이벤트 DTO 예시
public record UserCreatedEvent(
    Long userId,
    String name,
    String email,
    Instant timestamp
) {
    public UserCreatedEvent(Long userId, String name, String email) {
        this(userId, name, email, Instant.now());
    }
}
```

### 6. 모니터링 및 운영

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxMonitor {
    private final OutboxRepository outboxRepository;
    
    @Scheduled(fixedRate = 60000) // 1분마다
    public void monitorFailedEvents() {
        List<OutboxEvent> failedEvents = outboxRepository.findByStatus(OutboxEventStatus.FAILED);
        if (!failedEvents.isEmpty()) {
            log.warn("발행 실패한 이벤트 {}개 발견", failedEvents.size());
            // 알림 발송 또는 별도 처리 로직
        }
    }
    
    @EventListener
    @Async
    public void handleFailedEvent(OutboxFailedEvent event) {
        // 실패한 이벤트에 대한 별도 처리 (예: 슬랙 알림)
        log.error("Outbox 이벤트 발행 실패: {}", event);
    }
}
```

### 7. 테스트 설정

```java
@SpringBootTest
@TestPropertySource(properties = {
    "outbox.scheduler.fixed-delay=1000",  // 테스트에서는 빠르게
    "spring.kafka.producer.bootstrap-servers=localhost:9092"
})
class OutboxIntegrationTest {
    
    @Autowired
    private OutboxRepository outboxRepository;
    
    @Test
    void 이벤트가_정상적으로_발행된다() {
        // given
        OutboxEvent event = new OutboxEvent("test-1", "TestEvent", "{}");
        
        // when
        outboxRepository.save(event);
        
        // then - 스케줄러가 처리할 때까지 대기
        await().atMost(10, SECONDS)
               .until(() -> outboxRepository.findById(event.getId())
                           .map(e -> e.getStatus() == OutboxEventStatus.PUBLISHED)
                           .orElse(false));
    }
}
```

### 8. 프로덕션 배포 체크리스트

- [ ] 데이터베이스 스키마 적용 완료
- [ ] Kafka 토픽 생성 및 권한 설정
- [ ] 모니터링 대시보드 설정
- [ ] 장애 알림 설정
- [ ] 성능 테스트 완료
- [ ] 롤백 계획 수립

## 요구사항

- Java 21+
- Spring Boot 4.0+
- 지원 데이터베이스: MySQL, PostgreSQL, H2 등
- Apache Kafka 2.8+

## 문제 해결

### 자주 발생하는 문제

1. **이벤트가 발행되지 않음**
   - 스케줄러 활성화 확인: `@EnableScheduling`
   - Kafka 연결 상태 확인
   - 로그 레벨을 DEBUG로 설정하여 확인

2. **트랜잭션 롤백 시 이벤트가 남아있음**
   - `@Transactional` 어노테이션 확인
   - 데이터소스 트랜잭션 매니저 설정 확인

3. **성능 이슈**
   - 인덱스 설정 확인
   - 스케줄러 실행 주기 조정
   - 배치 처리 크기 조정