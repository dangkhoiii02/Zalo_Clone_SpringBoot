package com.zalo.clone;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ZaloCloneApplication {

    public static void main(String[] args) {
        log.info("🚀 Starting Zalo Clone Server (Spring Boot)...");
        SpringApplication.run(ZaloCloneApplication.class, args);
        log.info("🌐 Server running on http://localhost:8080");
        log.info("📡 WebSocket endpoint: ws://localhost:8080/api/v1/ws");
        log.info("❤️  Health check: http://localhost:8080/api/v1/health");
        log.info("📖 Swagger UI: http://localhost:8080/swagger-ui.html");
    }
}
