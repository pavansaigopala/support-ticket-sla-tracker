package com.inu.sts.support_ticket_sla_tracker.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Publishes audit events. Listener persists asynchronously so main API is not blocked.
 */
@Component
public class AuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public AuditEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(String entityType, UUID entityId, String eventType, String payload) {
        String correlationId = com.inu.sts.support_ticket_sla_tracker.config.CorrelationIdContext.get();
        applicationEventPublisher.publishEvent(
                new AuditEventPayload(this, entityType, entityId, eventType, payload, correlationId));
    }
}
