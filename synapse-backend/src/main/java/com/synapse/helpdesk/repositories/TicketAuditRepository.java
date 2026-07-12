package com.synapse.helpdesk.repositories;

import com.synapse.helpdesk.models.TicketAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketAuditRepository extends JpaRepository<TicketAudit, Long> {
    List<TicketAudit> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}