# Sequence Flows for Key Operations

## 1. Create Ticket

1. Client → `POST /api/v1/tickets` (body: title, description, priority, customerId).
2. Controller validates, maps to DTO, calls TicketService.create.
3. Service creates Ticket entity, saves via TicketRepository.
4. Service publishes domain event (e.g. TicketCreatedEvent).
5. Controller returns 201 + created ticket DTO.
6. Async: event listener persists AuditEvent (TICKET_CREATED).

## 2. Change Status (e.g. to RESOLVED)

1. Client → `POST /api/v1/tickets/{id}/status` (body: status).
2. Controller calls TicketService.updateStatus(id, status).
3. Service loads ticket; enforces: not from CLOSED; if status=RESOLVED, requires ≥1 comment.
4. Service updates status, saves; publishes StatusChangedEvent.
5. Controller returns 200.
6. Async: listener persists AuditEvent (STATUS_CHANGED).

## 3. SLA Breach Detection (Scheduled Job)

1. Scheduler runs every minute (configurable).
2. Job calls service to find tickets: SLA breached (due passed, not RESOLVED/CLOSED) and not yet marked (e.g. sla_breached=false or no SLA_BREACHED audit).
3. For each: set sla_breached=true (or equivalent), persist one AuditEvent (SLA_BREACHED).
4. Concurrency: ensure one event per ticket (e.g. flag or unique constraint).

## 4. Get Ticket SLA

1. Client → `GET /api/v1/tickets/{id}/sla`.
2. Controller calls SlaService.getSla(id).
3. Service loads ticket; computes dueAt from createdAt + priority duration; breached = (now > dueAt and status not RESOLVED/CLOSED); remainingSeconds.
4. Controller returns 200 with { dueAt, breached, remainingSeconds } or 404.

*(To be expanded with more flows as needed.)*
