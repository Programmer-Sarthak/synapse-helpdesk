package com.synapse.helpdesk.repositories;

import com.synapse.helpdesk.models.Ticket;
import com.synapse.helpdesk.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.priority = :priority")
    long countByPriority(@Param("priority") String priority);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findByCreatedByOrderByCreatedAtDesc(User user);
}