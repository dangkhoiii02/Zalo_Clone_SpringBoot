package com.zalo.clone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CallResponse {
    @JsonProperty("room_name")
    private String roomName;

    private String token;
}
