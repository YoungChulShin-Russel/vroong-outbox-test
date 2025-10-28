package com.company.outbox.core;

import java.util.List;
import java.util.Optional;

public interface OutboxRepository {
    OutboxEvent save(OutboxEvent event);
    
    Optional<OutboxEvent> findById(Long id);
    
    List<OutboxEvent> findByStatus(OutboxEventStatus status);
    
    List<OutboxEvent> findPendingEvents();
    
    void updateStatus(Long id, OutboxEventStatus status);
}