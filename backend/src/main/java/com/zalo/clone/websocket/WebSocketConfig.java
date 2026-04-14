package com.zalo.clone.websocket;

import com.zalo.clone.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket Configuration — registers the /api/v1/ws endpoint.
 * Equivalent to Go's router.GET("/api/v1/ws", ...) with JWT auth via query param.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final JwtTokenProvider tokenProvider;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/api/v1/ws")
                .addInterceptors(new JwtHandshakeInterceptor())
                .setAllowedOrigins("*");
    }

    /**
     * Intercepts WebSocket handshake to extract JWT from query param "token".
     * Equivalent to Go's ws.Handler.HandleWebSocket JWT parsing.
     */
    private class JwtHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request,
                                        ServerHttpResponse response,
                                        WebSocketHandler wsHandler,
                                        Map<String, Object> attributes) {
            URI uri = request.getURI();
            String query = uri.getQuery();

            String token = null;
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("token=")) {
                        token = param.substring(6);
                        break;
                    }
                }
            }

            if (token == null || !tokenProvider.validateToken(token)) {
                log.warn("❌ WebSocket handshake failed: invalid or missing token");
                return false;
            }

            String userId = tokenProvider.getUserIdFromToken(token);
            String username = tokenProvider.getUsernameFromToken(token);

            attributes.put("userId", userId);
            attributes.put("username", username);

            log.info("✅ WebSocket handshake: user={} ({})", username, userId);
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request,
                                    ServerHttpResponse response,
                                    WebSocketHandler wsHandler,
                                    Exception exception) {
            // No-op
        }
    }
}
