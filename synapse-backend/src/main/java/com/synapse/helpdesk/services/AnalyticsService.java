package com.synapse.helpdesk.services;

import com.synapse.helpdesk.dtos.AnalyticsDto;
import com.synapse.helpdesk.models.Ticket;
import com.synapse.helpdesk.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TicketRepository ticketRepository;

    public AnalyticsDto getAnalytics() {
        List<Ticket> allTickets = ticketRepository.findAll();
        AnalyticsDto dto = new AnalyticsDto();

        Map<String, Long> byStatus = allTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));
        dto.setTicketsByStatus(byStatus);

        Map<String, Long> byPriority = allTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getPriority, Collectors.counting()));
        dto.setTicketsByPriority(byPriority);

        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        Map<String, Long> byDay = allTickets.stream()
                .filter(t -> t.getCreatedAt().toLocalDate().isAfter(sevenDaysAgo))
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toLocalDate().format(formatter),
                        Collectors.counting()
                ));
        dto.setTicketsByDay(byDay);

        Map<String, Long> leaderboard = allTickets.stream()
                .filter(t -> "RESOLVED".equals(t.getStatus()) && t.getAssignedTo() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getAssignedTo().getName(),
                        Collectors.counting()
                ));
        dto.setAgentLeaderboard(leaderboard);

        return dto;
    }
}