# Hướng Dẫn Tích Hợp Phần Báo Cáo Thống Kê

## Tổng Quan

Phần báo cáo thống kê bao gồm 3 tab:
1. **Top Doanh Thu**: Hiển thị các ngày có doanh thu cao nhất
2. **Top Khách Hàng**: Hiển thị khách hàng mua nhiều nhất
3. **Top Sản Phẩm**: Hiển thị sản phẩm bán chạy nhất

Mỗi tab đều có bộ lọc:
- **Số lượng**: Chọn số lượng item muốn hiển thị (ví dụ: top 5, top 10)
- **Thời gian**: Chọn khoảng thời gian từ ngày đến ngày

## Cấu Trúc File Đã Tạo

### Android Studio

#### Models
- `app/src/main/java/com/example/duan1/model/TopRevenue.java`
- `app/src/main/java/com/example/duan1/model/TopCustomer.java`
- `app/src/main/java/com/example/duan1/model/TopProduct.java`

#### Adapters
- `app/src/main/java/com/example/duan1/BaoCaoTabAdapter.java` - Adapter cho tabs
- `app/src/main/java/com/example/duan1/TopRevenueAdapter.java` - Adapter cho top doanh thu
- `app/src/main/java/com/example/duan1/TopCustomerAdapter.java` - Adapter cho top khách hàng
- `app/src/main/java/com/example/duan1/TopProductAdapter.java` - Adapter cho top sản phẩm

#### Activity
- `app/src/main/java/com/example/duan1/BaoCaoAdmin.java` - Activity chính

#### Layouts
- `app/src/main/res/layout/activity_bao_cao_admin.xml`
- `app/src/main/res/layout/item_top_revenue.xml`
- `app/src/main/res/layout/item_top_customer.xml`
- `app/src/main/res/layout/item_top_product.xml` (đã cập nhật)

#### API Services
- Đã thêm 3 endpoints vào `ApiServices.java`:
  - `getTopRevenue(limit, startDate, endDate)`
  - `getTopCustomers(limit, startDate, endDate)`
  - `getTopProducts(limit, startDate, endDate)`

### Backend API

File `api_reports.js` chứa code Node.js/Express cho 3 endpoints:
- `GET /api/reports/top-revenue`
- `GET /api/reports/top-customers`
- `GET /api/reports/top-products`

## Cách Tích Hợp Backend API

### Bước 1: Tạo File Routes

Tạo file `routes/reports.js` và copy nội dung từ `api_reports.js` vào.

### Bước 2: Cập Nhật Server Chính

Trong file server chính của bạn (ví dụ: `app.js`, `index.js`, hoặc `server.js`), thêm:

```javascript
const reportsRouter = require('./routes/reports');
app.use('/api/reports', reportsRouter);
```

### Bước 3: Kiểm Tra Models

Đảm bảo các model của bạn có các trường sau:

**Order Model:**
- `_id`
- `user_id` (có thể populate)
- `status`
- `totalPrice` hoặc `total_price`
- `createdAt` hoặc `created_at`

**OrderDetail Model:**
- `_id`
- `order_id`
- `product_id` (có thể populate)
- `quantity`
- `subtotal`

**User Model:**
- `_id`
- `name`
- `email`
- `phone`

**Product Model:**
- `_id`
- `name`
- `image`
- `price`

### Bước 4: Điều Chỉnh Query (Nếu Cần)

Trong file `api_reports.js`, bạn có thể cần điều chỉnh:

1. **Status của đơn hàng**: Mặc định chỉ lấy đơn hàng có status `['đã giao', 'đã nhận', 'delivered']`. Nếu bạn dùng status khác, hãy cập nhật:

```javascript
const filter = { status: { $in: ['đã giao', 'đã nhận', 'delivered', 'your_status'] } };
```

2. **Populate fields**: Nếu cấu trúc populate của bạn khác, hãy điều chỉnh:

```javascript
const orders = await Order.find(filter).populate('user_id', 'name email phone');
```

3. **Date format**: Format ngày hiển thị có thể được điều chỉnh trong phần format date:

```javascript
item.date = date.toLocaleDateString('vi-VN', { 
    day: '2-digit', 
    month: '2-digit', 
    year: 'numeric' 
});
```

## Cách Sử Dụng

### Trong Android App

1. Mở app và đăng nhập với tài khoản admin
2. Vào trang chủ admin
3. Click vào card "Thống kê Báo cáo"
4. Chọn tab muốn xem (Top Doanh Thu, Top Khách Hàng, hoặc Top Sản Phẩm)
5. Nhập số lượng muốn hiển thị (ví dụ: 5, 10, 20)
6. Chọn khoảng thời gian bằng cách click vào "Từ ngày" và "Đến ngày"
7. Click "Áp dụng bộ lọc" để xem kết quả

### API Endpoints

#### Top Revenue
```
GET /api/reports/top-revenue?limit=5&startDate=2024-01-01&endDate=2024-01-31
```

Response:
```json
{
  "status": 200,
  "success": true,
  "message": "Lấy top doanh thu thành công",
  "data": [
    {
      "date": "15/01/2024",
      "total_revenue": 5000000,
      "order_count": 25
    },
    ...
  ]
}
```

#### Top Customers
```
GET /api/reports/top-customers?limit=5&startDate=2024-01-01&endDate=2024-01-31
```

Response:
```json
{
  "status": 200,
  "success": true,
  "message": "Lấy top khách hàng thành công",
  "data": [
    {
      "user_id": "user123",
      "user_name": "Nguyễn Văn A",
      "user_email": "a@example.com",
      "user_phone": "0123456789",
      "total_spent": 3000000,
      "order_count": 10
    },
    ...
  ]
}
```

#### Top Products
```
GET /api/reports/top-products?limit=5&startDate=2024-01-01&endDate=2024-01-31
```

Response:
```json
{
  "status": 200,
  "success": true,
  "message": "Lấy top sản phẩm thành công",
  "data": [
    {
      "product_id": "product123",
      "product_name": "Sản phẩm A",
      "product_image": "uploads/product.jpg",
      "product_price": 100000,
      "total_quantity": 150,
      "total_revenue": 15000000
    },
    ...
  ]
}
```

## Lưu Ý

1. **URL API**: Đảm bảo URL trong `ApiServices.java` đúng với server của bạn:
   ```java
   String Url = "http://192.168.0.114:3000/";
   ```

2. **Date Format**: API nhận date format `yyyy-MM-dd` (ví dụ: `2024-01-15`)

3. **Limit**: Số lượng tối thiểu là 1, không có giới hạn tối đa

4. **Time Range**: Nếu không chọn thời gian, API sẽ lấy tất cả dữ liệu (cần điều chỉnh trong backend nếu muốn giới hạn)

5. **Permissions**: Đảm bảo user có quyền truy cập các endpoint này

## Troubleshooting

### Lỗi: "Lỗi khi tải dữ liệu"
- Kiểm tra kết nối mạng
- Kiểm tra URL API có đúng không
- Kiểm tra server có chạy không
- Kiểm tra logs trên server để xem lỗi cụ thể

### Không có dữ liệu hiển thị
- Kiểm tra có đơn hàng trong khoảng thời gian đã chọn không
- Kiểm tra status của đơn hàng có đúng không (phải là 'đã giao' hoặc 'đã nhận')
- Kiểm tra query trong backend có đúng không

### Lỗi parse date
- Đảm bảo format date đúng `yyyy-MM-dd`
- Kiểm tra timezone trên server

## Tùy Chỉnh

### Thay đổi màu sắc
Các màu sắc có thể được thay đổi trong:
- Layout files: `activity_bao_cao_admin.xml`, `item_*.xml`
- Adapters: Các adapter có thể thay đổi màu highlight

### Thêm filter khác
Có thể thêm các filter khác như:
- Filter theo danh mục sản phẩm
- Filter theo khu vực
- Filter theo phương thức thanh toán

Chỉ cần thêm vào UI và cập nhật query trong backend.

