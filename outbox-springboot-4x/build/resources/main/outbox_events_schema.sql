-- Outbox Events Table Schema
-- Compatible with MySQL, PostgreSQL, and other major databases

CREATE TABLE outbox_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_id VARCHAR(64) NOT NULL,
    type VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME NULL
);

-- Index for efficient processing of pending events
CREATE INDEX idx_outbox_events_status_created_at ON outbox_events(status, created_at);

-- Index for lookup by aggregate_id
CREATE INDEX idx_outbox_events_aggregate_id ON outbox_events(aggregate_id);

-- Index for lookup by type
CREATE INDEX idx_outbox_events_type ON outbox_events(type);