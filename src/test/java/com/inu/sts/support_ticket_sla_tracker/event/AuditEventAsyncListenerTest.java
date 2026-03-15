package com.inu.sts.support_ticket_sla_tracker.event;

import com.inu.sts.support_ticket_sla_tracker.domain.AuditEvent;
import com.inu.sts.support_ticket_sla_tracker.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventAsyncListenerTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @InjectMocks
    private AuditEventAsyncListener auditEventAsyncListener;

    @Test
    void handleAuditEvent_persistsAuditEventWithCorrectFields() {
        UUID entityId = UUID.randomUUID();
        AuditEventPayload payload = new AuditEventPayload(
                this, "TICKET", entityId, "TICKET_CREATED", "title=Test", "corr-1");

        auditEventAsyncListener.handleAuditEvent(payload);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        AuditEvent saved = captor.getValue();
        assertThat(saved.getEntityType()).isEqualTo("TICKET");
        assertThat(saved.getEntityId()).isEqualTo(entityId);
        assertThat(saved.getEventType()).isEqualTo("TICKET_CREATED");
        assertThat(saved.getPayload()).isEqualTo("title=Test");
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
