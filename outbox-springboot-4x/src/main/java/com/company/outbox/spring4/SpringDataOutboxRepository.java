package com.company.outbox.spring4;

import com.company.outbox.core.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataOutboxRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findByStatusOrderByCreatedAt(OutboxEventStatus status);
}