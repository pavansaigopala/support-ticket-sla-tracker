# High-Level Architecture

## Overview

Support Ticket & SLA Tracker is a Spring Boot microservice with layered architecture, async audit, and role-based security.

## Layers

```
┌─────────────────────────────────────────────────────────────┐
│  REST API (v1) - Controllers                                 │
│  /api/v1/tickets, /api/v1/audit                              │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  Service Layer                                               │
│  TicketService, CommentService, AuditService, SlaService     │
│  Business rules, transactions, domain events                 │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  Repository Layer (JPA)                                      │
│  TicketRepository, TicketCommentRepository, AuditEventRepo   │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│  PostgreSQL                                                 │
│  Flyway migrations: ticket, ticket_comment, audit_event      │
└─────────────────────────────────────────────────────────────┘
```

## Cross-Cutting

- **Security:** Spring Security (Basic Auth or JWT); AGENT (read/write), VIEWER (read-only).
- **Audit:** Domain events → async listener → AuditEvent persistence.
- **SLA breach:** Scheduled job (e.g. every minute) → mark breached tickets → write SLA_BREACHED audit.
- **Observability:** Actuator (health, metrics), correlation id (X-Correlation-Id), structured logging.

## Design Notes

- DTOs at API boundary; MapStruct for entity ↔ DTO mapping.
- Global exception handler for consistent error responses and HTTP codes.
- Optimistic locking on Ticket via `version`; 409 on conflict.

*(To be expanded as implementation progresses.)*
