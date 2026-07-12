package com.synapse.helpdesk.controllers;

import com.synapse.helpdesk.dtos.TicketAuditDto;
import com.synapse.helpdesk.repositories.TicketAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets/{ticketId}/audit")
@RequiredArgsConstructor
public class TicketAuditController {

    private final TicketAuditRepository auditRepository;

    @GetMapping
    public ResponseEntity<List<TicketAuditDto>> getTicketAudit(@PathVariable Long ticketId) {
        List<TicketAuditDto> dtos = auditRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream().map(a -> {
                    TicketAuditDto dto = new TicketAuditDto();
                    dto.setId(a.getId());
                    dto.setAction(a.getAction());
                    dto.setDetails(a.getDetails());
                    dto.setChangedBy(a.getChangedBy());
                    dto.setCreatedAt(a.getCreatedAt());
                    return dto;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}