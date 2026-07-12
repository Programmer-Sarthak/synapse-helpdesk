package com.synapse.helpdesk.controllers;

import com.synapse.helpdesk.dtos.AttachmentResponseDto;
import com.synapse.helpdesk.models.Attachment;
import com.synapse.helpdesk.services.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<AttachmentResponseDto> upload(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(attachmentService.saveAttachment(ticketId, file));
    }

    @GetMapping
    public ResponseEntity<List<AttachmentResponseDto>> getAttachments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsForTicket(ticketId));
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<byte[]> download(@PathVariable Long attachmentId) {
        Attachment attachment = attachmentService.getAttachment(attachmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .body(attachment.getData());
    }
}