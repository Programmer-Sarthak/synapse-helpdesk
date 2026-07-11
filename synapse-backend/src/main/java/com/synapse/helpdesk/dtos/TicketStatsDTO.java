package com.synapse.helpdesk.dtos;

public class TicketStatsDTO {
    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long criticalTickets;

    public TicketStatsDTO(long totalTickets, long openTickets, long inProgressTickets, long resolvedTickets, long criticalTickets) {
        this.totalTickets = totalTickets;
        this.openTickets = openTickets;
        this.inProgressTickets = inProgressTickets;
        this.resolvedTickets = resolvedTickets;
        this.criticalTickets = criticalTickets;
    }

    public long getTotalTickets() { return totalTickets; }
    public long getOpenTickets() { return openTickets; }
    public long getInProgressTickets() { return inProgressTickets; }
    public long getResolvedTickets() { return resolvedTickets; }
    public long getCriticalTickets() { return criticalTickets; }
}