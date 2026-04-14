package com.zalo.clone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class FriendRequestDto {
    @JsonProperty("friend_id")
    private UUID friendId;
}
