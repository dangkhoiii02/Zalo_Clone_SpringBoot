package com.zalo.clone.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @JsonProperty("avatar_url")
    @Column(name = "avatar_url", columnDefinition = "TEXT DEFAULT ''")
    @Builder.Default
    private String avatarUrl = "";

    @Column(columnDefinition = "TEXT DEFAULT ''")
    @Builder.Default
    private String bio = "";

    @CreationTimestamp
    @JsonProperty("created_at")
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @JsonProperty("updated_at")
    @Column(name = "updated_at")
    private Instant updatedAt;
}
