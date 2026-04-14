package com.zalo.clone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartCallRequest {
    @NotBlank
    @JsonProperty("callee_id")
    private String calleeId;
}
