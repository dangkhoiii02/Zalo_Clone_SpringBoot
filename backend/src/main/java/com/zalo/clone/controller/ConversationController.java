package com.zalo.clone.controller;

import com.zalo.clone.domain.Conversation;
import com.zalo.clone.domain.Message;
import com.zalo.clone.dto.CreateConversationRequest;
import com.zalo.clone.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ConversationController — equivalent to Go's http.ConversationHandler.
 * Endpoints: /api/v1/conversations, /api/v1/conversations/{id}/messages
 */
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Conversation and message endpoints")
public class ConversationController {

    private final MessageService messageService;

    @PostMapping
    @Operation(summary = "Create a conversation")
    public ResponseEntity<?> createConversation(Authentication authentication,
                                                 @Valid @RequestBody CreateConversationRequest request) {
        UUID userId = (UUID) authentication.getPrincipal();
        try {
            Conversation conv = messageService.createConversation(userId.toString(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(conv);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get user conversations")
    public ResponseEntity<?> getConversations(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        try {
            List<Conversation> conversations = messageService.getConversations(userId.toString());
            return ResponseEntity.ok(Map.of("conversations", conversations));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Get conversation messages")
    public ResponseEntity<?> getMessages(@PathVariable("id") String conversationId,
                                          @RequestParam(defaultValue = "50") int limit,
                                          @RequestParam(defaultValue = "0") int offset) {
        try {
            List<Message> messages = messageService.getMessages(conversationId, limit, offset);
            return ResponseEntity.ok(Map.of("messages", messages));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
