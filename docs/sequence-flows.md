# Sequence Flows for Key Operations

## Audit (async)

- All audit events use **AuditEventPayload** (entityType, entityId, eventType, payload, correlationId).
- **AuditEventDispatcher** receives the event and calls **AuditEventAsyncListener.handleAuditEvent** (@Async).
- Listener persists to **AuditEvent** table; on failure logs with correlation id and does not fail the main API.
- **CorrelationIdFilter** sets `X-Correlation-Id` from header or generates one; stored in MDC and response header.

## 1. Create Ticket

1. Client → `POST /api/v1/tickets` (body: title, description, priority, customerId).
2. Controller validates, maps to DTO, calls TicketService.create.
3. Service creates Ticket entity, saves via TicketRepository.
4. Service publishes AuditEventPayload (TICKET_CREATED).
5. Controller returns 201 + created ticket DTO.
6. Async: AuditEventAsyncListener persists AuditEvent (TICKET_CREATED).

## 2. Change Status (e.g. to RESOLVED)

1. Client → `POST /api/v1/tickets/{id}/status` (body: status).
2. Controller calls TicketService.updateStatus(id, status).
3. Service loads ticket; enforces: not from CLOSED; if status=RESOLVED, requires ≥1 comment.
4. Service updates status, saves; publishes StatusChangedEvent.
5. Controller returns 200.
6. Async: listener persists AuditEvent (STATUS_CHANGED).

## 3. SLA Breach Detection (Scheduled Job)

1. **SlaBreachJob** runs on cron (default: every minute, `sla.breach.cron=0 * * * * ?`). Disable with `sla.breach.job.enabled=false`.
2. **SlaBreachService.markBreachedAndPublishEvents()** finds tickets: status not in (RESOLVED, CLOSED), sla_breached=false, and dueAt &lt; now.
3. For each: set sla_breached=true, save ticket; publish AuditEventPayload (SLA_BREACHED).
4. Async listener persists one AuditEvent (SLA_BREACHED) per ticket. Flag prevents duplicate events.

## 4. Get Ticket SLA

1. Client → `GET /api/v1/tickets/{id}/sla`.
2. Controller calls SlaService.getSla(id).
3. Service loads ticket; computes dueAt from createdAt + priority duration; breached = (now > dueAt and status not RESOLVED/CLOSED); remainingSeconds.
4. Controller returns 200 with { dueAt, breached, remainingSeconds } or 404.

*(To be expanded with more flows as needed.)*
