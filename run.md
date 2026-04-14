# 🚀 Hướng Dẫn Chạy Zalo Clone (Spring Boot)

## Yêu cầu hệ thống

| Phần mềm | Phiên bản | Ghi chú |
|-----------|-----------|---------|
| **Docker Desktop** | Latest | Chạy PostgreSQL, MongoDB, Redis |
| **Java** | 21+ | Đã tải sẵn trong `backend/jdk-21.0.10+7/` |
| **Node.js** | 18+ | Frontend (Vite) |
| **LiveKit** | 1.10+ | Chạy native hoặc Docker |

---

## Bước 1 — Khởi động Database (Docker)

> ⚠️ Nếu các container `zalo-postgres`, `zalo-mongo`, `zalo-redis` đã chạy sẵn (từ project Go cũ), **bỏ qua bước này**.

```bash
cd "/Users/dangkhoii/3D Web Development/zalo-clone-springboot"
docker-compose up -d postgres mongo redis
```

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

## Bước 2 — Khởi động LiveKit (Terminal riêng)

> ⚠️ Nếu LiveKit đã chạy native ở port 7880, **bỏ qua bước này**.

```bash
livekit-server --config livekit.yaml --dev --bind 0.0.0.0
```

---

## Bước 3 — Khởi động Backend Spring Boot (Terminal riêng)

> ⚠️ **Quan trọng**: Nếu Go backend đang chạy ở port 8080, tắt nó trước (Ctrl+C).

```bash
cd "/Users/dangkhoii/3D Web Development/zalo-clone-springboot/backend"
export JAVA_HOME="$(pwd)/jdk-21.0.10+7/Contents/Home"
./mvnw spring-boot:run
```

Kết quả mong đợi:
```
✅ HikariPool-1 - Start completed.        (PostgreSQL connected)
✅ Monitor thread successfully connected   (MongoDB connected)
✅ Tomcat started on port 8080
🌐 Server running on http://localhost:8080
📡 WebSocket endpoint: ws://localhost:8080/api/v1/ws
❤️  Health check: http://localhost:8080/api/v1/health
📖 Swagger UI: http://localhost:8080/swagger-ui.html
```

---

## Bước 4 — Khởi động Frontend Vite (Terminal riêng)

```bash
cd "/Users/dangkhoii/3D Web Development/zalo-clone-springboot/ui"
npm install    # chỉ cần chạy lần đầu
npm run dev
```

Kết quả mong đợi:
```
VITE v6.x.x  ready in 200ms
➜  Local:   http://localhost:5173/
```

---

## 🌐 Mở ứng dụng

Mở trình duyệt: **http://localhost:5173**

### Tài khoản test

| Email | Mật khẩu |
|-------|----------|
| `test1@gmail.com` | `123456` |
| `test2@gmail.com` | `123456` |

> 💡 **Tip**: Mở 2 tab để test nhắn tin và gọi video giữa 2 tài khoản.

---

## ✅ Kiểm tra các service

| Service | URL | Kỳ vọng |
|---------|-----|---------|
| Backend API | http://localhost:8080/api/v1/health | `{"status":"ok"}` |
| Swagger UI | http://localhost:8080/swagger-ui.html | API documentation |
| LiveKit | http://localhost:7880 | `OK` |
| Frontend | http://localhost:5173 | Trang đăng nhập |

---

## 🛑 Dừng ứng dụng

```bash
# Dừng Backend + Frontend: nhấn Ctrl+C trong mỗi terminal

# Dừng Database (tùy chọn — có thể giữ cho project Go)
docker-compose down
```

---

## 🔧 Xử lý lỗi thường gặp

### Port 8080 đã bị chiếm (Go backend đang chạy)
```bash
# Kiểm tra ai đang dùng port
lsof -i:8080

# Tắt process đó
lsof -ti:8080 | xargs kill -9
```

### Database không kết nối được
```bash
docker-compose down && docker-compose up -d postgres mongo redis
```

### Frontend không hiện dữ liệu
```bash
# Kiểm tra Backend
curl http://localhost:8080/api/v1/health

# Kiểm tra console trình duyệt (F12)
```