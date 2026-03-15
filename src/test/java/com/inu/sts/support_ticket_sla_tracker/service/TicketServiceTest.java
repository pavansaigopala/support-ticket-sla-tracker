package com.inu.sts.support_ticket_sla_tracker.service;

import com.inu.sts.support_ticket_sla_tracker.domain.*;
import com.inu.sts.support_ticket_sla_tracker.dto.*;
import com.inu.sts.support_ticket_sla_tracker.event.AuditEventPublisher;
import com.inu.sts.support_ticket_sla_tracker.exception.BadRequestException;
import com.inu.sts.support_ticket_sla_tracker.exception.ResourceNotFoundException;
import com.inu.sts.support_ticket_sla_tracker.mapper.CommentMapper;
import com.inu.sts.support_ticket_sla_tracker.mapper.TicketMapper;
import com.inu.sts.support_ticket_sla_tracker.repository.TicketCommentRepository;
import com.inu.sts.support_ticket_sla_tracker.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketCommentRepository commentRepository;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private AuditEventPublisher auditEventPublisher;

    @InjectMocks
    private TicketService ticketService;

    private Ticket ticket;
    private TicketResponse ticketResponse;
    private UUID ticketId;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        Instant now = Instant.now();
        ticket = Ticket.builder()
                .id(ticketId)
                .title("Test")
                .priority(Priority.HIGH)
                .status(TicketStatus.NEW)
                .customerId("cust1")
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();
        ticketResponse = TicketResponse.builder()
                .id(ticketId)
                .title("Test")
                .status(TicketStatus.NEW)
                .build();
    }

    @Test
    void getById_whenNotFound_throwsResourceNotFoundException() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getById(ticketId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }

    @Test
    void getById_whenFound_returnsResponse() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

        TicketResponse result = ticketService.getById(ticketId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ticketId);
        assertThat(result.getTitle()).isEqualTo("Test");
    }

    @Test
    void create_savesTicketAndReturnsResponse() {
        TicketCreateRequest request = TicketCreateRequest.builder()
                .title("New ticket")
                .priority(Priority.MEDIUM)
                .customerId("cust1")
                .build();
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(ticketId);
            return t;
        });
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(ticketResponse);

        TicketResponse result = ticketService.create(request);

        assertThat(result).isNotNull();
        verify(ticketRepository).save(any(Ticket.class));
        verify(auditEventPublisher).publish(eq("TICKET"), eq(ticketId), eq("TICKET_CREATED"), anyString());
    }

    @Test
    void updateStatus_whenFromClosed_throwsBadRequest() {
        ticket.setStatus(TicketStatus.CLOSED);
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        StatusUpdateRequest request = StatusUpdateRequest.builder().status(TicketStatus.IN_PROGRESS).build();

        assertThatThrownBy(() -> ticketService.updateStatus(ticketId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot change status from CLOSED");
    }

    @Test
    void updateStatus_toResolvedWithoutComment_throwsBadRequest() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(commentRepository.countByTicketId(ticketId)).thenReturn(0L);
        StatusUpdateRequest request = StatusUpdateRequest.builder().status(TicketStatus.RESOLVED).build();

        assertThatThrownBy(() -> ticketService.updateStatus(ticketId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least one comment");
    }

    @Test
    void updateStatus_toResolvedWithComment_succeeds() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(commentRepository.countByTicketId(ticketId)).thenReturn(1L);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);
        StatusUpdateRequest request = StatusUpdateRequest.builder().status(TicketStatus.RESOLVED).build();

        TicketResponse result = ticketService.updateStatus(ticketId, request);

        assertThat(result).isNotNull();
        verify(ticketRepository).save(ticket);
        verify(auditEventPublisher).publish(eq("TICKET"), eq(ticketId), eq("STATUS_CHANGED"), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void list_returnsPageResponse() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Ticket> page = new PageImpl<>(List.of(ticket), pageable, 1);
        when(ticketRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(ticketMapper.toResponse(ticket)).thenReturn(ticketResponse);

        PageResponse<TicketResponse> result = ticketService.list(null, null, null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(ticketId);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPage()).isZero();
        assertThat(result.getSize()).isEqualTo(20);
    }

    @Test
    void update_partialUpdate_savesAndReturnsResponse() {
        TicketUpdateRequest request = TicketUpdateRequest.builder()
                .title("Updated title")
                .build();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(ticketResponse);

        TicketResponse result = ticketService.update(ticketId, request);

        assertThat(result).isNotNull();
        assertThat(ticket.getTitle()).isEqualTo("Updated title");
        verify(ticketRepository).save(ticket);
    }

    @Test
    void update_priorityChange_publishesAuditEvent() {
        TicketUpdateRequest request = TicketUpdateRequest.builder()
                .priority(Priority.CRITICAL)
                .build();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(ticketResponse);

        ticketService.update(ticketId, request);

        verify(auditEventPublisher).publish(eq("TICKET"), eq(ticketId), eq("PRIORITY_CHANGED"), anyString());
    }

    @Test
    void update_whenTicketNotFound_throwsResourceNotFoundException() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());
        TicketUpdateRequest request = TicketUpdateRequest.builder().title("Title").build();

        assertThatThrownBy(() -> ticketService.update(ticketId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }

    @Test
    void addComment_savesAndReturnsResponse() {
        UUID commentId = UUID.randomUUID();
        CommentCreateRequest request = CommentCreateRequest.builder()
                .comment("Fix applied")
                .author("agent1")
                .build();
        CommentResponse commentResponse = CommentResponse.builder()
                .id(commentId)
                .ticketId(ticketId)
                .comment("Fix applied")
                .author("agent1")
                .build();
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any(TicketComment.class))).thenAnswer(inv -> {
            TicketComment c = inv.getArgument(0);
            c.setId(commentId);
            return c;
        });
        when(commentMapper.toResponse(any(TicketComment.class))).thenReturn(commentResponse);

        CommentResponse result = ticketService.addComment(ticketId, request);

        assertThat(result).isNotNull();
        assertThat(result.getComment()).isEqualTo("Fix applied");
        assertThat(result.getAuthor()).isEqualTo("agent1");
        verify(commentRepository).save(any(TicketComment.class));
        verify(auditEventPublisher).publish(eq("TICKET"), eq(ticketId), eq("COMMENT_ADDED"), anyString());
    }

    @Test
    void addComment_whenTicketNotFound_throwsResourceNotFoundException() {
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());
        CommentCreateRequest request = CommentCreateRequest.builder().comment("Hi").author("u").build();

        assertThatThrownBy(() -> ticketService.addComment(ticketId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }
}
