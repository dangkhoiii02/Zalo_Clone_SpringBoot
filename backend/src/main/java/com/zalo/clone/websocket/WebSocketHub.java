package com.zalo.clone.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocketHub — equivalent to Go's ws.Hub.
 * Manages all active WebSocket sessions (userId → session mapping).
 * Thread-safe using ConcurrentHashMap.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHub {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> usernames = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public void register(String userId, String username, WebSocketSession session) {
        // Close existing session for this user if any
        WebSocketSession existing = sessions.get(userId);
        if (existing != null && existing.isOpen()) {
            try {
                existing.close();
            } catch (IOException e) {
                log.warn("Failed to close existing session for user {}", userId);
            }
        }
        sessions.put(userId, session);
        usernames.put(userId, username);
    }

    public void unregister(String userId, WebSocketSession session) {
        // Only remove if it's the same session (prevents race conditions)
        sessions.remove(userId, session);
        usernames.remove(userId);
    }

    public void sendToUser(String userId, Object message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                synchronized (session) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (IOException e) {
                log.error("❌ Failed to send message to user {}: {}", userId, e.getMessage());
            }
        }
    }

    public void broadcastExcept(String excludeUserId, Object message) {
        sessions.forEach((userId, session) -> {
            if (!userId.equals(excludeUserId) && session.isOpen()) {
                try {
                    String json = objectMapper.writeValueAsString(message);
                    synchronized (session) {
                        session.sendMessage(new TextMessage(json));
                    }
                } catch (IOException e) {
                    log.error("❌ Failed to broadcast to user {}: {}", userId, e.getMessage());
                }
            }
        });
    }

    public int getOnlineCount() {
        return sessions.size();
    }

    public boolean isUserOnline(String userId) {
        WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }
}
