package com.zalo.clone.service;

import com.zalo.clone.domain.Conversation;
import com.zalo.clone.domain.Conversation.ConversationType;
import com.zalo.clone.domain.Conversation.LastMessage;
import com.zalo.clone.domain.Message;
import com.zalo.clone.domain.Message.MessageType;
import com.zalo.clone.dto.CreateConversationRequest;
import com.zalo.clone.dto.SendMessageRequest;
import com.zalo.clone.repository.ConversationRepository;
import com.zalo.clone.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * MessageService — equivalent to Go's usecase.MessageUsecase.
 * Handles message sending, retrieval, conversation creation, and read receipts.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MongoTemplate mongoTemplate;

    public Message sendMessage(String senderId, SendMessageRequest req) {
        // Verify conversation exists and sender is participant
        Conversation conv = conversationRepository.findById(req.getConversationId())
                .orElseThrow(() -> new RuntimeException("conversation not found"));

        if (!conv.getParticipants().contains(senderId)) {
            throw new RuntimeException("user is not a participant in this conversation");
        }

        MessageType msgType = MessageType.TEXT;
        if (req.getType() != null && !req.getType().isEmpty()) {
            try {
                msgType = MessageType.valueOf(req.getType().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(req.getConversationId())
                .senderId(senderId)
                .content(req.getContent())
                .type(msgType)
                .mediaUrl(req.getMediaUrl())
                .readBy(new ArrayList<>(List.of(senderId)))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        messageRepository.save(message);

        // Update last message in conversation
        updateLastMessage(req.getConversationId(), req.getContent(), senderId);

        return message;
    }

    public List<Message> getMessages(String conversationId, int limit, int offset) {
        if (limit <= 0 || limit > 100) {
            limit = 50;
        }
        return messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId, PageRequest.of(offset / Math.max(limit, 1), limit));
    }

    public Conversation createConversation(String creatorId, CreateConversationRequest req) {
        List<String> participants = new ArrayList<>(req.getParticipants());

        // Ensure creator is in participants
        if (!participants.contains(creatorId)) {
            participants.add(creatorId);
        }

        // For direct chats, check if conversation already exists
        if ("direct".equalsIgnoreCase(req.getType()) || "DIRECT".equals(req.getType())) {
            if (participants.size() != 2) {
                throw new RuntimeException("direct conversations must have exactly 2 participants");
            }

            List<String> sorted = new ArrayList<>(participants);
            Collections.sort(sorted);

            Optional<Conversation> existing = conversationRepository.findDirectConversation(sorted, sorted.size());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        Conversation conv = Conversation.builder()
                .id(UUID.randomUUID().toString())
                .type("direct".equalsIgnoreCase(req.getType()) ? ConversationType.DIRECT : ConversationType.GROUP)
                .participants(participants)
                .name(req.getName())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return conversationRepository.save(conv);
    }

    public List<Conversation> getConversations(String userId) {
        return conversationRepository.findByParticipantsContainingOrderByUpdatedAtDesc(userId);
    }

    public void markAsRead(String messageId, String userId) {
        Query query = new Query(Criteria.where("_id").is(messageId));
        Update update = new Update()
                .addToSet("read_by", userId)
                .set("updated_at", Instant.now());
        mongoTemplate.updateFirst(query, update, Message.class);
    }

    private void updateLastMessage(String conversationId, String content, String senderId) {
        Query query = new Query(Criteria.where("_id").is(conversationId));
        Update update = new Update()
                .set("last_message", LastMessage.builder()
                        .content(content)
                        .senderId(senderId)
                        .createdAt(Instant.now())
                        .build())
                .set("updated_at", Instant.now());
        mongoTemplate.updateFirst(query, update, Conversation.class);
    }
}
