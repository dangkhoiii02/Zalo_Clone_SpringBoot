package com.zalo.clone.service;

import com.zalo.clone.dto.CallResponse;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * CallService — equivalent to Go's usecase.CallUsecase.
 * Generates LiveKit room tokens for video calls.
 */
@Service
public class CallService {

    private final String apiKey;
    private final String apiSecret;

    public CallService(
            @Value("${app.livekit.api-key}") String apiKey,
            @Value("${app.livekit.api-secret}") String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public CallResponse startCall(String callerId, String callerName, String calleeId) {
        String roomName = "call-" + UUID.randomUUID();

        String token = generateLiveKitToken(roomName, callerId, callerName);

        return new CallResponse(roomName, token);
    }

    public CallResponse joinCall(String roomName, String userId, String userName) {
        String token = generateLiveKitToken(roomName, userId, userName);
        return new CallResponse(roomName, token);
    }

    private String generateLiveKitToken(String roomName, String participantId, String participantName) {
        AccessToken token = new AccessToken(apiKey, apiSecret);
        token.setName(participantName);
        token.setIdentity(participantId);

        // Grant permission to join the specified room
        token.addGrants(new RoomJoin(true), new RoomName(roomName));

        return token.toJwt();
    }
}
