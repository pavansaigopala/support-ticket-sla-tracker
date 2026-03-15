package com.inu.sts.support_ticket_sla_tracker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AuditEventPublisher auditEventPublisher;

    @Test
    void publish_sendsAuditEventPayloadWithCorrectFields() {
        UUID entityId = UUID.randomUUID();
        String payload = "status=RESOLVED";

        auditEventPublisher.publish("TICKET", entityId, "STATUS_CHANGED", payload);

        ArgumentCaptor<AuditEventPayload> captor = ArgumentCaptor.forClass(AuditEventPayload.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        AuditEventPayload event = captor.getValue();
        assertThat(event.getEntityType()).isEqualTo("TICKET");
        assertThat(event.getEntityId()).isEqualTo(entityId);
        assertThat(event.getEventType()).isEqualTo("STATUS_CHANGED");
        assertThat(event.getPayload()).isEqualTo(payload);
    }
}
