package com.zalo.clone.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friendships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("user_id")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @JsonProperty("friend_id")
    @Column(name = "friend_id", nullable = false)
    private UUID friendId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @CreationTimestamp
    @JsonProperty("created_at")
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum FriendshipStatus {
        PENDING, ACCEPTED, BLOCKED
    }
}
