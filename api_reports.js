// API Routes cho Báo Cáo Thống Kê
// Thêm vào file routes của bạn (ví dụ: routes/reports.js hoặc routes/orders.js)

const express = require('express');
const router = express.Router();
const Order = require('./models/Order'); // Điều chỉnh path theo cấu trúc của bạn
const OrderDetail = require('./models/OrderDetail');
const User = require('./models/User');
const Product = require('./models/Product');

// ==================== TOP REVENUE ====================
// GET /api/reports/top-revenue?limit=5&startDate=2024-01-01&endDate=2024-01-31
router.get('/top-revenue', async (req, res) => {
    try {
        const { limit = 5, startDate, endDate } = req.query;
        
        // Tạo query filter
        const filter = { status: { $in: ['đã giao', 'đã nhận', 'delivered'] } };
        
        if (startDate && endDate) {
            filter.createdAt = {
                $gte: new Date(startDate),
                $lte: new Date(endDate + 'T23:59:59.999Z')
            };
        }
        
        // Lấy tất cả đơn hàng trong khoảng thời gian
        const orders = await Order.find(filter);
        
        // Nhóm theo ngày và tính tổng doanh thu
        const revenueByDate = {};
        orders.forEach(order => {
            const date = new Date(order.createdAt).toISOString().split('T')[0];
            if (!revenueByDate[date]) {
                revenueByDate[date] = {
                    date: date,
                    totalRevenue: 0,
                    orderCount: 0
                };
            }
            revenueByDate[date].totalRevenue += order.totalPrice || 0;
            revenueByDate[date].orderCount += 1;
        });
        
        // Chuyển thành array và sắp xếp theo doanh thu giảm dần
        const topRevenue = Object.values(revenueByDate)
            .sort((a, b) => b.totalRevenue - a.totalRevenue)
            .slice(0, parseInt(limit));
        
        // Format date để hiển thị
        topRevenue.forEach(item => {
            const date = new Date(item.date);
            item.date = date.toLocaleDateString('vi-VN', { 
                day: '2-digit', 
                month: '2-digit', 
                year: 'numeric' 
            });
        });
        
        res.json({
            status: 200,
            success: true,
            message: 'Lấy top doanh thu thành công',
            data: topRevenue
        });
    } catch (error) {
        console.error('Error getting top revenue:', error);
        res.status(500).json({
            status: 500,
            success: false,
            message: 'Lỗi server: ' + error.message,
            data: null
        });
    }
});

// ==================== TOP CUSTOMERS ====================
// GET /api/reports/top-customers?limit=5&startDate=2024-01-01&endDate=2024-01-31
router.get('/top-customers', async (req, res) => {
    try {
        const { limit = 5, startDate, endDate } = req.query;
        
        // Tạo query filter
        const filter = { status: { $in: ['đã giao', 'đã nhận', 'delivered'] } };
        
        if (startDate && endDate) {
            filter.createdAt = {
                $gte: new Date(startDate),
                $lte: new Date(endDate + 'T23:59:59.999Z')
            };
        }
        
        // Lấy tất cả đơn hàng trong khoảng thời gian
        const orders = await Order.find(filter).populate('user_id', 'name email phone');
        
        // Nhóm theo user và tính tổng chi tiêu
        const customerStats = {};
        orders.forEach(order => {
            const userId = order.user_id?._id || order.user_id;
            if (!userId) return;
            
            if (!customerStats[userId]) {
                customerStats[userId] = {
                    user_id: userId,
                    user_name: order.user_id?.name || 'N/A',
                    user_email: order.user_id?.email || 'N/A',
                    user_phone: order.user_id?.phone || 'N/A',
                    totalSpent: 0,
                    orderCount: 0
                };
            }
            customerStats[userId].totalSpent += order.totalPrice || 0;
            customerStats[userId].orderCount += 1;
        });
        
        // Chuyển thành array và sắp xếp theo tổng chi tiêu giảm dần
        const topCustomers = Object.values(customerStats)
            .sort((a, b) => b.totalSpent - a.totalSpent)
            .slice(0, parseInt(limit));
        
        res.json({
            status: 200,
            success: true,
            message: 'Lấy top khách hàng thành công',
            data: topCustomers
        });
    } catch (error) {
        console.error('Error getting top customers:', error);
        res.status(500).json({
            status: 500,
            success: false,
            message: 'Lỗi server: ' + error.message,
            data: null
        });
    }
});

// ==================== TOP PRODUCTS ====================
// GET /api/reports/top-products?limit=5&startDate=2024-01-01&endDate=2024-01-31
router.get('/top-products', async (req, res) => {
    try {
        const { limit = 5, startDate, endDate } = req.query;
        
        // Tạo query filter cho orders
        const orderFilter = { status: { $in: ['đã giao', 'đã nhận', 'delivered'] } };
        
        if (startDate && endDate) {
            orderFilter.createdAt = {
                $gte: new Date(startDate),
                $lte: new Date(endDate + 'T23:59:59.999Z')
            };
        }
        
        // Lấy tất cả đơn hàng trong khoảng thời gian
        const orders = await Order.find(orderFilter);
        const orderIds = orders.map(order => order._id);
        
        // Lấy tất cả order details của các đơn hàng này
        const orderDetails = await OrderDetail.find({ 
            order_id: { $in: orderIds } 
        }).populate('product_id', 'name image price');
        
        // Nhóm theo product và tính tổng số lượng bán
        const productStats = {};
        orderDetails.forEach(detail => {
            const productId = detail.product_id?._id || detail.product_id;
            if (!productId) return;
            
            if (!productStats[productId]) {
                productStats[productId] = {
                    product_id: productId,
                    product_name: detail.product_id?.name || 'N/A',
                    product_image: detail.product_id?.image || '',
                    product_price: detail.product_id?.price || 0,
                    totalQuantity: 0,
                    totalRevenue: 0
                };
            }
            productStats[productId].totalQuantity += detail.quantity || 0;
            productStats[productId].totalRevenue += detail.subtotal || 0;
        });
        
        // Chuyển thành array và sắp xếp theo số lượng bán giảm dần
        const topProducts = Object.values(productStats)
            .sort((a, b) => b.totalQuantity - a.totalQuantity)
            .slice(0, parseInt(limit));
        
        res.json({
            status: 200,
            success: true,
            message: 'Lấy top sản phẩm thành công',
            data: topProducts
        });
    } catch (error) {
        console.error('Error getting top products:', error);
        res.status(500).json({
            status: 500,
            success: false,
            message: 'Lỗi server: ' + error.message,
            data: null
        });
    }
});

module.exports = router;

// ==================== CÁCH SỬ DỤNG ====================
// 1. Tạo file routes/reports.js với code trên
// 2. Trong file server chính (app.js hoặc index.js), thêm:
//    const reportsRouter = require('./routes/reports');
//    app.use('/api/reports', reportsRouter);
//
// 3. Các endpoint sẽ có dạng:
//    GET http://localhost:3000/api/reports/top-revenue?limit=5&startDate=2024-01-01&endDate=2024-01-31
//    GET http://localhost:3000/api/reports/top-customers?limit=5&startDate=2024-01-01&endDate=2024-01-31
//    GET http://localhost:3000/api/reports/top-products?limit=5&startDate=2024-01-01&endDate=2024-01-31

