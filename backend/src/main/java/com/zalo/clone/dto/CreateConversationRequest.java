package com.zalo.clone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateConversationRequest {
    @NotBlank
    private String type;

    @NotEmpty
    private List<String> participants;

    private String name;
}
