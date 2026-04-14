package com.zalo.clone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zalo.clone.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private User user;
}
