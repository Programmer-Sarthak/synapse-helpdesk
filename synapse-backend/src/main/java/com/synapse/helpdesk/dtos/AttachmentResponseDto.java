package com.synapse.helpdesk.dtos;

import lombok.Data;

@Data
public class AttachmentResponseDto {
    private Long id;
    private String fileName;
    private String fileType;
}