package com.synapse.helpdesk.services;

import com.synapse.helpdesk.dtos.TicketRegistrationDto;
import com.synapse.helpdesk.dtos.TicketResponseDto;
import com.synapse.helpdesk.dtos.UserSummaryDto;
import com.synapse.helpdesk.models.Ticket;
import com.synapse.helpdesk.models.TicketAudit;
import com.synapse.helpdesk.models.User;
import com.synapse.helpdesk.repositories.TicketAuditRepository;
import com.synapse.helpdesk.repositories.TicketRepository;
import com.synapse.helpdesk.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AiService aiService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TicketAuditRepository auditRepository;

    public TicketResponseDto createTicket(TicketRegistrationDto dto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setStatus("OPEN");
        ticket.setPriority(aiService.determinePriority(dto.getTitle(), dto.getDescription()));
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setCreatedBy(user);

        Ticket savedTicket = ticketRepository.save(ticket);
        TicketResponseDto responseDto = mapToResponseDto(savedTicket);

        messagingTemplate.convertAndSend("/topic/tickets", "NEW_TICKET");

        emailService.sendEmail(
                user.getEmail(),
                "Synapse Helpdesk - Ticket Created [#" + savedTicket.getId() + "]",
                "Hello " + user.getName() + ",\n\nYour ticket titled '" +
                        savedTicket.getTitle() + "' has been successfully logged into our system.\n\nAssigned Priority: " +
                        savedTicket.getPriority() + "\n\nAn IT Support Agent will review your request shortly."
        );

        logAudit(savedTicket, "CREATED", "Ticket was opened", user.getName());

        return responseDto;
    }

    public List<TicketResponseDto> getAllTickets(User user) {
        List<Ticket> tickets;
        if (user.getRole().equals("ROLE_AGENT")) {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        } else {
            tickets = ticketRepository.findByCreatedByOrderByCreatedAtDesc(user);
        }
        return tickets.stream().map(this::mapToResponseDto).collect(java.util.stream.Collectors.toList());
    }

    public TicketResponseDto assignTicketToAgent(Long ticketId, User agent) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getAssignedTo() != null) {
            throw new RuntimeException("Ticket is already assigned to someone else!");
        }

        ticket.setAssignedTo(agent);
        ticket.setStatus("IN_PROGRESS");
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);

        messagingTemplate.convertAndSend("/topic/tickets", "TICKET_UPDATED");

        emailService.sendEmail(
                updatedTicket.getCreatedBy().getEmail(),
                "Synapse Helpdesk - Ticket Claimed [#" + updatedTicket.getId() + "]",
                "Hello " + updatedTicket.getCreatedBy().getName() + ",\n\nYour IT issue regarding '" +
                        updatedTicket.getTitle() + "' has been claimed by Agent " + agent.getName() +
                        ".\n\nThe status is now: IN_PROGRESS."
        );

        logAudit(ticket, "ASSIGNED", "Ticket was claimed by agent", agent.getName());

        return mapToResponseDto(updatedTicket);
    }

    public TicketResponseDto resolveTicket(Long ticketId, User agent) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (!"IN_PROGRESS".equals(ticket.getStatus())) {
            throw new RuntimeException("Only 'In Progress' tickets can be resolved.");
        }

        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(agent.getId())) {
            throw new RuntimeException("You can only resolve tickets assigned to you.");
        }

        ticket.setStatus("RESOLVED");
        ticket.setUpdatedAt(java.time.LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);

        messagingTemplate.convertAndSend("/topic/tickets", "TICKET_UPDATED");

        emailService.sendEmail(
                updatedTicket.getCreatedBy().getEmail(),
                "Synapse Helpdesk - Ticket Resolved [#" + updatedTicket.getId() + "]",
                "Hello " + updatedTicket.getCreatedBy().getName() +
                        ",\n\nGreat news! Your IT Support ticket concerning '" +
                        updatedTicket.getTitle() + "' has been marked as RESOLVED by Agent " +
                        agent.getName() +
                        ".\n\nPlease verify that your issue is fixed. If problems persist, you may open a new ticket."
        );

        logAudit(ticket, "RESOLVED", "Ticket was marked as resolved", agent.getName());

        return mapToResponseDto(updatedTicket);
    }

    private TicketResponseDto mapToResponseDto(Ticket ticket) {
        TicketResponseDto dto = new TicketResponseDto();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setPriority(ticket.getPriority());
        dto.setCreatedAt(ticket.getCreatedAt());

        UserSummaryDto creatorDto = new UserSummaryDto();
        creatorDto.setId(ticket.getCreatedBy().getId());
        creatorDto.setName(ticket.getCreatedBy().getName());
        dto.setCreatedBy(creatorDto);

        if (ticket.getAssignedTo() != null) {
            UserSummaryDto assigneeDto = new UserSummaryDto();
            assigneeDto.setId(ticket.getAssignedTo().getId());
            assigneeDto.setName(ticket.getAssignedTo().getName());
            dto.setAssignedTo(assigneeDto);
        }
        return dto;
    }

    private void logAudit(Ticket ticket, String action, String details, String username) {
        TicketAudit audit = new com.synapse.helpdesk.models.TicketAudit();
        audit.setTicket(ticket);
        audit.setAction(action);
        audit.setDetails(details);
        audit.setChangedBy(username);
        audit.setCreatedAt(java.time.LocalDateTime.now());
        auditRepository.save(audit);
    }
}