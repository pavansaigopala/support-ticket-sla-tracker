package com.inu.sts.support_ticket_sla_tracker.repository;

import com.inu.sts.support_ticket_sla_tracker.domain.Ticket;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    /** Tickets not resolved/closed and not yet marked SLA breached (for scheduled job). */
    List<Ticket> findByStatusNotInAndSlaBreachedFalse(List<TicketStatus> statuses);
}
