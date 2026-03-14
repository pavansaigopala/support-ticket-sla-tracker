# Support Ticket & SLA Tracker

Spring Boot microservice: manage tickets, track SLAs, audit changes. (Inu Technology assessment.)

---

## Run locally

**Need:** Java 21, Maven, PostgreSQL.

1. **Build**
   ```bash
   ./mvnw clean install
   ```

2. **Create DB** (once)
   ```bash
   psql -U postgres -c "CREATE DATABASE support_ticket_db;"
   ```

3. **Start**
   ```bash
   ./mvnw spring-boot:run
   ```
   → `http://localhost:8080` · API: `/api/v1` · Swagger: `/swagger-ui.html`

---

## Env vars (optional)

| Variable | Default |
|----------|---------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/support_ticket_db` |
| `SPRING_DATASOURCE_USERNAME` | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` |

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

## Tests & observability

- **Tests:** `./mvnw test` · Coverage: `target/site/jacoco/index.html`
- **Health:** `/actuator/health` · **Metrics:** `/actuator/metrics`
- **Logs:** Use header `X-Correlation-Id` (or auto-generated) for tracing.
