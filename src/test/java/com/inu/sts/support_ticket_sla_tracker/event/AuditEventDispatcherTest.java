package com.inu.sts.support_ticket_sla_tracker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventDispatcherTest {

    @Mock
    private AuditEventAsyncListener auditEventAsyncListener;

    @InjectMocks
    private AuditEventDispatcher auditEventDispatcher;

    @Test
    void onAuditEvent_delegatesToAsyncListener() {
        AuditEventPayload event = new AuditEventPayload(
                this, "TICKET", UUID.randomUUID(), "COMMENT_ADDED", "commentId=xyz", "corr-1");

        auditEventDispatcher.onAuditEvent(event);

        verify(auditEventAsyncListener).handleAuditEvent(event);
    }
}
