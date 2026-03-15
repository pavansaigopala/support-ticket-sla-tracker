package com.inu.sts.support_ticket_sla_tracker.mapper;

import com.inu.sts.support_ticket_sla_tracker.domain.Ticket;
import com.inu.sts.support_ticket_sla_tracker.domain.TicketComment;
import com.inu.sts.support_ticket_sla_tracker.dto.CommentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private CommentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CommentMapperImpl();
    }

    @Test
    void toResponse_mapsAllFields_includingTicketId() {
        UUID commentId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        Instant now = Instant.now();
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .title("Ticket")
                .build();
        TicketComment comment = TicketComment.builder()
                .id(commentId)
                .ticket(ticket)
                .comment("Please check logs")
                .author("agent1")
                .createdAt(now)
                .build();

        CommentResponse response = mapper.toResponse(comment);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(commentId);
        assertThat(response.getTicketId()).isEqualTo(ticketId);
        assertThat(response.getComment()).isEqualTo("Please check logs");
        assertThat(response.getAuthor()).isEqualTo("agent1");
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_commentWithNullTicket_mapsTicketIdAsNull() {
        UUID commentId = UUID.randomUUID();
        TicketComment comment = TicketComment.builder()
                .id(commentId)
                .ticket(null)
                .comment("Comment")
                .author("user")
                .createdAt(Instant.now())
                .build();

        CommentResponse response = mapper.toResponse(comment);

        assertThat(response).isNotNull();
        assertThat(response.getTicketId()).isNull();
        assertThat(response.getComment()).isEqualTo("Comment");
    }

    @Test
    void toResponse_nullComment_returnsNull() {
        CommentResponse response = mapper.toResponse(null);
        assertThat(response).isNull();
    }
}
