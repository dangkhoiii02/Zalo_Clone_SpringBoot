package com.zalo.clone.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

/**
 * HealthController — equivalent to Go router's /api/v1/health endpoint.
 * Checks connectivity to PostgreSQL, MongoDB, and Redis.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Health check endpoint")
public class HealthController {

    private final DataSource dataSource;
    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<?> health() {
        String pgStatus = "up";
        try (Connection conn = dataSource.getConnection()) {
            conn.isValid(5);
        } catch (Exception e) {
            pgStatus = "down";
        }

        String mongoStatus = "up";
        try {
            mongoTemplate.getDb().runCommand(org.bson.Document.parse("{ping: 1}"));
        } catch (Exception e) {
            mongoStatus = "down";
        }

        String redisStatus = "up";
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
        } catch (Exception e) {
            redisStatus = "down";
        }

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "services", Map.of(
                        "postgres", pgStatus,
                        "mongodb", mongoStatus,
                        "redis", redisStatus
                ),
                "java_version", System.getProperty("java.version"),
                "framework", "Spring Boot 3.5.0"
        ));
    }
}
