package com.inu.sts.support_ticket_sla_tracker.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inu.sts.support_ticket_sla_tracker.domain.Priority;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import com.inu.sts.support_ticket_sla_tracker.dto.*;
import com.inu.sts.support_ticket_sla_tracker.exception.ResourceNotFoundException;
import com.inu.sts.support_ticket_sla_tracker.service.SlaService;
import com.inu.sts.support_ticket_sla_tracker.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketControllerV1.class)
class TicketControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private SlaService slaService;

    private final UUID ticketId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @Test
    @WithMockUser(roles = "AGENT")
    void create_returns201() throws Exception {
        TicketCreateRequest request = TicketCreateRequest.builder()
                .title("Test ticket")
                .priority(Priority.HIGH)
                .customerId("cust1")
                .build();
        TicketResponse response = TicketResponse.builder()
                .id(ticketId)
                .title("Test ticket")
                .status(TicketStatus.NEW)
                .priority(Priority.HIGH)
                .customerId("cust1")
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();
        when(ticketService.create(any(TicketCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.title").value("Test ticket"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    @WithMockUser(roles = "AGENT")
    void getById_returns200() throws Exception {
        TicketResponse response = TicketResponse.builder()
                .id(ticketId)
                .title("Test")
                .status(TicketStatus.NEW)
                .build();
        when(ticketService.getById(ticketId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.title").value("Test"));
    }

    @Test
    @WithMockUser(roles = "AGENT")
    void getById_whenNotFound_returns404() throws Exception {
        when(ticketService.getById(ticketId)).thenThrow(new ResourceNotFoundException("Ticket not found: " + ticketId));

        mockMvc.perform(get("/api/v1/tickets/{id}", ticketId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void list_returns200() throws Exception {
        PageResponse<TicketResponse> page = PageResponse.<TicketResponse>builder()
                .content(List.of())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .build();
        when(ticketService.list(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/tickets")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "AGENT")
    void update_returns200() throws Exception {
        TicketUpdateRequest request = TicketUpdateRequest.builder()
                .title("Updated title")
                .build();
        TicketResponse response = TicketResponse.builder()
                .id(ticketId)
                .title("Updated title")
                .status(TicketStatus.NEW)
                .build();
        when(ticketService.update(eq(ticketId), any(TicketUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/tickets/{id}", ticketId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"));
    }

    @Test
    @WithMockUser(roles = "AGENT")
    void updateStatus_returns200() throws Exception {
        StatusUpdateRequest request = StatusUpdateRequest.builder()
                .status(TicketStatus.IN_PROGRESS)
                .build();
        TicketResponse response = TicketResponse.builder()
                .id(ticketId)
                .status(TicketStatus.IN_PROGRESS)
                .build();
        when(ticketService.updateStatus(eq(ticketId), any(StatusUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tickets/{id}/status", ticketId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(roles = "AGENT")
    void addComment_returns201() throws Exception {
        CommentCreateRequest request = CommentCreateRequest.builder()
                .comment("A comment")
                .author("agent")
                .build();
        CommentResponse response = CommentResponse.builder()
                .id(UUID.randomUUID())
                .ticketId(ticketId)
                .comment("A comment")
                .author("agent")
                .createdAt(now)
                .build();
        when(ticketService.addComment(eq(ticketId), any(CommentCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tickets/{id}/comments", ticketId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comment").value("A comment"))
                .andExpect(jsonPath("$.ticketId").value(ticketId.toString()));
    }

    @Test
    @WithMockUser(roles = "AGENT")
    void getSla_returns200() throws Exception {
        SlaResponse response = SlaResponse.builder()
                .dueAt(now.plusSeconds(86400))
                .breached(false)
                .remainingSeconds(86400L)
                .build();
        when(slaService.getSla(ticketId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/tickets/{id}/sla", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.breached").value(false))
                .andExpect(jsonPath("$.remainingSeconds").value(86400));
    }
}
