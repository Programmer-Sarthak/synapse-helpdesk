package com.synapse.helpdesk.services;

import com.synapse.helpdesk.dtos.TicketRegistrationDto;
import com.synapse.helpdesk.dtos.TicketResponseDto;
import com.synapse.helpdesk.dtos.UserSummaryDto;
import com.synapse.helpdesk.models.Ticket;
import com.synapse.helpdesk.models.User;
import com.synapse.helpdesk.repositories.TicketRepository;
import com.synapse.helpdesk.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AiService aiService;

    public TicketResponseDto createTicket(TicketRegistrationDto dto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setStatus("OPEN");

        String priority = aiService.determinePriority(
                dto.getTitle(),
                dto.getDescription()
        );
        ticket.setPriority(priority);

        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setCreatedBy(user);

        Ticket savedTicket = ticketRepository.save(ticket);

        return mapToResponseDto(savedTicket);
    }

    public List<TicketResponseDto> getAllTickets(User user) {
        List<Ticket> tickets;

        if (user.getRole().equals("ROLE_AGENT")) {
            tickets = ticketRepository.findAll();
        } else {
            tickets = ticketRepository.findByCreatedBy(user);
        }

        return tickets.stream()
                .map(this::mapToResponseDto)
                .collect(java.util.stream.Collectors.toList());
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

        return mapToResponseDto(ticketRepository.save(ticket));
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

        return mapToResponseDto(ticketRepository.save(ticket));
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
}