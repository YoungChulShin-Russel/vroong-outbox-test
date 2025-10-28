package com.company.outbox.core;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}