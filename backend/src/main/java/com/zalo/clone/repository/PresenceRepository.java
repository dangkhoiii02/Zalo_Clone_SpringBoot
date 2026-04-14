package com.zalo.clone.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Presence Repository — equivalent to Go's redis.presenceRepository.
 * Manages online status and typing indicators using Redis keys with TTL.
 */
@Repository
@RequiredArgsConstructor
public class PresenceRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String PRESENCE_KEY_PREFIX = "user:presence:";
    private static final String TYPING_KEY_PREFIX = "user:typing:";
    private static final Duration PRESENCE_TTL = Duration.ofSeconds(60);
    private static final Duration TYPING_TTL = Duration.ofSeconds(5);

    public void setOnline(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, "online", PRESENCE_TTL);
    }

    public void setOffline(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public boolean isOnline(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Map<String, Boolean> getOnlineUsers(List<String> userIds) {
        Map<String, Boolean> onlineMap = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return onlineMap;
        }

        // Use pipeline for batch check
        for (String uid : userIds) {
            String key = PRESENCE_KEY_PREFIX + uid;
            onlineMap.put(uid, Boolean.TRUE.equals(redisTemplate.hasKey(key)));
        }
        return onlineMap;
    }

    public void setTyping(String userId, String conversationId) {
        String key = TYPING_KEY_PREFIX + conversationId + ":" + userId;
        redisTemplate.opsForValue().set(key, "typing", TYPING_TTL);
    }
}
