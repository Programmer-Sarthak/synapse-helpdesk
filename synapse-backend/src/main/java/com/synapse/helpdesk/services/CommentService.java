package com.synapse.helpdesk.services;

import com.synapse.helpdesk.dtos.CommentRequestDto;
import com.synapse.helpdesk.dtos.CommentResponseDto;
import com.synapse.helpdesk.dtos.UserSummaryDto;
import com.synapse.helpdesk.models.Comment;
import com.synapse.helpdesk.models.Ticket;
import com.synapse.helpdesk.models.User;
import com.synapse.helpdesk.repositories.CommentRepository;
import com.synapse.helpdesk.repositories.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public CommentResponseDto addComment(Long ticketId, CommentRequestDto dto, User author) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        CommentResponseDto responseDto = mapToDto(savedComment);

        messagingTemplate.convertAndSend("/topic/tickets/" + ticketId, "NEW_COMMENT");

        return responseDto;
    }

    public List<CommentResponseDto> getCommentsForTicket(Long ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CommentResponseDto mapToDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());

        UserSummaryDto authorDto = new UserSummaryDto();
        authorDto.setId(comment.getAuthor().getId());
        authorDto.setName(comment.getAuthor().getName());
        dto.setAuthor(authorDto);

        return dto;
    }
}