# Support Ticket & SLA Tracker

Spring Boot microservice: manage tickets, track SLAs, audit changes. (Inu Technology assessment.)

---

## Run locally

**Need:** Java 21, Maven.

```bash
./mvnw spring-boot:run
```

→ `http://localhost:8080` · API: `/api/v1` · Swagger: `/swagger-ui.html` · H2 console: `/h2-console`

Uses **H2 in-memory** by default (no PostgreSQL required).

**Auth (Basic):** Send header `Authorization: Basic <base64(username:password)`.
- **agent** / **agent** → role AGENT (read + write).
- **viewer** / **viewer** → role VIEWER (read only). Write calls return 403.

---

## DB migrations

- **Flyway** – scripts in `src/main/resources/db/migration/`
- Runs on app startup. Use `V1__...sql`, `V2__...sql`, etc.

---

## API (v1)

| Action | Method | Path |
|--------|--------|------|
| Create ticket | POST | `/api/v1/tickets` |
| Get ticket | GET | `/api/v1/tickets/{id}` |
| List (filter, page, sort) | GET | `/api/v1/tickets` |
| Update ticket | PATCH | `/api/v1/tickets/{id}` |
| Change status | POST | `/api/v1/tickets/{id}/status` |
| Add comment | POST | `/api/v1/tickets/{id}/comments` |
| Get SLA | GET | `/api/v1/tickets/{id}/sla` |
| Get audit | GET | `/api/v1/audit?entityType=TICKET&entityId={id}` |

Use **Postman** or **Swagger UI** for examples.

---

## Design notes

- **Layers:** controller → service → repository; DTOs at API.
- **SLA breach:** Flag on ticket + scheduled job; one audit event per breach.
- **Audit:** Async (domain events); main API not blocked.
- **Auth:** TBD (Basic or JWT); roles: AGENT (read/write), VIEWER (read).

More: [docs/architecture.md](docs/architecture.md), [docs/database-schema.md](docs/database-schema.md), [docs/sequence-flows.md](docs/sequence-flows.md).

---

## Tests

- **Run:** `./mvnw test`
- **Coverage:** After `./mvnw test`, open `target/site/jacoco/index.html`
- Unit tests for TicketService and SlaService (mocked repos)
- **Health:** `/actuator/health` · **Metrics:** `/actuator/metrics`
- **Logs:** Use header `X-Correlation-Id` (or auto-generated) for tracing.
