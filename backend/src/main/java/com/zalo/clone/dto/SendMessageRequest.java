package com.zalo.clone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SendMessageRequest {
    @NotBlank
    @JsonProperty("conversation_id")
    private String conversationId;

    @NotBlank
    private String content;

    private String type;

    @JsonProperty("media_url")
    private String mediaUrl;
}
