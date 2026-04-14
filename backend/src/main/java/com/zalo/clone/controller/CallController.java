package com.zalo.clone.controller;

import com.zalo.clone.dto.CallResponse;
import com.zalo.clone.dto.StartCallRequest;
import com.zalo.clone.service.CallService;
import com.zalo.clone.websocket.WebSocketHub;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * CallController — equivalent to Go's http.CallHandler.
 * Endpoints: /api/v1/calls/start, /api/v1/calls/join/{roomName}
 */
@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
@Tag(name = "Calls", description = "Video call endpoints")
public class CallController {

    private final CallService callService;
    private final WebSocketHub webSocketHub;

    @PostMapping("/start")
    @Operation(summary = "Start a call")
    public ResponseEntity<?> startCall(Authentication authentication,
                                        @Valid @RequestBody StartCallRequest request) {
        UUID userId = (UUID) authentication.getPrincipal();
        String username = (String) authentication.getDetails();

        try {
            CallResponse response = callService.startCall(
                    userId.toString(), username, request.getCalleeId());

            // Notify callee via WebSocket about the incoming call
            webSocketHub.sendToUser(request.getCalleeId(), Map.of(
                    "type", "incoming_call",
                    "data", Map.of(
                            "room_name", response.getRoomName(),
                            "caller_id", userId.toString(),
                            "caller_name", username
                    )
            ));

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/join/{roomName}")
    @Operation(summary = "Join a call")
    public ResponseEntity<?> joinCall(Authentication authentication,
                                       @PathVariable("roomName") String roomName) {
        UUID userId = (UUID) authentication.getPrincipal();
        String username = (String) authentication.getDetails();

        try {
            CallResponse response = callService.joinCall(roomName, userId.toString(), username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
