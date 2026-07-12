package com.synapse.helpdesk.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequestDto {
    @NotBlank(message = "Comment text cannot be empty")
    private String text;
}