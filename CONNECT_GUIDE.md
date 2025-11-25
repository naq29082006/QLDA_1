# Hướng dẫn kết nối MongoDB Server

## 📋 Các bước kết nối:

### 1. Khởi động MongoDB Database

#### Windows:
```bash
# Mở Command Prompt với quyền Administrator
net start MongoDB
```

#### Linux/Mac:
```bash
sudo systemctl start mongod
# hoặc
sudo service mongod start
```

### 2. Cài đặt Dependencies (nếu chưa cài)

```bash
cd MongoDBSever
npm install
```

### 3. Khởi động Node.js Server

#### Cách 1: Sử dụng script (Dễ nhất)

**Windows:**
```bash
cd MongoDBSever
start.bat
```

**Linux/Mac:**
```bash
cd MongoDBSever
chmod +x start.sh
./start.sh
```

#### Cách 2: Sử dụng npm
```bash
cd MongoDBSever
npm start
```

#### Cách 3: Sử dụng node trực tiếp
```bash
cd MongoDBSever
node ./bin/www
```

### 4. Kiểm tra kết nối thành công

Khi server khởi động thành công, bạn sẽ thấy:
```
✅ connect success
Server đang lắng nghe trên port 3000
```

### 5. Cấu hình IP trong Android App

1. Mở file: `app/src/main/java/com/example/duan1/services/ApiServices.java`
2. Tìm dòng: `String Url = "http://192.168.0.114:3000/";`
3. Thay đổi IP thành IP của máy bạn:

**Để lấy IP của máy:**

**Windows:**
```bash
ipconfig
# Tìm "IPv4 Address" (ví dụ: 192.168.1.100)
```

**Linux/Mac:**
```bash
ifconfig
# hoặc
ip addr
# Tìm IP của WiFi/Ethernet adapter
```

4. Cập nhật trong `ApiServices.java`:
```java
String Url = "http://YOUR_IP:3000/";
```

## 🔧 Troubleshooting

### Lỗi "connect fail"
- ✅ Kiểm tra MongoDB có đang chạy: `net start MongoDB` (Windows) hoặc `sudo systemctl status mongod` (Linux)
- ✅ Kiểm tra connection string trong `MongoDBSever/config/db.js`

### Lỗi "Port 3000 already in use"
- ✅ Đóng process đang dùng port 3000
- ✅ Hoặc thay đổi port trong `MongoDBSever/bin/www`

### Android không kết nối được
- ✅ Đảm bảo Android và Server cùng mạng WiFi
- ✅ Kiểm tra IP address trong `ApiServices.java` đúng chưa
- ✅ Tắt Firewall tạm thời để test
- ✅ Kiểm tra server có chạy: mở browser và vào `http://YOUR_IP:3000`

### MongoDB không khởi động được
- ✅ Kiểm tra MongoDB đã được cài đặt chưa
- ✅ Kiểm tra service MongoDB: `services.msc` (Windows)
- ✅ Xem log MongoDB để biết lỗi cụ thể

## 📱 Test API

Sau khi server chạy, bạn có thể test API bằng:

1. **Browser:** Mở `http://localhost:3000/api/products`
2. **Postman:** Test các endpoint API
3. **Android App:** Chạy app và kiểm tra kết nối

## ✅ Checklist

- [ ] MongoDB đang chạy
- [ ] Dependencies đã cài đặt (`npm install`)
- [ ] Node.js server đang chạy trên port 3000
- [ ] Thấy message "✅ connect success"
- [ ] IP address trong `ApiServices.java` đã được cập nhật
- [ ] Android và Server cùng mạng WiFi
- [ ] Firewall không chặn port 3000

## 🚀 Quick Start

```bash
# 1. Khởi động MongoDB
net start MongoDB          # Windows
# hoặc
sudo systemctl start mongod # Linux/Mac

# 2. Khởi động Server
cd MongoDBSever
npm start

# 3. Kiểm tra: Mở browser vào http://localhost:3000
```

