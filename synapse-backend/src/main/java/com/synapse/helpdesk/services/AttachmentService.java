package com.synapse.helpdesk.services;

import com.synapse.helpdesk.dtos.AttachmentResponseDto;
import com.synapse.helpdesk.models.Attachment;
import com.synapse.helpdesk.models.Ticket;
import com.synapse.helpdesk.repositories.AttachmentRepository;
import com.synapse.helpdesk.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;

    public AttachmentResponseDto saveAttachment(Long ticketId, MultipartFile file) throws Exception {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Attachment attachment = new Attachment();
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setData(file.getBytes());
        attachment.setTicket(ticket);

        Attachment saved = attachmentRepository.save(attachment);

        AttachmentResponseDto dto = new AttachmentResponseDto();
        dto.setId(saved.getId());
        dto.setFileName(saved.getFileName());
        dto.setFileType(saved.getFileType());
        return dto;
    }

    public Attachment getAttachment(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }

    public List<AttachmentResponseDto> getAttachmentsForTicket(Long ticketId) {
        return attachmentRepository.findByTicketId(ticketId).stream().map(a -> {
            AttachmentResponseDto dto = new AttachmentResponseDto();
            dto.setId(a.getId());
            dto.setFileName(a.getFileName());
            dto.setFileType(a.getFileType());
            return dto;
        }).collect(Collectors.toList());
    }
}