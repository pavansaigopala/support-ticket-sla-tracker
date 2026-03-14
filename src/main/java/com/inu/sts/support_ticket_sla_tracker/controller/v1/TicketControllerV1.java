package com.inu.sts.support_ticket_sla_tracker.controller.v1;

import com.inu.sts.support_ticket_sla_tracker.domain.Priority;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import com.inu.sts.support_ticket_sla_tracker.dto.*;
import com.inu.sts.support_ticket_sla_tracker.service.SlaService;
import com.inu.sts.support_ticket_sla_tracker.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketControllerV1 {

    private final TicketService ticketService;
    private final SlaService slaService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketCreateRequest request) {
        TicketResponse created = ticketService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TicketResponse>> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean slaBreached,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Sort.Order order = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(order));
        PageResponse<TicketResponse> result = ticketService.list(status, priority, customerId, search, slaBreached, pageable);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable UUID id, @Valid @RequestBody TicketUpdateRequest request) {
        return ResponseEntity.ok(ticketService.update(id, request));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable UUID id, @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse created = ticketService.addComment(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/sla")
    public ResponseEntity<SlaResponse> getSla(@PathVariable UUID id) {
        return ResponseEntity.ok(slaService.getSla(id));
    }

    private static Sort.Order parseSort(String sort) {
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return new Sort.Order(direction, property);
    }
}
