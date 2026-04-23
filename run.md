# 🚀 Hướng Dẫn Chạy Zalo Clone

## Yêu cầu hệ thống

| Phần mềm | Phiên bản | Ghi chú |
|-----------|-----------|---------|
| **Docker Desktop** | Latest | Chạy PostgreSQL, MongoDB, Redis |
| **Go** | 1.26+ | Backend server |
| **Node.js** | 18+ | Frontend (Vite) |
| **LiveKit** | 1.10+ | Video call server |

---

## Bước 1 — Khởi động Database (Docker)

```bash
cd "/Users/dangkhoii/3D Web Development/zalo-clone"
docker-compose up -d postgres mongo redis
```

> ⏳ Chờ ~10 giây cho các service khởi động xong

**Kiểm tra:**
```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```
Kết quả mong đợi:
```
NAMES           STATUS
zalo-postgres   Up ... (healthy)
zalo-mongo      Up ... (healthy)
zalo-redis      Up ... (healthy)
```

---

## Bước 2 — Khởi động LiveKit Server (Terminal riêng)

> ⚠️ **QUAN TRỌNG**: Chạy LiveKit **native** (KHÔNG dùng Docker) trên macOS.  
> Nếu container Docker LiveKit đang chạy, dừng nó trước: `docker stop zalo-livekit`

**Cài đặt lần đầu (nếu chưa có):**
```bash
brew install livekit
```

**Khởi động:**
```bash
cd "/Users/dangkhoii/3D Web Development/zalo-clone"
livekit-server --config livekit.yaml --dev --bind 0.0.0.0
```

Kết quả mong đợi:
```
INFO  starting LiveKit server  {"portHttp": 7880, "nodeIP": "192.168.x.x"}
```

---

## Bước 3 — Khởi động Backend Go (Terminal riêng)

```bash
cd "/Users/dangkhoii/3D Web Development/zalo-clone/backend"
go run ./cmd/server
```

Kết quả mong đợi:
```
🚀 Starting Zalo Clone Server...
✅ Connected to PostgreSQL
✅ PostgreSQL migrations completed
✅ MongoDB indexes created
✅ Connected to MongoDB
✅ Connected to Redis
🌐 Server running on http://0.0.0.0:8080
📡 WebSocket endpoint: ws://0.0.0.0:8080/api/v1/ws
```

---

## Bước 4 — Khởi động Frontend Vite (Terminal riêng)

```bash
cd "/Users/dangkhoii/3D Web Development/zalo-clone/ui"
npm run dev
```

Kết quả mong đợi:
```
VITE v6.4.2  ready in 200ms
➜  Local:   http://localhost:5173/
```

---

## 🌐 Mở ứng dụng

Mở trình duyệt và truy cập: **http://localhost:5173**

### Tài khoản test

| Email | Mật khẩu |
|-------|----------|
| `test1@gmail.com` | `123456` |
| `test2@gmail.com` | `123456` |

> 💡 **Tip**: Mở 2 tab/trình duyệt khác nhau để đăng nhập 2 tài khoản và test nhắn tin, gọi video.

---

## ✅ Kiểm tra các service

| Service | URL | Kỳ vọng |
|---------|-----|---------|
| Backend API | http://localhost:8080/api/v1/health | `{"status":"ok"}` |
| LiveKit | http://localhost:7880 | `OK` |
| Frontend | http://localhost:5173 | Trang đăng nhập/chat |
| Swagger Docs | http://localhost:8080/swagger/index.html | API documentation |

---

## 🛑 Dừng ứng dụng

```bash
# Dừng Frontend, Backend, LiveKit: nhấn Ctrl+C trong mỗi terminal

# Dừng Database
cd "/Users/dangkhoii/3D Web Development/zalo-clone"
docker-compose down
```

---

## 🔧 Xử lý lỗi thường gặp

### Port đã bị chiếm
```bash
# Kiểm tra port nào đang dùng
lsof -ti:8080   # Backend
lsof -ti:5173   # Frontend
lsof -ti:7880   # LiveKit

# Kill process đang chiếm port
lsof -ti:8080 | xargs kill -9
```

### Database không kết nối được
```bash
# Restart tất cả container
docker-compose down && docker-compose up -d postgres mongo redis
```

### LiveKit video call không kết nối
- Đảm bảo chạy LiveKit **native** (không phải Docker)
- Kiểm tra API key/secret **PHẢI khớp** giữa `livekit.yaml` và `.env`:
  ```yaml
  # livekit.yaml
  keys:
    devkey: devsecret
  ```
  ```env
  # .env
  LIVEKIT_API_KEY=devkey
  LIVEKIT_API_SECRET=devsecret
  ```
- Nếu thay đổi secret, **phải restart backend** để load lại `.env`

### Frontend không hiện dữ liệu
- Kiểm tra Backend đang chạy: `curl http://localhost:8080/api/v1/health`
- Kiểm tra console trình duyệt (F12) để xem lỗi

docker-compose up -d postgres mongo redis
livekit-server --config livekit.yaml --dev --bind 0.0.0.0
cd backend && go run ./cmd/server
cd ui && npm run dev  