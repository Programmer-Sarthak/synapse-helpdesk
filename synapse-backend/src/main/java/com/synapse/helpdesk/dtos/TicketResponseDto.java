package com.synapse.helpdesk.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketResponseDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDateTime createdAt;
    private UserSummaryDto createdBy;
    private UserSummaryDto assignedTo;
}
