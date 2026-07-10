package com.synapse.helpdesk.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketRegistrationDto {

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "description is required")
    private String description;
}
