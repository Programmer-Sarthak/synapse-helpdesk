package com.synapse.helpdesk.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name="ticket_audits")
public class TicketAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String details;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}