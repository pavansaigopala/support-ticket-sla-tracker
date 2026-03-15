package com.inu.sts.support_ticket_sla_tracker.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event for audit: ticket created, status/priority changed, comment added, SLA breached.
 * Published from service layer; async listener persists to AuditEvent.
 */
@Getter
public class AuditEventPayload extends ApplicationEvent {

    private final String entityType;
    private final UUID entityId;
    private final String eventType;
    private final String payload;
    private final String correlationId;

    public AuditEventPayload(Object source, String entityType, UUID entityId, String eventType,
                             String payload, String correlationId) {
        super(source);
        this.entityType = entityType;
        this.entityId = entityId;
        this.eventType = eventType;
        this.payload = payload != null ? payload : "";
        this.correlationId = correlationId != null ? correlationId : "";
    }
}
