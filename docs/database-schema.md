# Database Schema

## Entities (JPA)

- **Ticket** – `com.inu.sts.support_ticket_sla_tracker.domain.Ticket` (with `@Version` for optimistic locking)
- **TicketComment** – `TicketComment` (ManyToOne → Ticket)
- **AuditEvent** – `AuditEvent`
- **Enums** – `Priority` (LOW, MEDIUM, HIGH, CRITICAL), `TicketStatus` (NEW, IN_PROGRESS, ON_HOLD, RESOLVED, CLOSED)

List/filter for tickets uses `TicketSpecifications` + `JpaSpecificationExecutor` (status, priority, customerId, search, slaBreached, pagination, sort).

---

## Strategy

- **Migrations:** Flyway. Scripts under `src/main/resources/db/migration/`.
- **Naming:** `V{n}__description.sql`. Run on application startup.
- **JPA:** `spring.jpa.hibernate.ddl-auto=validate` so schema is owned by Flyway.

## Tables

### ticket

| Column        | Type      | Constraints |
|---------------|-----------|-------------|
| id            | UUID      | PK          |
| title         | VARCHAR(120) | NOT NULL |
| description   | VARCHAR(2000)  | nullable |
| priority      | VARCHAR(20)   | LOW, MEDIUM, HIGH, CRITICAL |
| status        | VARCHAR(20)   | NEW, IN_PROGRESS, ON_HOLD, RESOLVED, CLOSED |
| customer_id   | VARCHAR(255)  | NOT NULL |
| created_at    | TIMESTAMPTZ   | NOT NULL |
| updated_at    | TIMESTAMPTZ   | NOT NULL |
| version       | BIGINT        | NOT NULL (optimistic lock) |
| sla_breached  | BOOLEAN       | NOT NULL, default FALSE |

Indexes: status, priority, customer_id, created_at, sla_breached (partial).

### ticket_comment

| Column    | Type         | Constraints |
|-----------|--------------|-------------|
| id        | UUID         | PK          |
| ticket_id | UUID         | FK → ticket, CASCADE |
| comment   | VARCHAR(1000)| NOT NULL    |
| author    | VARCHAR(255) | NOT NULL    |
| created_at| TIMESTAMPTZ  | NOT NULL    |

Index: ticket_id.

### audit_event

| Column      | Type        | Constraints |
|-------------|-------------|-------------|
| id          | UUID        | PK          |
| entity_type | VARCHAR(50) | NOT NULL    |
| entity_id   | UUID        | NOT NULL    |
| event_type  | VARCHAR(100)| NOT NULL    |
| payload     | TEXT        | nullable    |
| created_at  | TIMESTAMPTZ | NOT NULL    |

Indexes: (entity_type, entity_id), created_at.

## ERD (Conceptual)

```
ticket 1───────* ticket_comment
  │
  └── (referenced by audit_event via entity_type='TICKET', entity_id=ticket.id)
```

*(An ERD image can be added here later if generated.)*
