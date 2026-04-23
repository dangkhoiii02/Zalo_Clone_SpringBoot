# TỔNG KẾT DỰ ÁN ZALO CLONE

**Ngày lập:** 16/04/2026  
**Dự án:** Zalo Clone – Ứng dụng nhắn tin và gọi video thời gian thực  
**Công nghệ:** Java Spring Boot 3.5 (Backend) + Vite + Vanilla JavaScript (Frontend) + Docker Compose (Infrastructure)

---

## 1. Tổng quan kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                    Frontend (Vite SPA - Port 5173)              │
│  index.html → main.js → store.js + api.js + websocket.js       │
│  Pages: auth.js | chat.js | friendsPanel.js | videoCall.js      │
│  Styles: index.css | auth.css | chat.css | components.css       │
└──────────┬──────────────┬────────────────────┬──────────────────┘
           │ HTTP/REST     │ WebSocket          │ WebRTC
           ▼              ▼                    ▼
┌──────────────────────────────────────────────────────────────────┐
│              Backend - Spring Boot 3.5 (Port 8080)              │
│                                                                  │
│  Delivery:  AuthController | UserController | FriendshipCtrl     │
│             ConversationController | CallController              │
│             WebSocket Hub (register/unregister/broadcast)        │
│  Middleware: AuthMiddleware (JWT) | CORSMiddleware               │
│                                                                  │
│  Service:   AuthService | UserService | FriendshipService        │
│             MessageService | CallService                         │
│                                                                  │
│  Repository: UserRepo | FriendshipRepo | MessageRepo             │
│              ConversationRepo | PresenceRepo                     │
│                                                                  │
│  Domain:    User | Friendship | Message | Conversation | Room    │
└──────┬──────────┬──────────┬──────────────────┬─────────────────┘
       │          │          │                  │
       ▼          ▼          ▼                  ▼
┌──────────┐ ┌────────┐ ┌────────┐ ┌─────────────────────┐
│PostgreSQL│ │MongoDB │ │ Redis  │ │   LiveKit Server    │
│  17      │ │ 8.0    │ │ 7     │ │   (WebRTC SFU)      │
│Port 5432 │ │Port    │ │Port   │ │   Port 7880-7882    │
│users     │ │27017   │ │6379   │ │                     │
│friendships││messages│ │presence│ │                     │
└──────────┘ │convs   │ │typing │ └─────────────────────┘
             └────────┘ └────────┘
```

---

## 2. Backend – Đã hoàn thành

### 2.1 Domain Models (4 files)

| File | Entity | Fields chính |
|------|--------|-------------|
| `domain/user.go` | `User` | ID (UUID), Username, Email, Password (bcrypt hash), AvatarURL, Bio, CreatedAt, UpdatedAt |
| | `RegisterRequest` | Username (required, 3-50 chars), Email (required, email), Password (required, min 6) |
| | `LoginRequest` | Email (required, email), Password (required) |
| | `UpdateProfileRequest` | Username?, AvatarURL?, Bio? (all optional) |
| | `AuthResponse` | Token (JWT string), User |
| `domain/message.go` | `Message` | ID, ConversationID, SenderID, Content, Type (text/image/file/system), MediaURL, ReadBy[], CreatedAt, UpdatedAt |
| | `Conversation` | ID, Type (direct/group), Participants[], Name, LastMessage (embedded), CreatedAt, UpdatedAt |
| | `LastMessage` | Content, SenderID, CreatedAt |
| `domain/friendship.go` | `Friendship` | ID (UUID), UserID, FriendID, Status (pending/accepted/blocked), CreatedAt, UpdatedAt |
| | `FriendInfo` | FriendshipID, UserID, Username, Email, AvatarURL, Bio, Status, IsOnline, CreatedAt |
| `domain/room.go` | `Room` | Name, CallerID, CalleeID, CallerToken, CalleeToken, Status, CreatedAt |
| | `StartCallResponse` | RoomName, Token |

### 2.2 Repository Interfaces (5 interfaces, 26 methods)

| Interface | Database | Methods |
|-----------|----------|---------|
| `UserRepository` | PostgreSQL | Create, GetByID, GetByEmail, GetByUsername, Update, SearchUsers |
| `FriendshipRepository` | PostgreSQL | Create, GetByID, UpdateStatus, Delete, GetFriends, GetPendingRequests, GetFriendship |
| `MessageRepository` | MongoDB | Create, GetByConversation, MarkAsRead |
| `ConversationRepository` | MongoDB | Create, GetByID, GetByParticipants, GetUserConversations, UpdateLastMessage |
| `PresenceRepository` | Redis | SetOnline, SetOffline, IsOnline, GetOnlineUsers, SetTyping |

### 2.3 Use Cases / Services (5 services)

| Service | Methods | Logic chi tiết |
|---------|---------|---------------|
| `AuthService` | `Register()` | Check email duplicate → Check username duplicate → bcrypt hash (DefaultCost) → Create User (UUID) → Generate JWT (HS256, {user_id, username, email, exp: now+24h}) |
| | `Login()` | FindByEmail → bcrypt CompareHashAndPassword → Generate JWT |
| | `GetUserByID()` | FindByID → return user |
| `UserService` | `UpdateProfile()` | Update optional fields (username, avatar, bio) |
| | `SearchUsers()` | Search by query string, limit results |
| `FriendshipService` | `SendRequest()` | Check self-friend → Check friend exists → Check duplicate friendship → Create (status=PENDING) |
| | `AcceptRequest()` | Get friendship → Check recipient = currentUser → Check status=PENDING → Update status=ACCEPTED |
| | `RemoveFriend()` | Get friendship → Check participant → Delete |
| | `GetFriends()` | Get accepted friendships → Enrich with online status from Redis |
| | `GetPendingRequests()` | Get friendships where status=PENDING |
| `MessageService` | `SendMessage()` | Verify conversation exists → Verify sender is participant → Default type=TEXT → Create message (ReadBy=[sender]) → Update LastMessage |
| | `GetMessages()` | Paginated (default 50, max 100) |
| | `CreateConversation()` | Auto-add creator to participants → For DIRECT: check existing (return if found), must have exactly 2 participants → Create |
| | `GetConversations()` | Get all conversations where user is participant |
| `CallService` | `StartCall()` | Generate room name "call-{UUID}" → Create LiveKit AccessToken (RoomJoin grant, identity=callerID, expiry=24h) |
| | `JoinCall()` | Create LiveKit AccessToken for callee |

### 2.4 HTTP Handlers & Routes (17 endpoints)

**Public Routes (no auth):**
```
POST /api/v1/auth/register    → AuthHandler.Register
POST /api/v1/auth/login       → AuthHandler.Login
GET  /api/v1/health           → Health check (PG + Mongo + Redis status)
GET  /swagger/*any             → Swagger UI
```

**Protected Routes (JWT AuthMiddleware):**
```
GET    /api/v1/auth/me                        → AuthHandler.Me
PUT    /api/v1/users/profile                  → UserHandler.UpdateProfile
GET    /api/v1/users/search?q=                → UserHandler.SearchUsers
POST   /api/v1/friends/request                → FriendshipHandler.SendRequest
PUT    /api/v1/friends/accept/:id             → FriendshipHandler.AcceptRequest
DELETE /api/v1/friends/:id                    → FriendshipHandler.RemoveFriend
GET    /api/v1/friends                        → FriendshipHandler.GetFriends
GET    /api/v1/friends/requests               → FriendshipHandler.GetPendingRequests
POST   /api/v1/conversations                 → ConversationHandler.CreateConversation
GET    /api/v1/conversations                  → ConversationHandler.GetConversations
GET    /api/v1/conversations/:id/messages     → ConversationHandler.GetMessages
POST   /api/v1/calls/start                    → CallHandler.StartCall
POST   /api/v1/calls/join/:roomName           → CallHandler.JoinCall
```

**WebSocket:**
```
GET /api/v1/ws?token=JWT  → WSHandler.HandleWebSocket (JWT from query param or Authorization header)
```

### 2.5 WebSocket Hub

| Component | Chi tiết |
|-----------|---------|
| **Hub** | Quản lý tất cả connections: `clients map[string]*Client`, register/unregister channels, goroutine Run() loop |
| **Client** | UserID, Username, conn, send buffer (capacity 256), ReadPump (64KB max message), WritePump (ping every 54s) |
| **Events handled** | `message` → save to MongoDB + broadcast to participants |
| | `typing` → set Redis typing + broadcast to other participants |
| | `read` → markAsRead in MongoDB + broadcast read_receipt |
| | `heartbeat` → refresh Redis presence |
| **Presence** | On register: SetOnline + broadcastPresence("online") to all clients |
| | On unregister: SetOffline + broadcastPresence("offline") |

### 2.6 Middleware

| Middleware | Logic |
|-----------|-------|
| `AuthMiddleware` | Extract "Authorization: Bearer {token}" → Parse JWT (HS256 verify) → Check valid + not expired → Extract user_id (UUID) + username → Set to Gin context |
| `CORSMiddleware` | AllowOrigins: localhost:5173, localhost:3000, 127.0.0.1:5173. AllowMethods: GET, POST, PUT, PATCH, DELETE, OPTIONS. MaxAge: 12h |

---

## 3. Frontend – Đã hoàn thành

### 3.1 Core Modules

| Module | File | Lines | Chức năng |
|--------|------|-------|-----------|
| **Entry Point** | `main.js` | 88 | init() → check stored token → auto-login → render auth/chat; `showToast()` (4 types, auto-dismiss 4s) |
| **State Store** | `store.js` | 170 | Reactive pub/sub: 14 state keys (user, token, conversations, messages, friends, onlineUsers, typingUsers, UI state); addMessage (dedup + sort by time); setTyping (auto-clear 3s); reset on logout |
| **API Client** | `api.js` | 117 | 15 methods with JWT Bearer auth; generic `request(method, path, body)` |
| **WebSocket** | `websocket.js` | 170 | Auto-reconnect (exponential backoff: `1000 × 1.5^attempt`, max 30s, max 10 retries); heartbeat 30s; event emitter; sendMessage, sendTyping, sendRead |
| **Icons** | `icons.js` | 13,162B | 40+ SVG icons (inline, no external dependencies) |

### 3.2 Pages

| Page | File | Size | Tính năng đã hoàn thành |
|------|------|------|------------------------|
| **Auth** | `auth.js` | 219 lines | ✅ Login form (email + password) |
| | | | ✅ Register form (username + email + password) |
| | | | ✅ Tab switching (Login ↔ Register animated) |
| | | | ✅ Inline validation (username ≥ 3, password ≥ 6) |
| | | | ✅ Loading spinner on submit button |
| | | | ✅ Animated background (3 gradient orbs) |
| | | | ✅ Footer toggle link ("Chưa có tài khoản? Đăng ký ngay") |
| **Chat** | `chat.js` | 61,343B | ✅ **Sidebar (cột trái):** Danh sách hội thoại + last message + avatar + time + unread dot |
| | | | ✅ **Search bar:** Tìm kiếm conversations |
| | | | ✅ **Tab switching:** Chats / Friends |
| | | | ✅ **Chat Panel (cột giữa):** Header (tên + online status + nút call/info) |
| | | | ✅ **Message bubbles:** Sent (bên phải, accent color) / Received (bên trái, dark color) |
| | | | ✅ **Input bar:** Text input + Send button + Attach + Emoji trigger |
| | | | ✅ **Context Menu:** Right-click tin nhắn → Trả lời, Thu hồi, Sao chép, Xóa |
| | | | ✅ **Message Status:** Đang gửi ⏳, Đã gửi ✓, Đã xem ✓✓ (xanh) |
| | | | ✅ **Emoji Picker:** 6 danh mục, tìm kiếm, click-to-insert |
| | | | ✅ **Drag & Drop:** Kéo thả file vào chat (overlay + pulse animation) |
| | | | ✅ **Typing Indicator:** "Username đang gõ..." |
| | | | ✅ **Info Panel (cột phải):** Toggle bằng nút "i", thông tin liên hệ/nhóm, Media Gallery (Ảnh/File/Liên kết tabs), thành viên nhóm |
| | | | ✅ **Message Reactions:** Hover → thanh 👍❤️😂😮😢😡, click → reaction chip |
| | | | ✅ **Sound System:** "Ting" nhận tin (tab unfocused), "xì" gửi tin, nút toggle sound |
| | | | ✅ **Skeleton Loading:** Gray placeholder khi loading conversations + messages |
| | | | ✅ **Settings Modal:** Edit username, avatar, bio |
| **Friends** | `friendsPanel.js` | 14,330B | ✅ Friends list với Online/Offline indicator |
| | | | ✅ Friend request list (received + sent) |
| | | | ✅ Accept / Reject buttons |
| | | | ✅ Search users (tìm bạn mới) |
| | | | ✅ Send friend request |
| | | | ✅ Remove friend (unfriend) |
| **New Group** | `newGroupModal.js` | 4,433B | ✅ Group name input |
| | | | ✅ Select participants from friends |
| | | | ✅ Create group conversation |
| **Video Call** | `videoCall.js` | 336 lines | ✅ `startCallFlow()`: API call → LiveKit connect → enable camera/mic |
| | | | ✅ `joinCallFlow()`: API call → LiveKit connect → enable camera/mic |
| | | | ✅ Full-screen overlay modal (room name + status) |
| | | | ✅ Video grid (local + remote participants) |
| | | | ✅ Controls: Toggle Mic, Toggle Camera, End Call |
| | | | ✅ Status indicator (Đang kết nối / Đã kết nối / Đang kết nối lại) |
| | | | ✅ Room cleanup on page unload (beforeunload + pagehide) |
| | | | ✅ Graceful fallback: vẫn connect dù camera bị từ chối |
| | | | ✅ LiveKit events: Connected, Reconnecting, Reconnected, Disconnected, TrackSubscribed, TrackUnsubscribed, LocalTrackPublished, LocalTrackUnpublished, MediaDevicesError |

### 3.3 CSS Design System (4 files, ~57KB)

| File | Nội dung |
|------|---------|
| `index.css` (7.6KB) | CSS Custom Properties (50+ variables), Dark mode, Base reset, Typography, Toast notifications, Custom scrollbar (macOS 5px auto-hide), Video call overlay + controls |
| `auth.css` (6.2KB) | Auth page centered card, Animated gradient orbs background, Form inputs/tabs/buttons, Loading spinner, Error states |
| `chat.css` (27.9KB) | 3-column layout (sidebar + chat + info), Conversation list items, Message bubbles (sent/received), Chat header + input, Context menu, Emoji picker, Drop zone, Typing indicator, Info panel slide-in, Online presence pulse dot |
| `components.css` (15.6KB) | Modal styles, Skeleton loading, Reaction chips + emojiPop animation, messagePopIn animation, presencePulse animation, Friends panel, Search results, Settings modal, Custom scrollbar |

### 3.4 Micro-animations (6 loại)

| Animation | Keyframe | Áp dụng |
|-----------|----------|---------|
| `messagePopIn` | translateY(10px) opacity(0) → normal | Tin nhắn mới |
| `presencePulse` | scale(1) → scale(1.3) → scale(1) | Chấm xanh online |
| `emojiPop` | scale(0) → scale(1.2) → scale(1) | Reaction chip |
| Context menu slide | translateY(-5px) opacity(0) → normal | Right-click menu |
| Info panel slide | translateX(100%) → translateX(0) | Sidebar phải |
| Drop zone pulse | border-color opacity animation | Kéo thả file |

---

## 4. Infrastructure – Docker Compose

| Service | Image | Port | Health Check | Volume |
|---------|-------|------|-------------|--------|
| `zalo-postgres` | postgres:17-alpine | 5432 | `pg_isready` | `postgres_data` |
| `zalo-mongo` | mongo:8.0 | 27017 | `mongosh ping` | `mongo_data` |
| `zalo-redis` | redis:7-alpine | 6379 | `redis-cli ping` | `redis_data` |
| `zalo-livekit` | livekit/livekit-server:latest | 7880, 7881, 7882/udp, 3478/udp, 30000-30010/udp | – | `livekit.yaml` |

---

## 5. Số liệu tổng hợp

| Metric | Giá trị |
|--------|---------|
| Backend files | ~25 files |
| Frontend files | 14 files (5 pages + 4 CSS + 5 core modules) |
| Frontend code size | ~125,000 bytes (~3,500+ lines) |
| REST API endpoints | 17 |
| WebSocket event types | 10 (message, typing, read, read_receipt, heartbeat, presence, connected, disconnected, error, reconnect_failed) |
| Domain entities | 5 (User, Friendship, Message, Conversation, Room) |
| Repository interfaces | 5 (26 methods total) |
| Service classes | 5 (15+ methods total) |
| HTTP Handlers | 5 (+ 1 WS Handler) |
| SVG icons | 40+ |
| CSS custom properties | 50+ |
| Micro-animations | 6 types |
| Docker services | 4 |
| Database types | 3 (PostgreSQL + MongoDB + Redis) |

---

## 6. Chưa hoàn thành

| # | Tính năng | Ưu tiên |
|---|-----------|---------|
| 1 | Image/File Gallery trong Info Panel | Trung bình |
| 2 | Friend Request Manager nâng cao (UI phân tab đã nhận/đã gửi) | Thấp |
| 3 | Global Search (Users/Groups/Messages) | Trung bình |
| 4 | Group Profile UI (đổi tên/ảnh nhóm) | Thấp |
| 5 | Incoming Call Overlay (thông báo cuộc gọi đến) | Cao |
| 6 | Call Controls nâng cao (Share screen, PiP) | Trung bình |
| 7 | Virtual Scroll (optimize 1000+ messages) | Trung bình |
| 8 | Profile Page (upload avatar preview) | Thấp |
| 9 | Bug fix tổng thể | Cao |

---

## 7. Cách chạy

```bash
# 1. Bật infrastructure (PostgreSQL, MongoDB, Redis, LiveKit)
docker-compose up -d

# 2. Chạy Backend
cd backend
mvn spring-boot:run
# Hoặc: go run ./cmd/server (nếu dùng Go)

# 3. Chạy Frontend
cd ui
npm install    # Chỉ lần đầu
npm run dev    # Vite dev server → http://localhost:5173
```

**Swagger UI:** http://localhost:8080/swagger/index.html
