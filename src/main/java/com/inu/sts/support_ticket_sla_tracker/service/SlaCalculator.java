package com.inu.sts.support_ticket_sla_tracker.service;

import com.inu.sts.support_ticket_sla_tracker.domain.Priority;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * SLA: dueAt = createdAt + duration(priority).
 * Breached when not RESOLVED/CLOSED by dueAt. Durations: LOW 72h, MEDIUM 48h, HIGH 24h, CRITICAL 4h.
 */
public final class SlaCalculator {

    private static final Map<Priority, Duration> DURATIONS = Map.of(
            Priority.LOW, Duration.ofHours(72),
            Priority.MEDIUM, Duration.ofHours(48),
            Priority.HIGH, Duration.ofHours(24),
            Priority.CRITICAL, Duration.ofHours(4)
    );

    private SlaCalculator() {
    }

    public static Instant dueAt(Instant createdAt, Priority priority) {
        return createdAt.plus(DURATIONS.get(priority));
    }

    public static long remainingSeconds(Instant dueAt, Instant now) {
        return Duration.between(now, dueAt).getSeconds();
    }
}
