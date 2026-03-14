package com.inu.sts.support_ticket_sla_tracker.service;

import com.inu.sts.support_ticket_sla_tracker.domain.AuditEvent;
import com.inu.sts.support_ticket_sla_tracker.dto.AuditEventResponse;
import com.inu.sts.support_ticket_sla_tracker.mapper.AuditEventMapper;
import com.inu.sts.support_ticket_sla_tracker.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final AuditEventMapper auditEventMapper;

    @Transactional(readOnly = true)
    public List<AuditEventResponse> getAuditHistory(String entityType, UUID entityId) {
        List<AuditEvent> events = auditEventRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc(entityType, entityId);
        return events.stream().map(auditEventMapper::toResponse).toList();
    }
}
