package com.zalo.clone.controller;

import com.zalo.clone.domain.Friendship;
import com.zalo.clone.dto.FriendInfo;
import com.zalo.clone.dto.FriendRequestDto;
import com.zalo.clone.service.FriendshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * FriendshipController — equivalent to Go's http.FriendshipHandler.
 * Endpoints: /api/v1/friends/**
 */
@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Tag(name = "Friendship", description = "Friend management endpoints")
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/request")
    @Operation(summary = "Send friend request")
    public ResponseEntity<?> sendRequest(Authentication authentication,
                                          @RequestBody FriendRequestDto request) {
        UUID userId = (UUID) authentication.getPrincipal();
        try {
            Friendship friendship = friendshipService.sendRequest(userId, request.getFriendId());
            return ResponseEntity.status(HttpStatus.CREATED).body(friendship);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/accept/{id}")
    @Operation(summary = "Accept friend request")
    public ResponseEntity<?> acceptRequest(Authentication authentication,
                                            @PathVariable("id") UUID friendshipId) {
        UUID userId = (UUID) authentication.getPrincipal();
        try {
            friendshipService.acceptRequest(userId, friendshipId);
            return ResponseEntity.ok(Map.of("message", "friend request accepted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove friend")
    public ResponseEntity<?> removeFriend(Authentication authentication,
                                           @PathVariable("id") UUID friendshipId) {
        UUID userId = (UUID) authentication.getPrincipal();
        try {
            friendshipService.removeFriend(userId, friendshipId);
            return ResponseEntity.ok(Map.of("message", "friend removed"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get friends list")
    public ResponseEntity<?> getFriends(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        try {
            List<FriendInfo> friends = friendshipService.getFriends(userId);
            return ResponseEntity.ok(Map.of("friends", friends));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/requests")
    @Operation(summary = "Get pending friend requests")
    public ResponseEntity<?> getPendingRequests(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        try {
            List<FriendInfo> requests = friendshipService.getPendingRequests(userId);
            return ResponseEntity.ok(Map.of("requests", requests));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
