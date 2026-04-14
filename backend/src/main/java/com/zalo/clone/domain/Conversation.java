package com.zalo.clone.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document(collection = "conversations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Conversation {

    @Id
    private String id;

    private ConversationType type;

    private List<String> participants;

    private String name;

    @Field("last_message")
    @JsonProperty("last_message")
    private LastMessage lastMessage;

    @Field("created_at")
    @JsonProperty("created_at")
    private Instant createdAt;

    @Field("updated_at")
    @JsonProperty("updated_at")
    private Instant updatedAt;

    public enum ConversationType {
        DIRECT, GROUP
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class LastMessage {
        private String content;

        @Field("sender_id")
        @JsonProperty("sender_id")
        private String senderId;

        @Field("created_at")
        @JsonProperty("created_at")
        private Instant createdAt;
    }
}
