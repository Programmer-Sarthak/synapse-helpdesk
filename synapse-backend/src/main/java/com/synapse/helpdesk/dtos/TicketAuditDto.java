package com.synapse.helpdesk.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketAuditDto {
    private Long id;
    private String action;
    private String details;
    private String changedBy;
    private LocalDateTime createdAt;
}