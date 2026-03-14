package com.inu.sts.support_ticket_sla_tracker.mapper;

import com.inu.sts.support_ticket_sla_tracker.domain.TicketComment;
import com.inu.sts.support_ticket_sla_tracker.dto.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(source = "ticket.id", target = "ticketId")
    CommentResponse toResponse(TicketComment comment);
}
