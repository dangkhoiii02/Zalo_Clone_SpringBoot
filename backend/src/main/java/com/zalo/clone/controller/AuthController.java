package com.zalo.clone.controller;

import com.zalo.clone.domain.User;
import com.zalo.clone.dto.AuthResponse;
import com.zalo.clone.dto.LoginRequest;
import com.zalo.clone.dto.RegisterRequest;
import com.zalo.clone.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * AuthController — equivalent to Go's http.AuthHandler.
 * Endpoints: /api/v1/auth/register, /api/v1/auth/login, /api/v1/auth/me
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists") || e.getMessage().contains("already taken")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(java.util.Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("invalid credentials")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(java.util.Map.of("error", "invalid email or password"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<?> me(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        try {
            User user = authService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", "user not found"));
        }
    }
}
