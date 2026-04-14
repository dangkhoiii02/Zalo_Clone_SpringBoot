package com.zalo.clone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String bio;
}
