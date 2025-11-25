# Hướng dẫn sửa nút Run không ấn được

## Bước 1: Sync Gradle (QUAN TRỌNG NHẤT)

1. **Click vào nút "Sync Now"** ở trên màn hình (nếu có)
2. Hoặc vào menu: **File → Sync Project with Gradle Files**
3. Chờ đến khi thấy thông báo: **"Gradle build finished"** ở thanh trạng thái dưới cùng

## Bước 2: Kiểm tra Run Configuration

1. Ở thanh toolbar trên cùng, tìm dropdown menu bên cạnh nút Run (có thể hiển thị "app" hoặc "No run configurations")
2. Click vào dropdown đó
3. Nếu thấy "Edit Configurations..." hoặc "No run configurations":
   - Click vào "Edit Configurations..."
   - Click dấu **+** (Add New Configuration)
   - Chọn **Android App**
   - Đặt tên: `app`
   - Module: chọn `app` từ dropdown
   - Click **Apply** → **OK**

## Bước 3: Chọn thiết bị

1. Ở thanh toolbar, bên trái nút Run, có dropdown chọn thiết bị
2. Click vào dropdown đó
3. Chọn **Pixel 3** (hoặc emulator/thiết bị khác)
4. Nếu chưa có emulator:
   - Click icon **Device Manager** (hình điện thoại)
   - Click nút **Play** (▶) bên cạnh Pixel 3 để khởi động emulator
   - Chờ emulator khởi động xong

## Bước 4: Clean và Rebuild

1. **Build → Clean Project**
2. Chờ hoàn thành
3. **Build → Rebuild Project**
4. Chờ build xong (kiểm tra tab "Build" ở dưới cùng)

## Bước 5: Invalidate Caches (nếu vẫn không được)

1. **File → Invalidate Caches...**
2. Chọn **Invalidate and Restart**
3. Chờ Android Studio khởi động lại
4. Sau khi khởi động lại, sync Gradle lại (Bước 1)

## Bước 6: Kiểm tra Build Output

1. Mở tab **Build** ở dưới cùng màn hình
2. Xem có lỗi màu đỏ nào không
3. Nếu có lỗi, copy và gửi lỗi đó

## Sau khi làm xong:

- Nút Run (▶) sẽ sáng lên và có thể click được
- Click vào nút Run để chạy ứng dụng

## Lưu ý:

- **minSdk = 26** (Android 8.0) - Pixel 3 (API 29) tương thích ✓
- Đảm bảo emulator đã khởi động hoàn toàn trước khi chạy
- Nếu vẫn không được, kiểm tra tab "Build" để xem lỗi cụ thể

