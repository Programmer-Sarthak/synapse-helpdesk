package com.synapse.helpdesk.controllers;

import com.synapse.helpdesk.models.Comment;
import com.synapse.helpdesk.models.Ticket;
import com.synapse.helpdesk.repositories.CommentRepository;
import com.synapse.helpdesk.repositories.TicketRepository;
import com.synapse.helpdesk.services.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    @GetMapping("/tickets/{ticketId}/smart-reply")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<Map<String, String>> getSmartReply(@PathVariable Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        List<Comment> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        List<String> history = comments.stream()
                .map(c -> c.getAuthor().getName() + ": " + c.getText())
                .collect(Collectors.toList());

        String reply = aiService.generateSmartReply(ticket.getTitle(), ticket.getDescription(), history);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}