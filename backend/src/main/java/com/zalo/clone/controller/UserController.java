package com.zalo.clone.controller;

import com.zalo.clone.domain.User;
import com.zalo.clone.dto.UpdateProfileRequest;
import com.zalo.clone.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * UserController — equivalent to Go's http.UserHandler.
 * Endpoints: /api/v1/users/profile, /api/v1/users/search
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<?> updateProfile(Authentication authentication,
                                            @RequestBody UpdateProfileRequest request) {
        UUID userId = (UUID) authentication.getPrincipal();
        try {
            User user = userService.updateProfile(userId, request);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by username or email")
    public ResponseEntity<?> searchUsers(@RequestParam("q") String query) {
        if (query == null || query.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "search query required"));
        }
        try {
            List<User> users = userService.searchUsers(query);
            return ResponseEntity.ok(Map.of("users", users));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
