# 🚀 Tiến độ Zalo Clone Web — Spring Boot Edition (2026)

> **Chuyển đổi backend từ Go (Gin + gorilla/websocket) → Java 21 + Spring Boot 3.x**
> Frontend (Vite + Vanilla JS) giữ nguyên, chỉ thay đổi base URL API nếu cần.

---

## Phase 0: Khởi tạo dự án Spring Boot (Hoàn thành ✅)
- [x] Tạo project Spring Boot 3.5.0 bằng [Spring Initializr](https://start.spring.io) với các dependencies:
  - `Spring Web` (REST API)
  - `Spring WebSocket` (STOMP + SockJS)
  - `Spring Data JPA` (PostgreSQL)
  - `Spring Data MongoDB` (lưu tin nhắn)
  - `Spring Data Redis` (cache, presence, pub/sub)
  - `Spring Security` + `jjwt` (JWT Authentication)
  - `Lombok` (giảm boilerplate)
  - `SpringDoc OpenAPI` (Swagger UI thay cho swaggo)
  - `Spring Boot DevTools` (hot-reload khi dev)
- [x] Cấu trúc project theo **Clean Architecture / Layered**:
  ```
  backend/
  ├── src/main/java/com/zalo/clone/
  │   ├── ZaloCloneApplication.java          // Main class
  │   ├── config/                            // Cấu hình (CORS, Security, WebSocket, DB,...)
  │   │   ├── CorsConfig.java
  │   │   ├── SecurityConfig.java
  │   │   ├── WebSocketConfig.java
  │   │   ├── MongoConfig.java
  │   │   └── RedisConfig.java
  │   ├── domain/                            // Entity / Model
  │   │   ├── User.java                      // ← user.go
  │   │   ├── Message.java                   // ← message.go
  │   │   ├── Conversation.java              // ← room.go
  │   │   └── Friendship.java               // ← friendship.go
  │   ├── repository/                        // Spring Data Repository interfaces
  │   │   ├── UserRepository.java            // JPA (PostgreSQL)
  │   │   ├── FriendshipRepository.java      // JPA (PostgreSQL)
  │   │   ├── MessageRepository.java         // MongoRepository
  │   │   └── ConversationRepository.java    // MongoRepository
  │   ├── service/                           // Business Logic (≈ usecase/)
  │   │   ├── AuthService.java               // ← auth_usecase.go
  │   │   ├── MessageService.java            // ← message_usecase.go
  │   │   ├── FriendshipService.java         // ← friendship_usecase.go
  │   │   ├── UserService.java               // ← user_usecase.go
  │   │   └── CallService.java               // ← call_usecase.go
  │   ├── controller/                        // REST Controllers (≈ delivery/http/)
  │   │   ├── AuthController.java            // ← auth_handler.go
  │   │   ├── ConversationController.java    // ← conversation_handler.go
  │   │   ├── FriendshipController.java      // ← friendship_handler.go
  │   │   ├── UserController.java            // ← user_handler.go
  │   │   └── CallController.java            // ← call_handler.go
  │   ├── websocket/                         // WebSocket (≈ delivery/ws/)
  │   │   ├── WebSocketHandler.java          // ← handler.go
  │   │   ├── WebSocketHub.java              // ← hub.go (quản lý sessions)
  │   │   └── WebSocketClient.java           // ← client.go
  │   ├── security/                          // JWT Filter, Utils
  │   │   ├── JwtTokenProvider.java
  │   │   └── JwtAuthenticationFilter.java
  │   ├── dto/                               // Request/Response DTOs
  │   │   ├── LoginRequest.java
  │   │   ├── RegisterRequest.java
  │   │   ├── MessageRequest.java
  │   │   └── ApiResponse.java
  │   └── exception/                         // Global Exception Handling
  │       ├── GlobalExceptionHandler.java
  │       └── ResourceNotFoundException.java
  ├── src/main/resources/
  │   ├── application.yml                    // Cấu hình (thay .env)
  │   └── application-dev.yml                // Profile dev
  └── pom.xml                                // Maven dependencies
  ```
- [x] Cấu hình `application.yml` kết nối PostgreSQL, MongoDB, Redis, LiveKit.
- [x] Cấu hình env variables thông qua `application.yml` Spring Profiles.
- [x] `docker-compose.yml` giữ nguyên (Postgres, Mongo, Redis, LiveKit).

---

## Phase 1: Backend Core — Authentication & User (Hoàn thành ✅)
> **Tương đương Go:** `auth_handler.go`, `auth_usecase.go`, `user_handler.go`, `user_usecase.go`, `middleware/`

- [x] Tạo entity `User.java` với JPA annotations (`@Entity`, `@Table`, `@Id`, `@GeneratedValue`).
- [x] Tạo `UserRepository.java` extend `JpaRepository<User, UUID>`.
- [x] Viết `JwtTokenProvider.java`: generate & validate JWT token (dùng thư viện `io.jsonwebtoken:jjwt`).
- [x] Viết `JwtAuthenticationFilter.java` extend `OncePerRequestFilter` — đọc token từ header `Authorization: Bearer <token>`.
- [x] Cấu hình `SecurityConfig.java`:
  - Cho phép truy cập không cần auth: `/api/v1/auth/**`, `/api/v1/ws/**`, `/swagger-ui/**`.
  - Tất cả endpoint còn lại yêu cầu JWT.
- [x] Viết `AuthService.java`: đăng ký (hash password bằng `BCryptPasswordEncoder`), đăng nhập (trả JWT).
- [x] Viết `AuthController.java`:
  - `POST /api/v1/auth/register` — Đăng ký.
  - `POST /api/v1/auth/login` — Đăng nhập, trả JWT.
- [x] Viết `UserService.java` & `UserController.java`:
  - `GET /api/v1/auth/me` — Lấy thông tin user hiện tại.
  - `GET /api/v1/users/search?q=` — Tìm kiếm user.
  - `PUT /api/v1/users/profile` — Cập nhật profile.
- [x] Cấu hình CORS (`CorsConfig.java`) cho phép `http://localhost:5173`.
- [x] Tích hợp Swagger (`SpringDoc OpenAPI`) — truy cập tại `/swagger-ui.html`.

---

## Phase 2: Backend Core — Messaging & Conversations (Hoàn thành ✅)
> **Tương đương Go:** `message.go`, `room.go`, `message_usecase.go`, `conversation_handler.go`, `mongo/message_repo.go`

- [x] Tạo entity `Message.java` với `@Document` (Spring Data MongoDB).
- [x] Tạo entity `Conversation.java` (MongoDB).
- [x] Tạo `MessageRepository.java` extend `MongoRepository<Message, String>`.
- [x] Tạo `ConversationRepository.java`.
- [x] Viết `MessageService.java`:
  - Gửi tin nhắn (lưu MongoDB, broadcast qua WebSocket).
  - Lấy lịch sử tin nhắn theo conversation (phân trang `Pageable`).
  - Đánh dấu đã đọc.
- [x] Viết `ConversationController.java`:
  - `GET /api/v1/conversations` — Danh sách hội thoại của user.
  - `GET /api/v1/conversations/{id}/messages` — Lấy tin nhắn theo hội thoại.
  - `POST /api/v1/conversations` — Tạo hội thoại mới / nhóm chat.

---

## Phase 3: Backend Core — WebSocket Realtime (Hoàn thành ✅)
> **Tương đương Go:** `hub.go`, `client.go`, `handler.go` (gorilla/websocket)

- [x] Cấu hình `WebSocketConfig.java`:
  - Đăng ký endpoint `/api/v1/ws` với `TextWebSocketHandler`.
  - JWT authentication qua query param `token` trong HandshakeInterceptor.
- [x] Viết `WebSocketHub.java` — quản lý danh sách connected sessions (ConcurrentHashMap).
  - `register(userId, username, session)` / `unregister(userId, session)`.
  - `broadcastExcept(userId, message)`.
  - `sendToUser(userId, message)`.
- [x] Viết `ChatWebSocketHandler.java` (extend `TextWebSocketHandler`):
  - `afterConnectionEstablished()` — đăng ký vào Hub, set online Redis.
  - `handleTextMessage()` — parse JSON, xử lý message, typing, read, heartbeat.
  - `afterConnectionClosed()` — cleanup session, broadcast offline.
- [ ] Tích hợp **Redis Pub/Sub** để hỗ trợ scale nhiều instance (tùy chọn).

---

## Phase 4: Backend Core — Friends & Social (Hoàn thành ✅)
> **Tương đương Go:** `friendship.go`, `friendship_usecase.go`, `friendship_handler.go`

- [x] Tạo entity `Friendship.java` (`@Entity`, enum `FriendshipStatus`).
  - Trạng thái: `PENDING`, `ACCEPTED`, `BLOCKED`.
- [x] Tạo `FriendshipRepository.java` extend `JpaRepository` với custom queries (`@Query`).
- [x] Viết `FriendshipService.java`:
  - Gửi lời mời kết bạn.
  - Chấp nhận lời mời.
  - Hủy kết bạn.
  - Lấy danh sách bạn bè (enriched với online status từ Redis).
  - Lấy danh sách lời mời đang chờ.
- [x] Viết `FriendshipController.java`:
  - `POST /api/v1/friends/request` — Gửi lời mời.
  - `PUT /api/v1/friends/accept/{id}` — Chấp nhận.
  - `DELETE /api/v1/friends/{id}` — Hủy kết bạn.
  - `GET /api/v1/friends` — Danh sách bạn bè.
  - `GET /api/v1/friends/requests` — Danh sách lời mời.

---

## Phase 5: Backend Core — Video Call với LiveKit (Hoàn thành ✅)
> **Tương đương Go:** `call_usecase.go`, `call_handler.go`, `infrastructure/livekit/`

- [x] Thêm dependency LiveKit Java SDK (`livekit-server` v0.8.1) vào `pom.xml`.
- [x] Viết `CallService.java`:
  - Generate access token cho participant (RoomJoin + RoomName grants).
- [x] Viết `CallController.java`:
  - `POST /api/v1/calls/start` — Bắt đầu cuộc gọi + thông báo WebSocket.
  - `POST /api/v1/calls/join/{roomName}` — Tham gia cuộc gọi.
- [ ] Kiểm tra kết nối giữa Spring Boot ↔ LiveKit Server (Docker).

---

## Phase 6: Web UI Foundation (Giữ nguyên từ bản Go ✅)
> Frontend Vite + Vanilla JS — **không thay đổi logic**, chỉ đảm bảo API URL mapping chính xác.

- [x] Khởi tạo dự án Vite trong thư mục `ui` (`package.json`, `vite.config.js`).
- [x] Thiết lập file gốc `index.html` và favicon svg.
- [x] Xây dựng Design System dùng CSS custom properties (`index.css`).
- [x] Xây dựng store (`store.js`) để quản lý trạng thái.
- [x] Viết file call API (`api.js`).
- [x] Viết file Websocket client (`websocket.js`).
- [ ] **[Migration]** Cập nhật `api.js`: đảm bảo base URL trỏ đúng Spring Boot server (`http://localhost:8080`).
- [ ] **[Migration]** Cập nhật `websocket.js`: đảm bảo WebSocket URL tương thích (`ws://localhost:8080/ws`).

---

## Phase 7: Auth Pages (Giữ nguyên UI ✅)
- [x] Viết file CSS cho layout và component đăng nhập/đăng ký (`auth.css`).
- [x] Viết logic hiển thị và xử lý đăng nhập/đăng ký (`auth.js`).
- [x] Tạo thư viện icon (`icons.js`).
- [ ] **[Migration]** Kiểm tra request/response format tương thích với Spring Boot API.

---

## Phase 8: Chat Interface (Giữ nguyên UI ✅)
- [x] Viết file CSS (`chat.css`) cho sidebar và chat panel.
- [x] Viết file CSS component (`components.css`).
- [x] Xây dựng view chính (`chat.js`):
    - [x] Render Sidebar (danh sách hội thoại, tìm kiếm, tab).
    - [x] Render Chat panel (bong bóng chat, typing indicator, thanh nhập text, header chat).
- [x] Liên kết `chat.js` với `store.js` và websocket để làm UI realtime.
- [x] Cập nhật `main.js` để route giữa trang Auth và Chat, logic giữ đăng nhập.
- [x] ✅ Context Menu: Click chuột phải vào tin nhắn để hiện menu (Trả lời, Thu hồi, Sao chép, Xóa).
- [x] ✅ Message Status: Hiển thị trạng thái tin nhắn (Đang gửi ⏳, Đã gửi ✓, Đã xem ✓✓ xanh).
- [x] ✅ Emoji & Sticker Picker: Bộ chọn emoji inline đầy đủ (6 danh mục, tìm kiếm, click-to-insert).
- [x] ✅ Drag & Drop: Kéo thả file trực tiếp vào khung chat (overlay + xử lý file).
- [ ] Image/File Gallery: Hiển thị danh sách ảnh/file đã gửi trong "Thông tin hội thoại".

---

## Phase 9: Friends & Social UI
- [x] Tab danh sách bạn bè / Phân loại "Bạn bè" và "Nhóm".
- [ ] Friend Request Manager: UI danh sách lời mời kết bạn (Đồng ý/Từ chối).
- [ ] Global Search: Ô tìm kiếm thông minh — kết quả phân loại theo Người dùng, Nhóm, Tin nhắn.
- [ ] Group Profile UI: Xem thành viên nhóm, đổi tên, đổi ảnh đại diện.

---

## Phase 10: Call & Media UI (LiveKit)
- [x] Tích hợp LiveKit core.
- [ ] Incoming Call Overlay: Lớp phủ khi có cuộc gọi đến (Nhận/Từ chối).
- [ ] Call Controls: Giao diện (Mute mic, Tắt camera, Share màn hình, Kết thúc).
- [ ] Mini Video Player: Picture-in-Picture khi quay lại nhắn tin.

---

## Phase 11: Premium UX & Polish

### ✅ Đã hoàn thành (từ bản Go)
- [x] ✅ Dark Mode: CSS Variables-based.
- [x] ✅ Skeleton Loading: Gray placeholders khi tải dữ liệu.
- [x] ✅ Sidebar Phải (Bảng thông tin hội thoại) — ẩn/hiện bằng nút "i".
- [x] ✅ Thanh Cảm Xúc (Message Reactions) — hover bong bóng → thanh icon (👍 ❤️ 😂 😮 😢 😡).
- [x] ✅ Hệ Thống Âm Thanh — WebAudio "ting" khi nhận tin, xì khi gửi.
- [x] ✅ Micro-animations — messagePopIn, presencePulse, emojiPop, context menu slide-in.
- [x] ✅ Custom Scrollbar (macOS Style) — 5px, bo tròn, auto-hide.

### 🔲 Chưa hoàn thành
- [ ] Virtual Scroll: Tối ưu render hàng ngàn tin nhắn.
- [ ] Profile Page: User tự đổi Avatar (preview trước khi upload).

---

## 📦 Icons đã có
- `copy`, `reply`, `trash`, `undo`, `download`, `link`
- `chevronRight`, `chevronDown`, `file`, `grid`
- `volume`, `volumeOff`, `monitor`, `pin`
- `bell`, `bellOff`, `upload`, `panelRight`

---

## 🔑 Mapping Go → Spring Boot (Tham khảo nhanh)

| Go (hiện tại)                        | Spring Boot (mới)                         |
|--------------------------------------|-------------------------------------------|
| `go run ./cmd/server`                | `mvn spring-boot:run`                     |
| `gin.Engine` (Router)                | `@RestController` + `@RequestMapping`     |
| `gin.Context`                        | `@RequestBody`, `@PathVariable`, `@RequestParam` |
| `gorilla/websocket`                  | `TextWebSocketHandler` / STOMP            |
| `internal/domain/*.go`               | `domain/*.java` (JPA Entity / MongoDB Document) |
| `internal/usecase/*.go`              | `service/*.java` (`@Service`)             |
| `internal/delivery/http/*.go`        | `controller/*.java` (`@RestController`)   |
| `internal/delivery/ws/*.go`          | `websocket/*.java`                        |
| `internal/repository/*.go`           | `repository/*.java` (Spring Data)         |
| `internal/middleware/auth.go`        | `security/JwtAuthenticationFilter.java`   |
| `internal/config/config.go`          | `application.yml` + `config/*.java`       |
| `go.mod` / `go.sum`                  | `pom.xml` (Maven) hoặc `build.gradle`     |
| `swaggo` (Swagger)                   | `SpringDoc OpenAPI`                       |
| `.env` (godotenv)                    | `application.yml` + Spring Profiles       |

---

## 🏃 Cách chạy

```bash
# 1. Bật các database (Postgres, Mongo, Redis, LiveKit)
docker-compose up -d

# 2. Chạy Backend (Spring Boot) — cần Java 21
cd backend
export JAVA_HOME="$(pwd)/jdk-21.0.10+7/Contents/Home"
./mvnw spring-boot:run

# 3. Di chuyển vào thư mục giao diện Web
cd ui

# 4. Cài đặt thư viện front-end (chỉ cần chạy lần đầu)
npm install

# 5. Khởi chạy Vite Server
npm run dev
```

---

## 📋 Thứ tự thực hiện ưu tiên

```
Phase 0–5 (Backend Spring Boot)     ✅ DONE
    ↓
Phase 6–8 (Frontend Migration — cập nhật api.js, websocket.js, test E2E)
    ↓
Phase 9 (Friends UI)
    ↓
Phase 10 (Call UI)
    ↓
Phase 11 (Premium UX & Polish)
```