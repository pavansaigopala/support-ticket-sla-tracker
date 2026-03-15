package com.inu.sts.support_ticket_sla_tracker.service;

import com.inu.sts.support_ticket_sla_tracker.domain.AuditEvent;
import com.inu.sts.support_ticket_sla_tracker.dto.AuditEventResponse;
import com.inu.sts.support_ticket_sla_tracker.mapper.AuditEventMapper;
import com.inu.sts.support_ticket_sla_tracker.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;
    @Mock
    private AuditEventMapper auditEventMapper;

    @InjectMocks
    private AuditService auditService;

    @Test
    void getAuditHistory_returnsMappedResponses() {
        UUID entityId = UUID.randomUUID();
        String entityType = "TICKET";
        AuditEvent event1 = AuditEvent.builder()
                .id(UUID.randomUUID())
                .entityType(entityType)
                .entityId(entityId)
                .eventType("TICKET_CREATED")
                .createdAt(Instant.now())
                .build();
        AuditEvent event2 = AuditEvent.builder()
                .id(UUID.randomUUID())
                .entityType(entityType)
                .entityId(entityId)
                .eventType("STATUS_CHANGED")
                .createdAt(Instant.now())
                .build();
        AuditEventResponse response1 = AuditEventResponse.builder().id(event1.getId()).eventType("TICKET_CREATED").build();
        AuditEventResponse response2 = AuditEventResponse.builder().id(event2.getId()).eventType("STATUS_CHANGED").build();

        when(auditEventRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc(entityType, entityId))
                .thenReturn(List.of(event1, event2));
        when(auditEventMapper.toResponse(event1)).thenReturn(response1);
        when(auditEventMapper.toResponse(event2)).thenReturn(response2);

        List<AuditEventResponse> result = auditService.getAuditHistory(entityType, entityId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isSameAs(response1);
        assertThat(result.get(1)).isSameAs(response2);
        verify(auditEventRepository).findByEntityTypeAndEntityIdOrderByCreatedAtAsc(entityType, entityId);
    }

    @Test
    void getAuditHistory_whenNoEvents_returnsEmptyList() {
        UUID entityId = UUID.randomUUID();
        when(auditEventRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc("TICKET", entityId))
                .thenReturn(List.of());

        List<AuditEventResponse> result = auditService.getAuditHistory("TICKET", entityId);

        assertThat(result).isEmpty();
    }
}
