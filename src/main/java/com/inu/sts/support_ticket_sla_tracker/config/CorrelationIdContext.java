package com.inu.sts.support_ticket_sla_tracker.config;

import org.slf4j.MDC;

/**
 * Read correlation id from MDC (set by CorrelationIdFilter). Use when publishing events
 * so async listener can log with same id.
 */
public final class CorrelationIdContext {

    private CorrelationIdContext() {
    }

    public static String get() {
        String id = MDC.get(CorrelationIdFilter.MDC_KEY);
        return id != null ? id : "";
    }
}
