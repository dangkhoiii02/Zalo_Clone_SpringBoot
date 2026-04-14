# Zalo Clone Project

A comprehensive real-time messaging and video calling application built with Clean Architecture, inspired by WhatsApp and Zalo.

![Architecture](https://img.shields.io/badge/Architecture-Clean-brightgreen?style=flat-square) ![Backend](https://img.shields.io/badge/Backend-Spring_Boot_3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white) ![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white) ![Frontend](https://img.shields.io/badge/Frontend-Vite_+_Vanilla_JS-646CFF?style=flat-square&logo=vite&logoColor=white)

## System Architecture

The overarching system is divided into two primary parts:
1. **Backend Server** — Java 21 + Spring Boot 3.5 (Clean Architecture)
2. **Frontend** — Vite + Vanilla JavaScript

The infrastructure consists of several interconnected pieces deployed via `docker-compose`:
- **PostgreSQL** — Persistent tabular data (Users, Friendships)
- **MongoDB** — Unstructured/high-volume data (Messages, Conversations)
- **Redis** — Caching and real-time presence (Online status with TTLs)
- **LiveKit Server (WebRTC SFU)** — Real-time video and audio calling

## Features

- **Authentication & Security:** JWT-based authentication (JJWT), BCrypt password hashing, Spring Security
- **Real-Time Data Flow:** Native WebSocket with TextWebSocketHandler
- **RESTful Endpoints:** SpringDoc OpenAPI / Swagger UI for interactive documentation
- **Message System:** Typing indicators, Read receipts, History pagination
- **Peer-to-Peer Calling:** LiveKit token-based video/audio calls
- **Premium UX:** Dark mode, emoji picker, message reactions, sound effects, micro-animations

## Getting Started

### 1. Prerequisites

- **Java 21** — Included in `backend/jdk-21.0.10+7/` (auto-downloaded)
- **Docker** & **Docker Compose**
- **Node.js 18+** — For frontend (Vite)

### 2. Start Infrastructure

```bash
docker-compose up -d
```

### 3. Run Backend (Spring Boot)

```bash
cd backend
export JAVA_HOME="$(pwd)/jdk-21.0.10+7/Contents/Home"
./mvnw spring-boot:run
```

The app will auto-create database tables, connect to MongoDB/Redis, and start on `http://localhost:8080`.

### 4. Run Frontend

```bash
cd ui
npm install   # first time only
npm run dev
```

Open **http://localhost:5173** in your browser.

### 5. API Documentation (Swagger)

Once the server is running:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

## Project Structure

```text
backend/
├── src/main/java/com/zalo/clone/
│   ├── ZaloCloneApplication.java       # Main entry point
│   ├── config/                          # CORS, Security configs
│   ├── controller/                      # REST Controllers (6 files)
│   ├── domain/                          # JPA Entities + MongoDB Documents (4 files)
│   ├── dto/                             # Request/Response DTOs (10 files)
│   ├── exception/                       # Global exception handler
│   ├── repository/                      # Spring Data repositories (5 files)
│   ├── security/                        # JWT provider + filter
│   ├── service/                         # Business logic (5 files)
│   └── websocket/                       # WebSocket handler + hub (3 files)
├── src/main/resources/
│   └── application.yml                  # All configuration
├── pom.xml                              # Maven dependencies
└── mvnw                                 # Maven Wrapper (no global install needed)

ui/
├── src/
│   ├── api.js                           # REST API client
│   ├── websocket.js                     # WebSocket client
│   ├── auth.js                          # Login/Register UI
│   ├── chat.js                          # Chat interface
│   └── ...
├── index.html
└── package.json
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/auth/register` | Register |
| POST | `/api/v1/auth/login` | Login |
| GET | `/api/v1/auth/me` | Current user |
| PUT | `/api/v1/users/profile` | Update profile |
| GET | `/api/v1/users/search?q=` | Search users |
| POST | `/api/v1/friends/request` | Send friend request |
| PUT | `/api/v1/friends/accept/{id}` | Accept request |
| DELETE | `/api/v1/friends/{id}` | Remove friend |
| GET | `/api/v1/friends` | List friends |
| GET | `/api/v1/friends/requests` | Pending requests |
| POST | `/api/v1/conversations` | Create conversation |
| GET | `/api/v1/conversations` | List conversations |
| GET | `/api/v1/conversations/{id}/messages` | Get messages |
| POST | `/api/v1/calls/start` | Start video call |
| POST | `/api/v1/calls/join/{roomName}` | Join video call |
| GET | `/api/v1/health` | Health check |
| WS | `/api/v1/ws?token=` | WebSocket |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot 3.5, Spring Security, Spring Data JPA/MongoDB/Redis |
| Auth | JJWT 0.12.6, BCrypt |
| Database | PostgreSQL 17, MongoDB 8.0, Redis 7 |
| WebSocket | Spring WebSocket (TextWebSocketHandler) |
| Video Call | LiveKit Server + LiveKit Java SDK |
| API Docs | SpringDoc OpenAPI 2.8.6 |
| Frontend | Vite, Vanilla JavaScript, CSS Custom Properties |
