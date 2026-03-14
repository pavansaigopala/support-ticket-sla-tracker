package com.inu.sts.support_ticket_sla_tracker.controller.v1;

import com.inu.sts.support_ticket_sla_tracker.dto.AuditEventResponse;
import com.inu.sts.support_ticket_sla_tracker.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditControllerV1 {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditEventResponse>> getAudit(
            @RequestParam String entityType,
            @RequestParam UUID entityId
    ) {
        List<AuditEventResponse> events = auditService.getAuditHistory(entityType, entityId);
        return ResponseEntity.ok(events);
    }
}
