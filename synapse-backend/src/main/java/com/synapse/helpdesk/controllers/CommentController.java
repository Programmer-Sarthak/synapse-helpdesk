package com.synapse.helpdesk.controllers;

import com.synapse.helpdesk.dtos.CommentRequestDto;
import com.synapse.helpdesk.dtos.CommentResponseDto;
import com.synapse.helpdesk.models.User;
import com.synapse.helpdesk.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CommentRequestDto dto) {
        User loggedInUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(commentService.addComment(ticketId, dto, loggedInUser));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.getCommentsForTicket(ticketId));
    }
}