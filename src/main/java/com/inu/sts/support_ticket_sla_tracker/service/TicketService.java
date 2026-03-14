package com.inu.sts.support_ticket_sla_tracker.service;

import com.inu.sts.support_ticket_sla_tracker.domain.*;
import com.inu.sts.support_ticket_sla_tracker.dto.*;
import com.inu.sts.support_ticket_sla_tracker.exception.BadRequestException;
import com.inu.sts.support_ticket_sla_tracker.exception.ResourceNotFoundException;
import com.inu.sts.support_ticket_sla_tracker.mapper.CommentMapper;
import com.inu.sts.support_ticket_sla_tracker.mapper.TicketMapper;
import com.inu.sts.support_ticket_sla_tracker.repository.TicketCommentRepository;
import com.inu.sts.support_ticket_sla_tracker.repository.TicketRepository;
import com.inu.sts.support_ticket_sla_tracker.repository.TicketSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Ticket CRUD, list (filter/pagination/sort), status updates, comments.
 * Business rules: (1) Cannot transition from CLOSED. (2) RESOLVED requires ≥1 comment.
 * PATCH uses optimistic locking via @Version; concurrent update returns 409.
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketMapper ticketMapper;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public TicketResponse getById(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse create(TicketCreateRequest request) {
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(TicketStatus.NEW)
                .customerId(request.getCustomerId())
                .build();
        ticket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> list(TicketStatus status, Priority priority, String customerId,
                                            String search, Boolean slaBreached, Pageable pageable) {
        var spec = TicketSpecifications.withFilters(status, priority, customerId, search, slaBreached);
        Page<Ticket> page = ticketRepository.findAll(spec, pageable);
        return PageResponse.<TicketResponse>builder()
                .content(page.getContent().stream().map(ticketMapper::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional
    public TicketResponse update(UUID id, TicketUpdateRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
        if (request.getTitle() != null) ticket.setTitle(request.getTitle());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getPriority() != null) ticket.setPriority(request.getPriority());
        ticket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(UUID id, StatusUpdateRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
        TicketStatus newStatus = request.getStatus();

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Cannot change status from CLOSED");
        }
        if (newStatus == TicketStatus.RESOLVED) {
            long count = commentRepository.countByTicketId(id);
            if (count < 1) {
                throw new BadRequestException("RESOLVED requires at least one comment");
            }
        }

        ticket.setStatus(newStatus);
        ticket = ticketRepository.save(ticket);
        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public CommentResponse addComment(UUID ticketId, CommentCreateRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .comment(request.getComment())
                .author(request.getAuthor())
                .build();
        comment = commentRepository.save(comment);
        return commentMapper.toResponse(comment);
    }
}
