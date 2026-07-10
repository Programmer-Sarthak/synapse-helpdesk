package com.synapse.helpdesk.repositories;

import com.synapse.helpdesk.models.Ticket;
import com.synapse.helpdesk.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedById(Long userId);
    List<Ticket> findByAssignedToId(Long agentId);
    List<Ticket> findByCreatedBy(User user);
}