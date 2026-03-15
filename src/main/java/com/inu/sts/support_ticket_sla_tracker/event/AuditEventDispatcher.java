package com.inu.sts.support_ticket_sla_tracker.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for AuditEventPayload and delegates to async listener (so main thread returns immediately).
 */
@Component
@RequiredArgsConstructor
public class AuditEventDispatcher {

    private final AuditEventAsyncListener auditEventAsyncListener;

    @EventListener
    public void onAuditEvent(AuditEventPayload event) {
        auditEventAsyncListener.handleAuditEvent(event);
    }
}
