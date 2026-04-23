# Tiến độ UI Zalo Clone Web

## Phase 1: Backend Enhancements (Hoàn thành)
- [x] Tạo `conversation_handler.go` với các endpoints REST (`/conversations`, `/conversations/:id/messages`).
- [x] Đăng ký endpoints mới trong file `router.go`.
- [x] Liên kết `MessageUsecase` với `ConversationHandler` trong `main.go`.
- [x] Sửa cấu hình CORS để tương thích với local development (`http://localhost:5173`).

## Phase 2: Web UI Foundation (Hoàn thành)
- [x] Khởi tạo dự án Vite trong thư mục `ui` (`package.json`, `vite.config.js`).
- [x] Thiết lập file gốc `index.html` và favicon svg (`favicon.svg`).
- [x] Xây dựng Design System dùng CSS custom properties (`index.css`).
- [x] Xây dựng store (`store.js`) để quản lý trạng thái.
- [x] Viết file call API (`api.js`).
- [x] Viết file Websocket client (`websocket.js`).

## Phase 3: Auth Pages (Hoàn thành)
- [x] Viết file CSS cho layout và component đăng nhập/đăng ký (`auth.css`).
- [x] Viết logic hiển thị và xử lý đăng nhập/đăng ký (`auth.js`).
- [x] Tạo thư viện icon (`icons.js`).

## Phase 4: Chat Interface (Hoàn thành 90%)
- [x] Viết file CSS (`chat.css`) cho sidebar và chat panel.
- [x] Viết file CSS component (`components.css`).
- [x] Xây dựng view chính (`chat.js`):
    - [x] Render Sidebar (danh sách hội thoại, tìm kiếm, tab).
    - [x] Render Chat panel (bong bóng chat, typing indicator, thanh nhập text, header chat).
- [x] Liên kết `chat.js` với `store.js` và websocket để làm UI realtime.
- [x] Cập nhật `main.js` để route giữa trang Auth và Chat, logic giữ đăng nhập.

## Phase 5: Friends & Video Call (Sắp tới)
- [x] Màn hình danh sách bạn bè / Tab bạn bè (`friendsPanel.js`).
- [x] Model gửi/nhận/xoá kết bạn, xoá bạn.
- [x] UI tạo nhóm chat mới.
- [x] Tích hợp LiveKit / Video UI (`videoCall.js`).

## Phase 6: Polish & Verify 
- [x] NPM install trên máy local của user.
- [ ] Bug fix.


🚀 Tiến độ UI Zalo Clone Web (Updated 2026)
## Phase 4: Chat Interface (Nâng cấp độ chi tiết)
- [x] Render Sidebar & Chat panel cơ bản.
- [x] ✅ Context Menu: Click chuột phải vào tin nhắn để hiện menu (Trả lời, Thu hồi, Sao chép, Xóa).
- [x] ✅ Message Status: Hiển thị trạng thái tin nhắn (Đang gửi ⏳, Đã gửi ✓, Đã xem ✓✓ xanh).
- [x] ✅ Emoji & Sticker Picker: Bộ chọn emoji inline đầy đủ (6 danh mục, tìm kiếm, click-to-insert).
- [x] ✅ Drag & Drop: Kéo thả file trực tiếp vào khung chat (overlay + xử lý file).
- [ ] [Mới] Image/File Gallery: Hiển thị danh sách ảnh/file đã gửi trong mục "Thông tin hội thoại" ở sidebar bên phải.

## Phase 5: Friends & Social UI (Hoàn thiện)
- [x] Tab danh sách bạn bè / Phân loại "Bạn bè" và "Nhóm".
- [ ] [Mới] Friend Request Manager: UI danh sách lời mời kết bạn đã nhận và đã gửi (có nút Đồng ý/Từ chối).
- [ ] [Mới] Global Search: Ô tìm kiếm thông minh, hiển thị kết quả phân loại theo Người dùng, Nhóm hoặc Tin nhắn.
- [ ] [Mới] Group Profile UI: Màn hình xem danh sách thành viên nhóm, đổi tên nhóm, đổi ảnh đại diện nhóm.

## Phase 6: Call & Media UI (Chuyên nghiệp với LiveKit)
- [x] Tích hợp LiveKit core.
- [ ] [Mới] Incoming Call Overlay: Khi có người gọi, hiện một lớp phủ (overlay) toàn màn hình hoặc thông báo góc trên có nút Nhận/Từ chối.
- [ ] [Mới] Call Controls: Giao diện khi đang gọi (Mute mic, Tắt camera, Share màn hình, Kết thúc).
- [ ] [Mới] Mini Video Player: Khi đang gọi mà quay lại nhắn tin, video call thu nhỏ vào một góc (Picture-in-Picture style).

## Phase 7: Polish & UX "Sịn Sò"
- [x] ✅ Dark Mode: Giao diện tối dựa trên CSS Variables (đã tích hợp sẵn từ đầu).
- [x] ✅ Skeleton Loading: Hiển thị khung xương (gray placeholders) khi đang tải danh sách hội thoại + tin nhắn cũ.
- [ ] [Mới] Virtual Scroll: Tối ưu hóa render khi hội thoại có hàng ngàn tin nhắn (giúp app trên MacBook chạy cực mượt).
- [ ] [Mới] Profile Page: Trang cá nhân cho phép user tự đổi Avatar (Preview ảnh trước khi upload).

## Phase 8: Trải nghiệm người dùng cao cấp (Premium UX)
Giai đoạn này tập trung vào những chi tiết nhỏ nhưng lại là thứ tạo nên cảm giác "xịn" và chuyên nghiệp cho một ứng dụng nhắn tin hàng đầu.

- [x] ✅ Sidebar Phải (Bảng thông tin hội thoại):
  - Thiết kế cột thứ 3 bên phải (ẩn/hiện bằng nút "i").
  - Hiển thị chi tiết thông tin cá nhân/nhóm.
  - Kho Lưu Trữ (Media Gallery): Phân loại theo tab "Ảnh", "File", và "Liên kết".
  - Danh sách thành viên nhóm.

- [x] ✅ Thanh Cảm Xúc (Message Reactions):
  - Khi di chuột (hover) vào bong bóng tin nhắn, hiện thanh icon nổi lên (👍 ❤️ 😂 😮 😢 😡).
  - Hiệu ứng transition mượt mà khi thanh này xuất hiện.
  - Click để thêm reaction, hiện chip reaction bên dưới tin nhắn.

- [x] ✅ Hệ Thống Âm Thanh:
  - Tiếng "Ting" WebAudio khi có tin nhắn mới (chỉ kích hoạt khi tab không được focus).
  - Tiếng xì khi gửi tin nhắn.
  - Nút bật/tắt âm thanh trong menu Settings.

- [x] ✅ Hiệu Ứng Chuyển Động Vi Mô (Micro-animations):
  - Message Pop-in: Tin nhắn mới trượt nhẹ từ dưới lên với `messagePopIn` keyframe.
  - Presence Indicator: Chấm xanh trạng thái Online có hiệu ứng `presencePulse` (đập nhẹ).
  - Emoji Pop: Reaction chips animate với `emojiPop`.
  - Context Menu: Slide-in animation.
  - Info Panel: Slide-in-from-right animation.
  - Drop Zone: Pulse animation.

- [x] ✅ Custom Scrollbar (macOS Style):
  - Thanh cuộn 5px, bo tròn.
  - Chỉ hiện khi cuộn (auto-hide).
  - Phong cách thẩm mỹ macOS.

## 📦 Icons mới đã thêm
- `copy`, `reply`, `trash`, `undo`, `download`, `link`
- `chevronRight`, `chevronDown`, `file`, `grid`
- `volume`, `volumeOff`, `monitor`, `pin`
- `bell`, `bellOff`, `upload`, `panelRight`


cách chạy:
# Bật các database (Postgres, Mongo, Redis, LiveKit)
docker-compose up -d

# Chạy Backend (Go)
cd backend
go run ./cmd/server

# Di chuyển vào thư mục giao diện Web 
cd ui

# Cài đặt thư viện front-end (chỉ cần chạy lần đầu)
npm install

# Khởi chạy Vite Server
npm run dev