# Hướng dẫn kết nối MongoDB Server

## Bước 1: Khởi động MongoDB (nếu chưa chạy)

### Trên Windows:
```bash
# Mở Command Prompt hoặc PowerShell với quyền Administrator
# Di chuyển đến thư mục MongoDB (thường là C:\Program Files\MongoDB\Server\<version>\bin)
cd "C:\Program Files\MongoDB\Server\7.0\bin"

# Khởi động MongoDB
mongod
```

### Hoặc sử dụng MongoDB như một service (Windows):
```bash
# Kiểm tra service đã chạy chưa
net start MongoDB

# Nếu chưa chạy, khởi động service
net start MongoDB
```

### Trên Linux/Mac:
```bash
# Khởi động MongoDB service
sudo systemctl start mongod

# Hoặc
sudo service mongod start
```

## Bước 2: Cài đặt dependencies cho Node.js server

```bash
# Di chuyển vào thư mục MongoDBSever
cd MongoDBSever

# Cài đặt các package cần thiết
npm install
```

## Bước 3: Khởi động Node.js Server

```bash
# Vẫn trong thư mục MongoDBSever
npm start

# Hoặc
node ./bin/www
```

## Bước 4: Kiểm tra kết nối

Sau khi khởi động, bạn sẽ thấy:
- ✅ `connect success` - Kết nối MongoDB thành công
- Server đang lắng nghe trên port 3000
- URL: `http://localhost:3000` hoặc `http://192.168.0.114:3000`

## Cấu hình kết nối

### MongoDB Connection String:
- Local: `mongodb://127.0.0.1:27017/Duan1`
- Database name: `Duan1`

### Server Port:
- Port: `3000`
- Host: `0.0.0.0` (lắng nghe trên tất cả interfaces)

## Lưu ý:

1. **Đảm bảo MongoDB đang chạy** trước khi khởi động Node.js server
2. **Kiểm tra IP address** trong `ApiServices.java`:
   - Mở file: `app/src/main/java/com/example/duan1/services/ApiServices.java`
   - Thay đổi IP: `String Url = "http://YOUR_IP:3000/";`
   - Để lấy IP: 
     - Windows: `ipconfig` (tìm IPv4 Address)
     - Linux/Mac: `ifconfig` hoặc `ip addr`

3. **Firewall**: Đảm bảo port 3000 không bị chặn

## Troubleshooting:

### Lỗi "connect fail":
- Kiểm tra MongoDB có đang chạy không
- Kiểm tra connection string trong `config/db.js`

### Lỗi "Port already in use":
- Đóng process đang dùng port 3000
- Hoặc thay đổi port trong `bin/www`

### Không kết nối được từ Android:
- Kiểm tra Android và server có cùng mạng WiFi không
- Kiểm tra IP address trong ApiServices.java
- Kiểm tra firewall trên máy chạy server

