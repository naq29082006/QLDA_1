# Lệnh kết nối MongoDB

## 1. Khởi động MongoDB Database

### Windows:
```bash
# Kiểm tra MongoDB service
net start MongoDB

# Nếu chưa chạy, khởi động:
net start MongoDB
```

### Linux/Mac:
```bash
# Khởi động MongoDB service
sudo systemctl start mongod

# Hoặc
sudo service mongod start

# Kiểm tra trạng thái
sudo systemctl status mongod
```

## 2. Kết nối MongoDB bằng MongoDB Shell

### Windows:
```bash
# Mở Command Prompt và chạy:
mongosh

# Hoặc nếu dùng mongo (phiên bản cũ):
mongo
```

### Kết nối đến database cụ thể:
```bash
mongosh mongodb://127.0.0.1:27017/Duan1
```

## 3. Khởi động Node.js Server (Backend)

```bash
# Di chuyển vào thư mục MongoDB Server
cd MongoDBSever

# Cài đặt dependencies (nếu chưa cài)
npm install

# Khởi động server
npm start

# Hoặc
node ./bin/www
```

## 4. Kiểm tra kết nối

Sau khi khởi động server, bạn sẽ thấy:
```
✅ connect success
Server đang lắng nghe trên port 3000
```

## 5. Test kết nối từ Browser

Mở browser và truy cập:
```
http://localhost:3000/api/products
```

Nếu thấy JSON response, nghĩa là đã kết nối thành công!

## 6. Thêm dữ liệu mẫu

```bash
cd MongoDBSever
node seed-auto.js
```

## Quick Start (Tất cả trong một):

### Windows:
```bash
# 1. Khởi động MongoDB
net start MongoDB

# 2. Khởi động Server
cd MongoDBSever
npm start

# 3. Thêm dữ liệu (trong terminal khác)
cd MongoDBSever
node seed-auto.js
```

### Linux/Mac:
```bash
# 1. Khởi động MongoDB
sudo systemctl start mongod

# 2. Khởi động Server
cd MongoDBSever
npm start

# 3. Thêm dữ liệu (trong terminal khác)
cd MongoDBSever
node seed-auto.js
```

## Connection String trong code:

- **MongoDB URI**: `mongodb://127.0.0.1:27017/Duan1`
- **Database name**: `Duan1`
- **Port**: `27017` (MongoDB mặc định)
- **API Server**: `http://localhost:3000` hoặc `http://YOUR_IP:3000`

## Troubleshooting:

### MongoDB không khởi động:
```bash
# Windows: Kiểm tra service
services.msc
# Tìm "MongoDB" và khởi động thủ công

# Linux: Xem log
sudo journalctl -u mongod
```

### Port 3000 đã được sử dụng:
```bash
# Windows: Tìm process đang dùng port 3000
netstat -ano | findstr :3000

# Linux/Mac:
lsof -i :3000
```

### Không kết nối được từ Android:
- Kiểm tra IP address trong `ApiServices.java`
- Đảm bảo Android và Server cùng mạng WiFi
- Tắt Firewall tạm thời để test

