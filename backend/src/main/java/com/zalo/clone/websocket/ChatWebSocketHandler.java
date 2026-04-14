package com.zalo.clone.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zalo.clone.domain.Conversation;
import com.zalo.clone.domain.Message;
import com.zalo.clone.domain.Message.MessageType;
import com.zalo.clone.repository.ConversationRepository;
import com.zalo.clone.repository.PresenceRepository;
import com.zalo.clone.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.*;

/**
 * ChatWebSocketHandler — equivalent to Go's ws.Hub + ws.Client combined.
 * Handles all WebSocket message types: message, typing, read, heartbeat.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketHub hub;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final PresenceRepository presenceRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");

        hub.register(userId, username, session);
        presenceRepository.setOnline(userId);

        log.info("👤 User {} ({}) connected. Total: {}", username, userId, hub.getOnlineCount());

        // Broadcast presence
        broadcastPresence(userId, "online");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = (String) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");

        hub.unregister(userId, session);
        presenceRepository.setOffline(userId);

        log.info("👤 User {} ({}) disconnected. Total: {}", username, userId, hub.getOnlineCount());

        broadcastPresence(userId, "offline");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        String userId = (String) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");

        try {
            JsonNode json = objectMapper.readTree(textMessage.getPayload());
            String type = json.has("type") ? json.get("type").asText() : "";
            JsonNode data = json.get("data");

            switch (type) {
                case "message" -> handleChatMessage(userId, username, data);
                case "typing" -> handleTyping(userId, username, data);
                case "read" -> handleRead(userId, data);
                case "heartbeat" -> presenceRepository.setOnline(userId);
                default -> log.warn("⚠️ Unknown message type: {} from user {}", type, userId);
            }
        } catch (Exception e) {
            log.error("❌ Failed to handle message from user {}: {}", userId, e.getMessage());
        }
    }

    private void handleChatMessage(String userId, String username, JsonNode data) {
        String conversationId = data.get("conversation_id").asText();
        String content = data.get("content").asText();
        String msgType = data.has("type") ? data.get("type").asText() : "text";
        String mediaUrl = data.has("media_url") ? data.get("media_url").asText() : "";

        // Get conversation to find recipients
        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) {
            log.error("❌ Conversation not found: {}", conversationId);
            return;
        }
        Conversation conv = convOpt.get();

        // Save message to MongoDB
        MessageType type = MessageType.TEXT;
        try { type = MessageType.valueOf(msgType.toUpperCase()); } catch (Exception ignored) {}

        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(conversationId)
                .senderId(userId)
                .content(content)
                .type(type)
                .mediaUrl(mediaUrl)
                .readBy(new ArrayList<>(List.of(userId)))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        messageService.markAsRead(message.getId(), userId); // reuse for save
        // Actually save through the lower-level approach
        try {
            // We need direct repo access, but MessageService.sendMessage does validation we don't need
            // So we build the message manually and save
            com.zalo.clone.dto.SendMessageRequest req = new com.zalo.clone.dto.SendMessageRequest();
            req.setConversationId(conversationId);
            req.setContent(content);
            req.setType(msgType);
            req.setMediaUrl(mediaUrl);
            message = messageService.sendMessage(userId, req);
        } catch (Exception e) {
            log.error("❌ Failed to save message: {}", e.getMessage());
            return;
        }

        // Broadcast to all participants
        Map<String, Object> outgoing = Map.of(
                "type", "message",
                "data", Map.of(
                        "id", message.getId(),
                        "conversation_id", message.getConversationId(),
                        "sender_id", message.getSenderId(),
                        "sender_name", username,
                        "content", message.getContent(),
                        "type", message.getType().name().toLowerCase(),
                        "media_url", message.getMediaUrl() != null ? message.getMediaUrl() : "",
                        "created_at", message.getCreatedAt().toString()
                )
        );

        for (String participantId : conv.getParticipants()) {
            hub.sendToUser(participantId, outgoing);
        }
    }

    private void handleTyping(String userId, String username, JsonNode data) {
        String conversationId = data.get("conversation_id").asText();
        presenceRepository.setTyping(userId, conversationId);

        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) return;

        Map<String, Object> outgoing = Map.of(
                "type", "typing",
                "data", Map.of(
                        "conversation_id", conversationId,
                        "user_id", userId,
                        "username", username
                )
        );

        for (String participantId : convOpt.get().getParticipants()) {
            if (!participantId.equals(userId)) {
                hub.sendToUser(participantId, outgoing);
            }
        }
    }

    private void handleRead(String userId, JsonNode data) {
        String conversationId = data.get("conversation_id").asText();
        String messageId = data.get("message_id").asText();

        messageService.markAsRead(messageId, userId);

        Optional<Conversation> convOpt = conversationRepository.findById(conversationId);
        if (convOpt.isEmpty()) return;

        Map<String, Object> outgoing = Map.of(
                "type", "read_receipt",
                "data", Map.of(
                        "conversation_id", conversationId,
                        "message_id", messageId,
                        "read_by", userId
                )
        );

        for (String participantId : convOpt.get().getParticipants()) {
            if (!participantId.equals(userId)) {
                hub.sendToUser(participantId, outgoing);
            }
        }
    }

    private void broadcastPresence(String userId, String status) {
        Map<String, Object> outgoing = Map.of(
                "type", "presence",
                "data", Map.of(
                        "user_id", userId,
                        "status", status
                )
        );

        hub.broadcastExcept(userId, outgoing);
    }
}
