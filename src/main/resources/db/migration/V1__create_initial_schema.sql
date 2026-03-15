-- Support Ticket & SLA Tracker - Initial schema
-- Tables: ticket, ticket_comment, audit_event

CREATE TABLE ticket (
    id              UUID PRIMARY KEY,
    title           VARCHAR(120) NOT NULL,
    description     VARCHAR(2000),
    priority        VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    customer_id     VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         BIGINT NOT NULL DEFAULT 0,
    sla_breached    BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_ticket_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    CONSTRAINT chk_ticket_status CHECK (status IN ('NEW', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED'))
);

CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_priority ON ticket(priority);
CREATE INDEX idx_ticket_customer_id ON ticket(customer_id);
CREATE INDEX idx_ticket_created_at ON ticket(created_at);
CREATE INDEX idx_ticket_sla_breached ON ticket(sla_breached);

CREATE TABLE ticket_comment (
    id              UUID PRIMARY KEY,
    ticket_id       UUID NOT NULL,
    comment         VARCHAR(1000) NOT NULL,
    author          VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES ticket(id) ON DELETE CASCADE
);

CREATE INDEX idx_ticket_comment_ticket_id ON ticket_comment(ticket_id);

CREATE TABLE audit_event (
    id              UUID PRIMARY KEY,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       UUID NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    payload         TEXT,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_entity ON audit_event(entity_type, entity_id);
CREATE INDEX idx_audit_created_at ON audit_event(created_at);
