package com.zalo.clone.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "messages")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Message {

    @Id
    private String id;

    @Field("conversation_id")
    @JsonProperty("conversation_id")
    private String conversationId;

    @Field("sender_id")
    @JsonProperty("sender_id")
    private String senderId;

    private String content;

    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Field("media_url")
    @JsonProperty("media_url")
    private String mediaUrl;

    @Field("read_by")
    @JsonProperty("read_by")
    @Builder.Default
    private List<String> readBy = new ArrayList<>();

    @Field("created_at")
    @JsonProperty("created_at")
    private Instant createdAt;

    @Field("updated_at")
    @JsonProperty("updated_at")
    private Instant updatedAt;

    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM
    }
}
