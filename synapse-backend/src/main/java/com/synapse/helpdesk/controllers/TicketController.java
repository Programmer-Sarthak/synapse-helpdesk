package com.synapse.helpdesk.controllers;

import com.synapse.helpdesk.dtos.TicketRegistrationDto;
import com.synapse.helpdesk.dtos.TicketResponseDto;
import com.synapse.helpdesk.dtos.TicketStatsDTO;
import com.synapse.helpdesk.models.User;
import com.synapse.helpdesk.repositories.TicketRepository;
import com.synapse.helpdesk.services.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;

    @PostMapping
    public ResponseEntity<TicketResponseDto> createTicket(
            @Valid @RequestBody TicketRegistrationDto dto) {

        User loggedInUser = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return ResponseEntity.ok(ticketService.createTicket(dto, loggedInUser.getId()));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDto>> getAllTickets() {
        User loggedInUser = (User) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(ticketService.getAllTickets(loggedInUser));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<TicketResponseDto> assignTicket(@PathVariable Long id) {

        User loggedInAgent = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return ResponseEntity.ok(ticketService.assignTicketToAgent(id, loggedInAgent));
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<TicketResponseDto> resolveTicket(@PathVariable Long id) {
        User loggedInAgent = (User) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ticketService.resolveTicket(id, loggedInAgent));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<?> getTicketStats() {

        long total = ticketRepository.count();
        long open = ticketRepository.countByStatus("OPEN");
        long inProgress = ticketRepository.countByStatus("IN_PROGRESS");
        long resolved = ticketRepository.countByStatus("RESOLVED");
        long critical = ticketRepository.countByPriority("CRITICAL");

        TicketStatsDTO stats =
                new TicketStatsDTO(total, open, inProgress, resolved, critical);

        return ResponseEntity.ok(stats);
    }
}