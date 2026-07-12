package com.synapse.helpdesk.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private Long id;
    private String text;
    private UserSummaryDto author;
    private LocalDateTime createdAt;
}