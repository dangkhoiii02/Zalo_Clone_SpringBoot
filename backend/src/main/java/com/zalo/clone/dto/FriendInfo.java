package com.zalo.clone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zalo.clone.domain.Friendship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendInfo {
    @JsonProperty("friendship_id")
    private UUID friendshipId;

    @JsonProperty("user_id")
    private UUID userId;

    private String username;
    private String email;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String bio;

    private Friendship.FriendshipStatus status;

    @JsonProperty("is_online")
    private boolean isOnline;

    @JsonProperty("created_at")
    private Instant createdAt;
}
