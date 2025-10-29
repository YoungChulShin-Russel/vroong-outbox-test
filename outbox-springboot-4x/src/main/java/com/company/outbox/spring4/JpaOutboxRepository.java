package com.company.outbox.spring4;

import com.company.outbox.core.OutboxEvent;
import com.company.outbox.core.OutboxEventStatus;
import com.company.outbox.core.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaOutboxRepository implements OutboxRepository {
    
    private final SpringDataOutboxRepository springDataRepository;
    
    @Override
    @Transactional
    public OutboxEvent save(OutboxEvent event) {
        OutboxEventEntity entity = OutboxEventEntity.from(event);
        OutboxEventEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<OutboxEvent> findById(Long id) {
        return springDataRepository.findById(id)
                .map(OutboxEventEntity::toDomain);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findByStatus(OutboxEventStatus status) {
        return springDataRepository.findByStatusOrderByCreatedAt(status)
                .stream()
                .map(OutboxEventEntity::toDomain)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findPendingEvents() {
        return findByStatus(OutboxEventStatus.PENDING);
    }
    
    @Override
    @Transactional
    public void updateStatus(Long id, OutboxEventStatus status) {
        springDataRepository.findById(id).ifPresent(entity -> {
            if (status == OutboxEventStatus.PUBLISHED) {
                entity.markAsPublished();
            } else if (status == OutboxEventStatus.FAILED) {
                entity.markAsFailed();
            }
            springDataRepository.save(entity);
        });
    }
}