package com.synapse.helpdesk.dtos;

import lombok.Data;
import java.util.Map;

@Data
public class AnalyticsDto {
    private Map<String, Long> ticketsByStatus;
    private Map<String, Long> ticketsByPriority;
    private Map<String, Long> ticketsByDay; // Last 7 days
    private Map<String, Long> agentLeaderboard; // Agent Name -> Resolved Count
}