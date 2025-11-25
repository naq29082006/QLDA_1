var express = require('express');
var router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { Users, Categories, Products, CartItems, Orders, OrderDetails, Reviews } = require('../models/database'); 

const JWT_SECRET = "NHOM6";
const JWT_EXPIRE = "1h";

// ==================== AUTH APIs ====================
// API register
router.post('/register', async (req, res) => {
  try {
    const { name, email, password, sdt } = req.body;
    console.log("Register body:", req.body);

    // Kiểm tra dữ liệu
    if (!name || !sdt || !email || !password) {
      return res.status(400).json({ success: false, message: "Thiếu dữ liệu khi đăng kí" });
    }

    // Kiểm tra username đã tồn tại chưa
    const existUser = await Users.findOne({ email });
    if (existUser) {
      return res.status(400).json({ success: false, message: "Email đã tồn tại" });
    }

    const newUser = await Users.create({ name, email, password, sdt });

    res.json({
      success: true,
      message: "Đăng ký thành công",
      data: newUser
    });

  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// API login
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ success: false, message: "Thiếu dữ liệu" });

    const user = await Users.findOne({ email });
    if (!user) return res.status(400).json({ success: false, message: "Người dùng không tồn tại" });

    if (password !== user.password) {
        return res.status(400).json({ success: false, message: "Sai mật khẩu" });
    }

    const token = jwt.sign({ id: user._id }, JWT_SECRET, { expiresIn: JWT_EXPIRE });
    const refreshToken = jwt.sign({ id: user._id }, JWT_SECRET, { expiresIn: "7d" });

    res.json({
      success: true,
      message: "Đăng nhập thành công",
      token,
      refreshToken,
      data: {
        id: user._id,
        email: user.email,
        name: user.name,
        phone: user.sdt,
        pass: user.password
      }
    });

  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// ==================== PRODUCT APIs ====================
// Lấy tất cả sản phẩm
router.get('/products', async (req, res) => {
  try {
    const products = await Products.find().populate('category_id', 'name');
    res.json({
      success: true,
      message: "Lấy danh sách sản phẩm thành công",
      data: products
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// Lấy sản phẩm theo ID
router.get('/products/:id', async (req, res) => {
  try {
    const product = await Products.findById(req.params.id).populate('category_id', 'name');
    if (!product) {
      return res.status(404).json({ success: false, message: "Không tìm thấy sản phẩm" });
    }
    res.json({
      success: true,
      message: "Lấy sản phẩm thành công",
      data: product
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// Tìm kiếm sản phẩm
router.get('/products/search/:keyword', async (req, res) => {
  try {
    const keyword = req.params.keyword;
    const products = await Products.find({
      name: { $regex: keyword, $options: 'i' }
    }).populate('category_id', 'name');
    res.json({
      success: true,
      message: "Tìm kiếm sản phẩm thành công",
      data: products
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// ==================== ORDER APIs ====================
// Lấy tất cả đơn hàng
router.get('/orders', async (req, res) => {
  try {
    const orders = await Orders.find().populate('user_id', 'name email').sort({ createdAt: -1 });
    const ordersWithDetails = await Promise.all(orders.map(async (order) => {
      const orderDetails = await OrderDetails.find({ order_id: order._id }).populate('product_id', 'name price');
      return {
        ...order.toObject(),
        order_id: `DH${order._id.toString().substring(0, 8)}`,
        details: orderDetails
      };
    }));
    res.json({
      success: true,
      message: "Lấy danh sách đơn hàng thành công",
      data: ordersWithDetails
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// Lấy đơn hàng theo user_id
router.get('/orders/user/:userId', async (req, res) => {
  try {
    const orders = await Orders.find({ user_id: req.params.userId }).sort({ createdAt: -1 });
    const ordersWithDetails = await Promise.all(orders.map(async (order) => {
      const orderDetails = await OrderDetails.find({ order_id: order._id }).populate('product_id', 'name price');
      return {
        ...order.toObject(),
        order_id: `DH${order._id.toString().substring(0, 8)}`,
        details: orderDetails
      };
    }));
    res.json({
      success: true,
      message: "Lấy danh sách đơn hàng thành công",
      data: ordersWithDetails
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// Lấy đơn hàng chưa giao (status không phải "Đã giao" hoặc "delivered")
router.get('/orders/undelivered', async (req, res) => {
  try {
    const orders = await Orders.find({
      status: { $nin: ['Đã giao', 'delivered', 'Hủy', 'cancelled'] }
    }).populate('user_id', 'name email').sort({ createdAt: -1 }).limit(5);
    
    const ordersWithDetails = await Promise.all(orders.map(async (order) => {
      const orderDetails = await OrderDetails.find({ order_id: order._id }).populate('product_id', 'name price');
      return {
        ...order.toObject(),
        order_id: `DH${order._id.toString().substring(0, 8)}`,
        details: orderDetails
      };
    }));
    
    res.json({
      success: true,
      message: "Lấy danh sách đơn chưa giao thành công",
      data: ordersWithDetails
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// Lấy chi tiết đơn hàng
router.get('/orders/:id', async (req, res) => {
  try {
    const order = await Orders.findById(req.params.id).populate('user_id', 'name email phone');
    if (!order) {
      return res.status(404).json({ success: false, message: "Không tìm thấy đơn hàng" });
    }
    const orderDetails = await OrderDetails.find({ order_id: order._id }).populate('product_id');
    res.json({
      success: true,
      message: "Lấy chi tiết đơn hàng thành công",
      data: {
        ...order.toObject(),
        order_id: `DH${order._id.toString().substring(0, 8)}`,
        details: orderDetails
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// ==================== STATISTICS APIs ====================
// Thống kê tổng sản phẩm
router.get('/statistics/products/total', async (req, res) => {
  try {
    const total = await Products.countDocuments();
    res.json({
      success: true,
      message: "Lấy tổng sản phẩm thành công",
      data: { total }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// Top 5 sản phẩm bán chạy
router.get('/statistics/products/top5', async (req, res) => {
  try {
    // Tính tổng số lượng bán của mỗi sản phẩm từ OrderDetails
    const topProducts = await OrderDetails.aggregate([
      {
        $group: {
          _id: '$product_id',
          totalQuantity: { $sum: '$quantity' }
        }
      },
      { $sort: { totalQuantity: -1 } },
      { $limit: 5 },
      {
        $lookup: {
          from: 'products',
          localField: '_id',
          foreignField: '_id',
          as: 'product'
        }
      },
      { $unwind: '$product' }
    ]);
    
    res.json({
      success: true,
      message: "Lấy top 5 sản phẩm bán chạy thành công",
      data: topProducts.map(item => ({
        id: item.product._id,
        name: item.product.name,
        description: item.product.description,
        price: item.product.price,
        image: item.product.image,
        quantity: item.totalQuantity
      }))
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// Thống kê tỉ lệ đơn hàng
router.get('/statistics/orders/rate', async (req, res) => {
  try {
    const total = await Orders.countDocuments();
    const delivered = await Orders.countDocuments({ status: { $in: ['Đã giao', 'delivered'] } });
    const pending = await Orders.countDocuments({ status: { $in: ['Đang chờ', 'pending'] } });
    const cancelled = await Orders.countDocuments({ status: { $in: ['Hủy', 'cancelled'] } });
    
    res.json({
      success: true,
      message: "Lấy tỉ lệ đơn hàng thành công",
      data: {
        total,
        delivered: total > 0 ? Math.round((delivered / total) * 100) : 0,
        pending: total > 0 ? Math.round((pending / total) * 100) : 0,
        cancelled: total > 0 ? Math.round((cancelled / total) * 100) : 0
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// Doanh thu hôm nay
router.get('/statistics/revenue/today', async (req, res) => {
  try {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    const orders = await Orders.find({
      createdAt: { $gte: today, $lt: tomorrow },
      status: { $in: ['Đã giao', 'delivered'] }
    });
    
    const revenue = orders.reduce((sum, order) => sum + order.total_price, 0);
    
    res.json({
      success: true,
      message: "Lấy doanh thu hôm nay thành công",
      data: { revenue }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

module.exports = router;
