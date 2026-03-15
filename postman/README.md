# Postman collections

Use these with [Postman](https://www.postman.com/) or compatible tools (e.g. Insomnia can import Postman collections).

## Files

| File | Purpose |
|------|--------|
| `Support Ticket SLA Tracker.postman_collection.json` | All API requests (tickets + audit). |
| `Support Ticket SLA Tracker - Local.postman_environment.json` | Local server, **agent** (read + write). |
| `Support Ticket SLA Tracker - Viewer.postman_environment.json` | Local server, **viewer** (read only). |

## Setup

1. Import the collection: **Import** → select `Support Ticket SLA Tracker.postman_collection.json`.
2. (Optional) Import environments: **Import** → select the `*.postman_environment.json` files.
3. Start the app: `mvn spring-boot:run` (default port 8080).
4. In Postman, choose an environment (e.g. **Local (agent)**) from the top-right dropdown.
5. Run **Tickets → Create ticket**; the collection variable `ticketId` is set from the response so **Get ticket by ID**, **Add comment**, **Update status**, etc. work without editing.

## Endpoints in the collection

**Tickets** (`/api/v1/tickets`)

- Create ticket (POST)
- List tickets (GET, with filters and pagination)
- Get ticket by ID (GET `/{id}`)
- Update ticket (PATCH `/{id}`)
- Update ticket status (POST `/{id}/status`)
- Add comment (POST `/{id}/comments`)
- Get ticket SLA (GET `/{id}/sla`)

**Audit** (`/api/v1/audit`)

- Get audit history (GET, query: `entityType`, `entityId`)

## Auth

The collection uses **Basic Auth**. Variables `username` and `password` come from the collection (default `agent`/`agent`) or from the selected environment. Use the **Viewer** environment to verify that write calls return 403.
