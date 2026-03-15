package com.inu.sts.support_ticket_sla_tracker.controller.v1;

import com.inu.sts.support_ticket_sla_tracker.dto.AuditEventResponse;
import com.inu.sts.support_ticket_sla_tracker.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditControllerV1.class)
class AuditControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    private final UUID entityId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @Test
    @WithMockUser(roles = "AGENT")
    void getAudit_returns200WithEvents() throws Exception {
        AuditEventResponse event = AuditEventResponse.builder()
                .id(UUID.randomUUID())
                .entityType("TICKET")
                .entityId(entityId)
                .eventType("TICKET_CREATED")
                .payload("title=Test")
                .createdAt(now)
                .build();
        when(auditService.getAuditHistory(eq("TICKET"), eq(entityId)))
                .thenReturn(List.of(event));

        mockMvc.perform(get("/api/v1/audit")
                        .param("entityType", "TICKET")
                        .param("entityId", entityId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].entityType").value("TICKET"))
                .andExpect(jsonPath("$[0].entityId").value(entityId.toString()))
                .andExpect(jsonPath("$[0].eventType").value("TICKET_CREATED"))
                .andExpect(jsonPath("$[0].payload").value("title=Test"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void getAudit_whenNoEvents_returns200EmptyList() throws Exception {
        when(auditService.getAuditHistory(eq("TICKET"), eq(entityId)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/audit")
                        .param("entityType", "TICKET")
                        .param("entityId", entityId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
