package com.inu.sts.support_ticket_sla_tracker.event;

import com.inu.sts.support_ticket_sla_tracker.domain.AuditEvent;
import com.inu.sts.support_ticket_sla_tracker.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Persists audit events asynchronously. Failures are logged (with correlation id) and do not fail the main API.
 */
@Component
@RequiredArgsConstructor
public class AuditEventAsyncListener {

    private static final Logger log = LoggerFactory.getLogger(AuditEventAsyncListener.class);

    private final AuditEventRepository auditEventRepository;

    @Async
    @Transactional(rollbackFor = Exception.class)
    public void handleAuditEvent(AuditEventPayload event) {
        String correlationId = event.getCorrelationId();
        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put("correlationId", correlationId);
        }
        try {
            AuditEvent entity = AuditEvent.builder()
                    .entityType(event.getEntityType())
                    .entityId(event.getEntityId())
                    .eventType(event.getEventType())
                    .payload(event.getPayload())
                    .createdAt(Instant.now())
                    .build();
            auditEventRepository.save(entity);
            log.debug("Audit event persisted: {} {} {}", event.getEntityType(), event.getEntityId(), event.getEventType());
        } catch (Exception e) {
            log.warn("Failed to persist audit event (entityType={}, entityId={}, eventType={}): {}",
                    event.getEntityType(), event.getEntityId(), event.getEventType(), e.getMessage());
        } finally {
            if (correlationId != null && !correlationId.isBlank()) {
                MDC.remove("correlationId");
            }
        }
    }
}
