var express = require('express');
var router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const path = require('path');
const { Users, Categories, Products, CartItems, Orders, OrderDetails, Reviews, Vouchers } = require('../models/database');
const transporter = require('../config/common/mail');
const upload = require('../config/common/upload');

const JWT_SECRET = "NHOM6";
const JWT_EXPIRE = "1h";

// Lưu trữ code quên mật khẩu tạm thời (trong production nên dùng Redis)
const resetCodes = {};

// ==================== AUTH APIs ====================

// API Đăng ký
router.post('/register', async (req, res) => {
  try {
    const { name, email, password, phone } = req.body;

    // Kiểm tra dữ liệu đầu vào
    if (!name || !email || !password || !phone) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập đầy đủ thông tin: name, email, password, phone"
      });
    }

    // Kiểm tra email đã tồn tại chưa
    const existUser = await Users.findOne({ email });
    if (existUser) {
      return res.status(400).json({
        success: false,
        message: "Email này đã được sử dụng"
      });
    }

    // Tạo user mới (mặc định role = 'user')
    const newUser = await Users.create({
      name,
      email,
      password,
      phone,
      role: 'user'
    });

    // Trả về kết quả (không trả về password)
    const userResponse = {
      id: newUser._id,
      name: newUser.name,
      email: newUser.email,
      phone: newUser.phone,
      role: newUser.role
    };

    return res.status(201).json({
      success: true,
      message: "Đăng ký thành công",
      data: userResponse
    });

  } catch (err) {
    console.error("Lỗi đăng ký:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// API Đăng nhập
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    // Kiểm tra dữ liệu đầu vào
    if (!email || !password) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập email và mật khẩu"
      });
    }

    // Tìm user theo email
    const user = await Users.findOne({ email });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Email hoặc mật khẩu không đúng"
      });
    }

    // Kiểm tra mật khẩu
    if (password !== user.password) {
      return res.status(400).json({
        success: false,
        message: "Email hoặc mật khẩu không đúng"
      });
    }

    // Tạo token
    const token = jwt.sign({ id: user._id }, JWT_SECRET, { expiresIn: JWT_EXPIRE });
    const refreshToken = jwt.sign({ id: user._id }, JWT_SECRET, { expiresIn: "7d" });

    // Trả về kết quả
    return res.json({
      success: true,
      message: "Đăng nhập thành công",
      token,
      refreshToken,
      data: {
        id: user._id,
        name: user.name,
        email: user.email,
        phone: user.phone,
        role: user.role
      }
    });

  } catch (err) {
    console.error("Lỗi đăng nhập:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// API Quên mật khẩu - Bước 1: Gửi code qua email
router.post('/forgot-password', async (req, res) => {
  try {
    const { email } = req.body;

    // Kiểm tra dữ liệu đầu vào
    if (!email) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập email"
      });
    }

    // Kiểm tra email có tồn tại không
    const user = await Users.findOne({ email });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Email không tồn tại trong hệ thống"
      });
    }

    // Tạo code 6 chữ số ngẫu nhiên
    const code = Math.floor(100000 + Math.random() * 900000).toString();

    // Normalize email để lưu
    const normalizedEmail = email.trim().toLowerCase();

    // Lưu code vào memory (có thể dùng Redis trong production)
    resetCodes[normalizedEmail] = {
      code: code.trim(),
      expiresAt: Date.now() + 10 * 60 * 1000 // 10 phút
    };

    console.log('Code generated for email:', normalizedEmail, 'Code:', code);

    // Gửi email
    const mailOptions = {
      from: 'naq29082006@gmail.com',
      to: email,
      subject: 'Mã xác nhận đặt lại mật khẩu',
      html: `
        <h2>Mã xác nhận đặt lại mật khẩu</h2>
        <p>Xin chào ${user.name},</p>
        <p>Mã xác nhận của bạn là: <strong>${code}</strong></p>
        <p>Mã này có hiệu lực trong 10 phút.</p>
        <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
      `
    };

    await transporter.sendMail(mailOptions);

    return res.json({
      success: true,
      message: "Đã gửi mã xác nhận đến email của bạn",
      data: { email }
    });

  } catch (err) {
    console.error("Lỗi quên mật khẩu:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// API Quên mật khẩu - Bước 2: Xác nhận code (chỉ verify, không đổi password)
router.post('/verify-reset-code', async (req, res) => {
  try {
    const { email, code } = req.body;

    // Kiểm tra dữ liệu đầu vào
    if (!email || !code) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập đầy đủ: email, code"
      });
    }

    // Normalize email và code
    const normalizedEmail = email.trim().toLowerCase();
    const normalizedCode = String(code).trim();

    console.log('Verify code request:', { email: normalizedEmail, code: normalizedCode });
    console.log('Stored codes:', Object.keys(resetCodes));

    // Kiểm tra code có tồn tại không
    const resetData = resetCodes[normalizedEmail];
    if (!resetData) {
      console.log('Code not found for email:', normalizedEmail);
      return res.status(400).json({
        success: false,
        message: "Mã xác nhận không hợp lệ hoặc đã hết hạn"
      });
    }

    console.log('Stored code:', resetData.code, 'Type:', typeof resetData.code);
    console.log('Input code:', normalizedCode, 'Type:', typeof normalizedCode);

    // Kiểm tra code có hết hạn không
    if (Date.now() > resetData.expiresAt) {
      delete resetCodes[normalizedEmail];
      return res.status(400).json({
        success: false,
        message: "Mã xác nhận đã hết hạn, vui lòng yêu cầu lại"
      });
    }

    // Kiểm tra code có đúng không (so sánh string)
    const storedCode = String(resetData.code).trim();
    if (normalizedCode !== storedCode) {
      console.log('Code mismatch:', normalizedCode, 'vs', storedCode);
      return res.status(400).json({
        success: false,
        message: "Mã xác nhận không đúng"
      });
    }

    console.log('Code verified successfully');
    return res.json({
      success: true,
      message: "Mã xác nhận hợp lệ"
    });

  } catch (err) {
    console.error("Lỗi xác nhận code:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// API Quên mật khẩu - Bước 3: Xác nhận code và đặt lại mật khẩu
router.post('/reset-password', async (req, res) => {
  try {
    const { email, code, newPassword } = req.body;

    // Kiểm tra dữ liệu đầu vào
    if (!email || !code || !newPassword) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập đầy đủ: email, code, newPassword"
      });
    }

    // Normalize email và code
    const normalizedEmail = email.trim().toLowerCase();
    const normalizedCode = String(code).trim();

    // Kiểm tra code có tồn tại không
    const resetData = resetCodes[normalizedEmail];
    if (!resetData) {
      return res.status(400).json({
        success: false,
        message: "Mã xác nhận không hợp lệ hoặc đã hết hạn"
      });
    }

    // Kiểm tra code có hết hạn không
    if (Date.now() > resetData.expiresAt) {
      delete resetCodes[normalizedEmail];
      return res.status(400).json({
        success: false,
        message: "Mã xác nhận đã hết hạn, vui lòng yêu cầu lại"
      });
    }

    // Kiểm tra code có đúng không (so sánh string)
    const storedCode = String(resetData.code).trim();
    if (normalizedCode !== storedCode) {
      return res.status(400).json({
        success: false,
        message: "Mã xác nhận không đúng"
      });
    }

    // Tìm user và cập nhật mật khẩu (dùng email gốc để tìm)
    const user = await Users.findOne({ email: normalizedEmail });
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Email không tồn tại"
      });
    }

    user.password = newPassword;
    await user.save();

    // Xóa code đã sử dụng
    delete resetCodes[normalizedEmail];

    return res.json({
      success: true,
      message: "Đặt lại mật khẩu thành công"
    });

  } catch (err) {
    console.error("Lỗi đặt lại mật khẩu:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// ==================== CATEGORIES APIs ====================

// Lấy tất cả danh mục
router.get('/get-all-categories', async (req, res) => {
  try {
    const categories = await Categories.find().sort({ createdAt: -1 });
    return res.json({
      success: true,
      message: "Lấy danh sách danh mục thành công",
      data: categories
    });
  } catch (err) {
    console.error("Lỗi lấy danh mục:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Lấy danh mục theo ID
router.get('/get-id-categories/:id', async (req, res) => {
  try {
    const category = await Categories.findById(req.params.id);
    if (!category) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy danh mục"
      });
    }
    return res.json({
      success: true,
      message: "Lấy danh mục thành công",
      data: category
    });
  } catch (err) {
    console.error("Lỗi lấy danh mục:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Tạo danh mục mới
router.post('/create-categories', async (req, res) => {
  try {
    const { name, description } = req.body;

    if (!name) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập tên danh mục"
      });
    }

    const newCategory = await Categories.create({ name, description });
    return res.status(201).json({
      success: true,
      message: "Tạo danh mục thành công",
      data: newCategory
    });
  } catch (err) {
    console.error("Lỗi tạo danh mục:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Cập nhật danh mục
router.put('/update-id-categories/:id', async (req, res) => {
  try {
    const { name, description } = req.body;
    const category = await Categories.findById(req.params.id);

    if (!category) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy danh mục"
      });
    }

    if (name) category.name = name;
    if (description !== undefined) category.description = description;

    await category.save();

    return res.json({
      success: true,
      message: "Cập nhật danh mục thành công",
      data: category
    });
  } catch (err) {
    console.error("Lỗi cập nhật danh mục:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Xóa danh mục
router.delete('/delete-id-categories/:id', async (req, res) => {
  try {
    const category = await Categories.findById(req.params.id);
    if (!category) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy danh mục"
      });
    }

    // Kiểm tra xem có sản phẩm nào đang dùng danh mục này không
    const productsCount = await Products.countDocuments({ category_id: category._id });
    if (productsCount > 0) {
      return res.status(400).json({
        success: false,
        message: `Không thể xóa danh mục này vì có ${productsCount} sản phẩm đang sử dụng`
      });
    }

    await Categories.findByIdAndDelete(req.params.id);
    return res.json({
      success: true,
      message: "Xóa danh mục thành công"
    });
  } catch (err) {
    console.error("Lỗi xóa danh mục:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// ==================== PRODUCTS APIs ====================

// Lấy tất cả sản phẩm
router.get('/get-all-products', async (req, res) => {
  try {
    const products = await Products.find().populate('category_id', 'name description').sort({ createdAt: -1 });
    return res.json({
      success: true,
      message: "Lấy danh sách sản phẩm thành công",
      data: products
    });
  } catch (err) {
    console.error("Lỗi lấy sản phẩm:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Lấy sản phẩm theo ID
router.get('/get-id-products/:id', async (req, res) => {
  try {
    const product = await Products.findById(req.params.id).populate('category_id', 'name description');
    if (!product) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy sản phẩm"
      });
    }
    return res.json({
      success: true,
      message: "Lấy sản phẩm thành công",
      data: product
    });
  } catch (err) {
    console.error("Lỗi lấy sản phẩm:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Tìm kiếm sản phẩm
router.get('/get-products-search/:keyword', async (req, res) => {
  try {
    const keyword = req.params.keyword;
    const products = await Products.find({
      $or: [
        { name: { $regex: keyword, $options: 'i' } },
        { description: { $regex: keyword, $options: 'i' } }
      ]
    }).populate('category_id', 'name');
    return res.json({
      success: true,
      message: "Tìm kiếm sản phẩm thành công",
      data: products
    });
  } catch (err) {
    console.error("Lỗi tìm kiếm sản phẩm:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Lấy sản phẩm theo danh mục
router.get('/get-products-by-category/:categoryId', async (req, res) => {
  try {
    const categoryId = req.params.categoryId;

    // Kiểm tra category có tồn tại không
    const category = await Categories.findById(categoryId);
    if (!category) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy danh mục"
      });
    }

    // Lấy tất cả sản phẩm theo category_id
    const products = await Products.find({ category_id: categoryId })
      .populate('category_id', 'name description')
      .sort({ createdAt: -1 });

    return res.json({
      success: true,
      message: "Lấy danh sách sản phẩm theo danh mục thành công",
      data: products
    });
  } catch (err) {
    console.error("Lỗi lấy sản phẩm theo danh mục:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Tạo sản phẩm mới (có upload ảnh)
router.post('/create-products', upload.single('image'), async (req, res) => {
  try {
    const { name, description, price, category_id } = req.body;

    // Kiểm tra dữ liệu đầu vào
    if (!name || !price || !category_id) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập đầy đủ: name, price, category_id"
      });
    }

    // Kiểm tra category có tồn tại không
    const category = await Categories.findById(category_id);
    if (!category) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy danh mục"
      });
    }

    // Lấy đường dẫn ảnh nếu có upload
    let imagePath = null;
    if (req.file) {
      imagePath = `/uploads/${req.file.filename}`;
    }

    // Tạo sản phẩm mới
    const newProduct = await Products.create({
      name,
      description: description || '',
      price: parseFloat(price),
      category_id,
      image: imagePath
    });

    const product = await Products.findById(newProduct._id).populate('category_id', 'name description');

    return res.status(201).json({
      success: true,
      message: "Tạo sản phẩm thành công",
      data: product
    });
  } catch (err) {
    console.error("Lỗi tạo sản phẩm:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Cập nhật sản phẩm (có thể upload ảnh mới)
router.put('/update-products/:id', upload.single('image'), async (req, res) => {
  try {
    const { name, description, price, category_id } = req.body;
    const product = await Products.findById(req.params.id);

    if (!product) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy sản phẩm"
      });
    }

    // Cập nhật thông tin
    if (name) product.name = name;
    if (description !== undefined) product.description = description;
    if (price) product.price = parseFloat(price);
    if (category_id) {
      const category = await Categories.findById(category_id);
      if (!category) {
        return res.status(404).json({
          success: false,
          message: "Không tìm thấy danh mục"
        });
      }
      product.category_id = category_id;
    }

    // Cập nhật ảnh nếu có upload mới
    if (req.file) {
      product.image = `/uploads/${req.file.filename}`;
    }

    await product.save();
    const updatedProduct = await Products.findById(product._id).populate('category_id', 'name description');

    return res.json({
      success: true,
      message: "Cập nhật sản phẩm thành công",
      data: updatedProduct
    });
  } catch (err) {
    console.error("Lỗi cập nhật sản phẩm:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Xóa sản phẩm
router.delete('/delete-products/:id', async (req, res) => {
  try {
    const product = await Products.findById(req.params.id);
    if (!product) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy sản phẩm"
      });
    }

    // Kiểm tra xem có đơn hàng nào đang dùng sản phẩm này không
    const orderDetailsCount = await OrderDetails.countDocuments({ product_id: product._id });
    if (orderDetailsCount > 0) {
      return res.status(400).json({
        success: false,
        message: `Không thể xóa sản phẩm này vì có ${orderDetailsCount} đơn hàng đang sử dụng`
      });
    }

    await Products.findByIdAndDelete(req.params.id);
    return res.json({
      success: true,
      message: "Xóa sản phẩm thành công"
    });
  } catch (err) {
    console.error("Lỗi xóa sản phẩm:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});



// ==================== USERS APIs ====================

// Lấy tất cả users
router.get('/get-all-users', async (req, res) => {
  try {
    const users = await Users.find().select('-password').sort({ createdAt: -1 });
    return res.json({
      success: true,
      message: "Lấy danh sách users thành công",
      data: users
    });
  } catch (err) {
    console.error("Lỗi lấy users:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Lấy user theo ID
router.get('/get-id-users/:id', async (req, res) => {
  try {
    const user = await Users.findById(req.params.id).select('-password');
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy user"
      });
    }
    return res.json({
      success: true,
      message: "Lấy user thành công",
      data: user
    });
  } catch (err) {
    console.error("Lỗi lấy user:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Đổi mật khẩu
router.put('/change-password/:id', async (req, res) => {
  try {
    const { oldPassword, newPassword } = req.body;
    const userId = req.params.id;

    // Kiểm tra dữ liệu đầu vào
    if (!oldPassword || !newPassword) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập đầy đủ mật khẩu cũ và mật khẩu mới"
      });
    }

    // Tìm user
    const user = await Users.findById(userId);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy user"
      });
    }

    // Kiểm tra mật khẩu cũ có đúng không
    if (oldPassword !== user.password) {
      return res.status(400).json({
        success: false,
        message: "Mật khẩu cũ không đúng"
      });
    }

    // Kiểm tra mật khẩu mới có khác mật khẩu cũ không
    if (oldPassword === newPassword) {
      return res.status(400).json({
        success: false,
        message: "Mật khẩu mới phải khác mật khẩu cũ"
      });
    }

    // Cập nhật mật khẩu mới
    user.password = newPassword;
    await user.save();

    return res.json({
      success: true,
      message: "Đổi mật khẩu thành công"
    });
  } catch (err) {
    console.error("Lỗi đổi mật khẩu:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Cập nhật user
router.put('/update-users/:id', async (req, res) => {
  try {
    const { name, email, phone, role } = req.body;
    const user = await Users.findById(req.params.id);

    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy user"
      });
    }

    // Kiểm tra email có bị trùng với user khác không (nếu có thay đổi email)
    if (email && email !== user.email) {
      const existUser = await Users.findOne({ email });
      if (existUser) {
        return res.status(400).json({
          success: false,
          message: "Email này đã được sử dụng"
        });
      }
      user.email = email;
    }

    // Cập nhật các trường khác
    if (name) user.name = name;
    if (phone !== undefined) user.phone = phone;
    if (role && (role === 'admin' || role === 'user')) {
      user.role = role;
    }

    await user.save();

    // Trả về user không có password
    const userResponse = {
      id: user._id,
      name: user.name,
      email: user.email,
      phone: user.phone,
      role: user.role
    };

    return res.json({
      success: true,
      message: "Cập nhật user thành công",
      data: userResponse
    });
  } catch (err) {
    console.error("Lỗi cập nhật user:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Xóa user
router.delete('/delete-users/:id', async (req, res) => {
  try {
    const user = await Users.findById(req.params.id);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy user"
      });
    }

    // Kiểm tra xem user có đơn hàng nào không
    const ordersCount = await Orders.countDocuments({ user_id: user._id });
    if (ordersCount > 0) {
      return res.status(400).json({
        success: false,
        message: `Không thể xóa user này vì có ${ordersCount} đơn hàng liên quan`
      });
    }

    // Xóa giỏ hàng của user (nếu có)
    await CartItems.deleteMany({ user_id: user._id });

    // Xóa user
    await Users.findByIdAndDelete(req.params.id);
    return res.json({
      success: true,
      message: "Xóa user thành công"
    });
  } catch (err) {
    console.error("Lỗi xóa user:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// ==================== CART APIs ====================

// Lấy giỏ hàng của user
router.get('/get-cart/:userId', async (req, res) => {
  try {
    const userId = req.params.userId;
    const cartItems = await CartItems.find({ user_id: userId })
      .populate('product_id', 'name description price image category_id')
      .sort({ createdAt: -1 });
    
    return res.json({
      success: true,
      message: "Lấy giỏ hàng thành công",
      data: cartItems
    });
  } catch (err) {
    console.error("Lỗi lấy giỏ hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Thêm sản phẩm vào giỏ hàng
router.post('/add-to-cart', async (req, res) => {
  try {
    const { user_id, product_id, quantity } = req.body;

    // Kiểm tra dữ liệu đầu vào
    if (!user_id || !product_id || !quantity) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập đầy đủ: user_id, product_id, quantity"
      });
    }

    // Kiểm tra user và product có tồn tại không
    const user = await Users.findById(user_id);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy user"
      });
    }

    const product = await Products.findById(product_id);
    if (!product) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy sản phẩm"
      });
    }

    // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
    const existingCartItem = await CartItems.findOne({ 
      user_id: user_id, 
      product_id: product_id 
    });

    if (existingCartItem) {
      // Cập nhật số lượng và subtotal
      existingCartItem.quantity += parseInt(quantity);
      existingCartItem.subtotal = existingCartItem.quantity * product.price;
      await existingCartItem.save();
      
      const updatedCartItem = await CartItems.findById(existingCartItem._id)
        .populate('product_id', 'name description price image category_id');
      
      return res.json({
        success: true,
        message: "Cập nhật giỏ hàng thành công",
        data: updatedCartItem
      });
    } else {
      // Tạo cart item mới
      const subtotal = parseInt(quantity) * product.price;
      const newCartItem = await CartItems.create({
        user_id,
        product_id,
        quantity: parseInt(quantity),
        subtotal
      });

      const cartItem = await CartItems.findById(newCartItem._id)
        .populate('product_id', 'name description price image category_id');

      return res.status(201).json({
        success: true,
        message: "Thêm vào giỏ hàng thành công",
        data: cartItem
      });
    }
  } catch (err) {
    console.error("Lỗi thêm vào giỏ hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Cập nhật số lượng trong giỏ hàng
router.put('/update-cart/:id', async (req, res) => {
  try {
    const { quantity } = req.body;
    const cartItem = await CartItems.findById(req.params.id)
      .populate('product_id', 'price');

    if (!cartItem) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy sản phẩm trong giỏ hàng"
      });
    }

    if (!quantity || quantity < 1) {
      return res.status(400).json({
        success: false,
        message: "Số lượng phải lớn hơn 0"
      });
    }

    cartItem.quantity = parseInt(quantity);
    cartItem.subtotal = cartItem.quantity * cartItem.product_id.price;
    await cartItem.save();

    const updatedCartItem = await CartItems.findById(cartItem._id)
      .populate('product_id', 'name description price image category_id');

    return res.json({
      success: true,
      message: "Cập nhật giỏ hàng thành công",
      data: updatedCartItem
    });
  } catch (err) {
    console.error("Lỗi cập nhật giỏ hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Xóa sản phẩm khỏi giỏ hàng
router.delete('/delete-cart/:id', async (req, res) => {
  try {
    const cartItem = await CartItems.findById(req.params.id);
    if (!cartItem) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy sản phẩm trong giỏ hàng"
      });
    }

    await CartItems.findByIdAndDelete(req.params.id);
    return res.json({
      success: true,
      message: "Xóa sản phẩm khỏi giỏ hàng thành công"
    });
  } catch (err) {
    console.error("Lỗi xóa giỏ hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// ==================== ORDERS APIs ====================

// Tạo đơn hàng mới
router.post('/create-order', async (req, res) => {
  try {
    const { user_id, cart_item_ids, receiver_name, receiver_address, receiver_phone } = req.body;

    // Kiểm tra dữ liệu đầu vào
    if (!user_id || !cart_item_ids || !Array.isArray(cart_item_ids) || cart_item_ids.length === 0) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng chọn ít nhất một sản phẩm để đặt hàng"
      });
    }

    if (!receiver_name || !receiver_address || !receiver_phone) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập đầy đủ thông tin người nhận"
      });
    }

    // Validate số điện thoại (phải có 10 chữ số)
    const phoneRegex = /^[0-9]{10}$/;
    if (!phoneRegex.test(receiver_phone)) {
      return res.status(400).json({
        success: false,
        message: "Số điện thoại phải có đúng 10 chữ số"
      });
    }

    // Validate địa chỉ (ít nhất 10 ký tự)
    if (receiver_address.length < 10) {
      return res.status(400).json({
        success: false,
        message: "Địa chỉ phải có ít nhất 10 ký tự"
      });
    }

    // Validate tên (ít nhất 2 ký tự)
    if (receiver_name.length < 2) {
      return res.status(400).json({
        success: false,
        message: "Tên người nhận phải có ít nhất 2 ký tự"
      });
    }

    // Kiểm tra user có tồn tại không
    const user = await Users.findById(user_id);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy user"
      });
    }

    // Lấy các cart items được chọn
    const cartItems = await CartItems.find({ 
      _id: { $in: cart_item_ids },
      user_id: user_id 
    }).populate('product_id', 'price');

    if (cartItems.length === 0) {
      return res.status(400).json({
        success: false,
        message: "Không tìm thấy sản phẩm trong giỏ hàng"
      });
    }

    // Tính tổng tiền
    let totalPrice = 0;
    let subtotal = 0;
    cartItems.forEach(item => {
      subtotal += item.subtotal;
    });
    totalPrice = subtotal;

    // Tự động tìm và áp dụng TẤT CẢ voucher đang hoạt động
    let discountAmount = 0;
    let totalDiscountPercentage = 0;
    let voucherCodes = [];
    let voucherTitles = [];
    const now = new Date();
    
    // Tìm TẤT CẢ voucher đang active và trong khoảng thời gian hiện tại
    // Voucher phải: is_active = true, start_date <= now, end_date >= now
    const activeVouchers = await Vouchers.find({
      is_active: true,
      start_date: { $lte: now },
      end_date: { $gte: now }
    }).sort({ createdAt: -1 });
    
    if (activeVouchers && activeVouchers.length > 0) {
      // Áp dụng TẤT CẢ voucher - mỗi voucher được tính riêng với max discount của nó
      let totalDiscount = 0;
      for (const voucher of activeVouchers) {
        // Tính discount cho voucher này
        let voucherDiscount = (subtotal * voucher.discount_percentage) / 100;
        
        // Áp dụng max discount nếu có
        if (voucher.max_discount_amount > 0 && voucherDiscount > voucher.max_discount_amount) {
          voucherDiscount = voucher.max_discount_amount;
        }
        
        totalDiscount += voucherDiscount;
        voucherCodes.push(voucher.voucher_code);
        voucherTitles.push(voucher.title);
      }
      
      // Đảm bảo discount không vượt quá subtotal
      discountAmount = Math.min(totalDiscount, subtotal);
      totalPrice = Math.max(0, subtotal - discountAmount);
      
      console.log(`Áp dụng ${activeVouchers.length} voucher: ${voucherCodes.join(', ')}, Tổng giảm: ${discountAmount}, Tổng tiền sau giảm: ${totalPrice}`);
    } else {
      console.log("Không có voucher đang active");
    }
    
    // Lưu thông tin voucher (lưu mã và title của voucher đầu tiên, hoặc tổng hợp)
    const voucherCode = voucherCodes.length > 0 ? voucherCodes.join(', ') : null;
    const voucherTitle = voucherTitles.length > 0 ? voucherTitles.join(' + ') : null;

    // Tạo mã đơn hàng tự động (DA1, DA2, ...)
    const lastOrder = await Orders.findOne().sort({ createdAt: -1 });
    let orderNumber = 1;
    if (lastOrder && lastOrder.order_id) {
      // Lấy số từ mã đơn hàng cuối cùng (ví dụ: DA123 -> 123)
      const match = lastOrder.order_id.match(/DA(\d+)/);
      if (match) {
        orderNumber = parseInt(match[1]) + 1;
      }
    }
    const orderId = `DA${orderNumber}`;

    // Tạo đơn hàng
    const newOrder = await Orders.create({
      user_id,
      order_id: orderId,
      total_price: totalPrice,
      subtotal: subtotal,
      status: "chờ xác nhận",
      receiver_name,
      receiver_address,
      receiver_phone,
      voucher_code: voucherCode,
      discount_amount: discountAmount,
      voucher_title: voucherTitle
    });

    // Tạo order details
    const orderDetails = [];
    for (const cartItem of cartItems) {
      const orderDetail = await OrderDetails.create({
        order_id: newOrder._id,
        product_id: cartItem.product_id._id,
        quantity: cartItem.quantity,
        price: cartItem.product_id.price,
        subtotal: cartItem.subtotal
      });
      orderDetails.push(orderDetail);
    }

    // Xóa các cart items đã đặt hàng
    await CartItems.deleteMany({ _id: { $in: cart_item_ids } });

    // Lấy đơn hàng với thông tin đầy đủ
    const order = await Orders.findById(newOrder._id)
      .populate('user_id', 'name email phone');

    return res.status(201).json({
      success: true,
      message: "Đặt hàng thành công",
      data: order
    });
  } catch (err) {
    console.error("Lỗi tạo đơn hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Lấy đơn hàng của user
router.get('/get-orders/:userId', async (req, res) => {
  try {
    const userId = req.params.userId;
    const orders = await Orders.find({ user_id: userId })
      .sort({ createdAt: -1 });
    
    return res.json({
      success: true,
      message: "Lấy danh sách đơn hàng thành công",
      data: orders
    });
  } catch (err) {
    console.error("Lỗi lấy đơn hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Lấy tất cả đơn hàng (cho admin)
router.get('/get-all-orders', async (req, res) => {
  try {
    const orders = await Orders.find()
      .sort({ createdAt: -1 });
    
    return res.json({
      success: true,
      message: "Lấy danh sách đơn hàng thành công",
      data: orders
    });
  } catch (err) {
    console.error("Lỗi lấy đơn hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Cập nhật trạng thái đơn hàng
router.put('/update-order-status/:id', async (req, res) => {
  try {
    const { status, cancel_reason } = req.body;
    const orderId = req.params.id;

    if (!status) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng cung cấp trạng thái mới"
      });
    }

    // Nếu hủy đơn hàng (admin hủy), yêu cầu lý do
    const statusLower = status.toLowerCase();
    if ((statusLower.includes('hủy') || statusLower.includes('cancel')) && 
        statusLower.includes('admin')) {
      if (!cancel_reason || cancel_reason.trim().length < 5) {
        return res.status(400).json({
          success: false,
          message: "Vui lòng nhập lý do hủy đơn hàng (ít nhất 5 ký tự)"
        });
      }
    }

    const updateData = { status: status };
    if (cancel_reason) {
      updateData.cancel_reason = cancel_reason.trim();
    }

    const order = await Orders.findByIdAndUpdate(
      orderId,
      updateData,
      { new: true }
    );

    if (!order) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy đơn hàng"
      });
    }

    return res.json({
      success: true,
      message: "Cập nhật trạng thái đơn hàng thành công",
      data: order
    });
  } catch (err) {
    console.error("Lỗi cập nhật trạng thái đơn hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Lấy chi tiết đơn hàng (order details)
// Lấy đơn hàng theo ID
router.get('/get-order-by-id/:id', async (req, res) => {
  try {
    const orderId = req.params.id;
    const order = await Orders.findById(orderId)
      .populate('user_id', 'name email phone');
    
    if (!order) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy đơn hàng"
      });
    }
    
    return res.json({
      success: true,
      message: "Lấy đơn hàng thành công",
      data: order
    });
  } catch (err) {
    console.error("Lỗi lấy đơn hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

router.get('/get-order-details/:orderId', async (req, res) => {
  try {
    const orderId = req.params.orderId;
    
    // Kiểm tra order có tồn tại không
    const order = await Orders.findById(orderId);
    if (!order) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy đơn hàng"
      });
    }

    // Lấy order details với thông tin sản phẩm
    const orderDetails = await OrderDetails.find({ order_id: orderId })
      .populate('product_id', 'name description price image category_id')
      .sort({ createdAt: -1 });
    
    return res.json({
      success: true,
      message: "Lấy chi tiết đơn hàng thành công",
      data: orderDetails
    });
  } catch (err) {
    console.error("Lỗi lấy chi tiết đơn hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// User hủy đơn hàng (chỉ khi chưa được xác nhận)
router.put('/cancel-order/:id', async (req, res) => {
  try {
    const orderId = req.params.id;
    const { user_id } = req.body;

    if (!user_id) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng cung cấp user_id"
      });
    }

    const order = await Orders.findById(orderId);
    if (!order) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy đơn hàng"
      });
    }

    // Kiểm tra user có phải chủ đơn hàng không
    if (order.user_id.toString() !== user_id) {
      return res.status(403).json({
        success: false,
        message: "Bạn không có quyền hủy đơn hàng này"
      });
    }

    // Chỉ cho phép hủy khi đơn hàng chưa được xác nhận
    const status = order.status ? order.status.toLowerCase() : '';
    if (!status.includes('chờ xác nhận') && !status.includes('pending') && !status.includes('đang chờ')) {
      return res.status(400).json({
        success: false,
        message: "Chỉ có thể hủy đơn hàng khi chưa được xác nhận"
      });
    }

    // Cập nhật trạng thái thành "user đã hủy"
    order.status = "user đã hủy";
    await order.save();

    return res.json({
      success: true,
      message: "Hủy đơn hàng thành công",
      data: order
    });
  } catch (err) {
    console.error("Lỗi hủy đơn hàng:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// ==================== REVIEWS APIs ====================

// Tạo đánh giá mới
router.post('/create-review', async (req, res) => {
  try {
    const { order_id, user_id, rating, comment } = req.body;

    // Kiểm tra dữ liệu đầu vào
    if (!order_id || !user_id || !rating) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập đầy đủ: order_id, user_id, rating"
      });
    }

    // Kiểm tra rating hợp lệ (1-5)
    if (rating < 1 || rating > 5) {
      return res.status(400).json({
        success: false,
        message: "Rating phải từ 1 đến 5"
      });
    }

    // Kiểm tra order có tồn tại không
    const order = await Orders.findById(order_id);
    if (!order) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy đơn hàng"
      });
    }

    // Kiểm tra user có tồn tại không
    const user = await Users.findById(user_id);
    if (!user) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy user"
      });
    }

    // Kiểm tra xem đơn hàng đã có đánh giá chưa
    const existingReview = await Reviews.findOne({ order_id: order_id });
    if (existingReview) {
      return res.status(400).json({
        success: false,
        message: "Đơn hàng này đã được đánh giá"
      });
    }

    // Tạo review mới
    const newReview = await Reviews.create({
      order_id,
      user_id,
      rating: parseInt(rating),
      comment: comment || ''
    });

    const review = await Reviews.findById(newReview._id)
      .populate('order_id', 'order_id status')
      .populate('user_id', 'name email');

    return res.status(201).json({
      success: true,
      message: "Đánh giá thành công",
      data: review
    });
  } catch (err) {
    console.error("Lỗi tạo đánh giá:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// Lấy đánh giá theo order_id
// Lấy tất cả đánh giá (cho admin)
router.get('/get-all-reviews', async (req, res) => {
  try {
    const reviews = await Reviews.find()
      .populate('order_id', 'order_id status total_price receiver_name receiver_address receiver_phone _id')
      .populate('user_id', 'name email phone _id')
      .sort({ createdAt: -1 });
    
    return res.json({
      success: true,
      message: "Lấy danh sách đánh giá thành công",
      data: reviews
    });
  } catch (err) {
    console.error("Lỗi lấy danh sách đánh giá:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

router.get('/get-review-by-order/:orderId', async (req, res) => {
  try {
    const orderId = req.params.orderId;
    const review = await Reviews.findOne({ order_id: orderId })
      .populate('order_id', 'order_id status')
      .populate('user_id', 'name email');
    
    if (!review) {
      return res.json({
        success: true,
        message: "Chưa có đánh giá cho đơn hàng này",
        data: null
      });
    }

    return res.json({
      success: true,
      message: "Lấy đánh giá thành công",
      data: review
    });
  } catch (err) {
    console.error("Lỗi lấy đánh giá:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// ==================== VOUCHER APIs ====================

// ==================== VOUCHER APIs ====================

// API Get All Vouchers
router.get('/get-all-vouchers', async (req, res) => {
  try {
    const vouchers = await Vouchers.find().sort({ createdAt: -1 });
    return res.status(200).json({
      success: true,
      message: "Lấy danh sách voucher thành công",
      data: vouchers
    });
  } catch (err) {
    console.error("Lỗi get all vouchers:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// API Get Voucher By ID
router.get('/get-voucher-by-id/:id', async (req, res) => {
  try {
    const voucher = await Vouchers.findById(req.params.id);
    if (!voucher) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy voucher"
      });
    }
    return res.status(200).json({
      success: true,
      message: "Lấy voucher thành công",
      data: voucher
    });
  } catch (err) {
    console.error("Lỗi get voucher by id:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// API Create Voucher
router.post('/create-voucher', async (req, res) => {
  try {
    const { voucher_code, title, discount_percentage, max_discount_amount, start_date, end_date, is_active } = req.body;

    if (!voucher_code || !title || discount_percentage === undefined || max_discount_amount === undefined || !start_date || !end_date) {
      return res.status(400).json({
        success: false,
        message: "Vui lòng nhập đầy đủ thông tin"
      });
    }

    // Kiểm tra voucher code đã tồn tại chưa
    const existVoucher = await Vouchers.findOne({ voucher_code: voucher_code.toUpperCase() });
    if (existVoucher) {
      return res.status(400).json({
        success: false,
        message: "Mã voucher đã tồn tại"
      });
    }

    // Parse dates - format: yyyy-MM-dd
    let parsedStartDate, parsedEndDate;
    try {
      // Nếu date là string format yyyy-MM-dd, parse thành Date
      if (typeof start_date === 'string') {
        const startParts = start_date.split('-');
        if (startParts.length === 3) {
          parsedStartDate = new Date(parseInt(startParts[0]), parseInt(startParts[1]) - 1, parseInt(startParts[2]));
        } else {
          parsedStartDate = new Date(start_date);
        }
      } else {
        parsedStartDate = new Date(start_date);
      }

      if (typeof end_date === 'string') {
        const endParts = end_date.split('-');
        if (endParts.length === 3) {
          parsedEndDate = new Date(parseInt(endParts[0]), parseInt(endParts[1]) - 1, parseInt(endParts[2]), 23, 59, 59);
        } else {
          parsedEndDate = new Date(end_date);
        }
      } else {
        parsedEndDate = new Date(end_date);
      }

      // Validate dates
      if (isNaN(parsedStartDate.getTime()) || isNaN(parsedEndDate.getTime())) {
        return res.status(400).json({
          success: false,
          message: "Ngày không hợp lệ. Vui lòng nhập đúng format yyyy-MM-dd"
        });
      }

      if (parsedStartDate > parsedEndDate) {
        return res.status(400).json({
          success: false,
          message: "Ngày bắt đầu phải nhỏ hơn ngày kết thúc"
        });
      }
    } catch (dateError) {
      return res.status(400).json({
        success: false,
        message: "Lỗi parse ngày: " + dateError.message
      });
    }

    const newVoucher = await Vouchers.create({
      voucher_code: voucher_code.toUpperCase(),
      title,
      discount_percentage: parseFloat(discount_percentage),
      max_discount_amount: parseFloat(max_discount_amount) || 0,
      start_date: parsedStartDate,
      end_date: parsedEndDate,
      is_active: is_active !== undefined ? is_active : true
    });

    return res.status(201).json({
      success: true,
      message: "Tạo voucher thành công",
      data: newVoucher
    });
  } catch (err) {
    console.error("Lỗi create voucher:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// API Update Voucher
router.put('/update-voucher/:id', async (req, res) => {
  try {
    const { voucher_code, title, discount_percentage, max_discount_amount, start_date, end_date, is_active } = req.body;

    const voucher = await Vouchers.findById(req.params.id);
    if (!voucher) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy voucher"
      });
    }

    // Kiểm tra voucher code đã tồn tại chưa (nếu thay đổi)
    if (voucher_code && voucher_code.toUpperCase() !== voucher.voucher_code) {
      const existVoucher = await Vouchers.findOne({ voucher_code: voucher_code.toUpperCase() });
      if (existVoucher) {
        return res.status(400).json({
          success: false,
          message: "Mã voucher đã tồn tại"
        });
      }
    }

    // Cập nhật thông tin
    if (voucher_code) voucher.voucher_code = voucher_code.toUpperCase();
    if (title) voucher.title = title;
    if (discount_percentage !== undefined) voucher.discount_percentage = parseFloat(discount_percentage);
    if (max_discount_amount !== undefined) voucher.max_discount_amount = parseFloat(max_discount_amount) || 0;
    
    // Parse dates nếu có
    if (start_date) {
      try {
        if (typeof start_date === 'string') {
          const startParts = start_date.split('-');
          if (startParts.length === 3) {
            voucher.start_date = new Date(parseInt(startParts[0]), parseInt(startParts[1]) - 1, parseInt(startParts[2]));
          } else {
            voucher.start_date = new Date(start_date);
          }
        } else {
          voucher.start_date = new Date(start_date);
        }
        if (isNaN(voucher.start_date.getTime())) {
          return res.status(400).json({
            success: false,
            message: "Ngày bắt đầu không hợp lệ"
          });
        }
      } catch (e) {
        return res.status(400).json({
          success: false,
          message: "Lỗi parse ngày bắt đầu: " + e.message
        });
      }
    }
    
    if (end_date) {
      try {
        if (typeof end_date === 'string') {
          const endParts = end_date.split('-');
          if (endParts.length === 3) {
            voucher.end_date = new Date(parseInt(endParts[0]), parseInt(endParts[1]) - 1, parseInt(endParts[2]), 23, 59, 59);
          } else {
            voucher.end_date = new Date(end_date);
          }
        } else {
          voucher.end_date = new Date(end_date);
        }
        if (isNaN(voucher.end_date.getTime())) {
          return res.status(400).json({
            success: false,
            message: "Ngày kết thúc không hợp lệ"
          });
        }
      } catch (e) {
        return res.status(400).json({
          success: false,
          message: "Lỗi parse ngày kết thúc: " + e.message
        });
      }
    }
    
    // Validate: start_date phải <= end_date
    if (voucher.start_date && voucher.end_date && voucher.start_date > voucher.end_date) {
      return res.status(400).json({
        success: false,
        message: "Ngày bắt đầu phải nhỏ hơn ngày kết thúc"
      });
    }
    
    if (is_active !== undefined) voucher.is_active = is_active;

    await voucher.save();

    return res.status(200).json({
      success: true,
      message: "Cập nhật voucher thành công",
      data: voucher
    });
  } catch (err) {
    console.error("Lỗi update voucher:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// API Delete Voucher
router.delete('/delete-voucher/:id', async (req, res) => {
  try {
    const voucher = await Vouchers.findByIdAndDelete(req.params.id);
    if (!voucher) {
      return res.status(404).json({
        success: false,
        message: "Không tìm thấy voucher"
      });
    }
    return res.status(200).json({
      success: true,
      message: "Xóa voucher thành công"
    });
  } catch (err) {
    console.error("Lỗi delete voucher:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// API Validate Voucher
router.post('/validate-voucher', async (req, res) => {
  try {
    const { total_amount } = req.body;

    const now = new Date();
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const todayEnd = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
    
    // Tìm voucher đang active và trong khoảng thời gian hiện tại
    const voucher = await Vouchers.findOne({
      is_active: true,
      start_date: { $lte: todayEnd },
      end_date: { $gte: todayStart }
    }).sort({ createdAt: -1 }); // Lấy voucher mới nhất nếu có nhiều voucher

    if (!voucher) {
      return res.status(200).json({
        success: true,
        message: "Không có voucher đang active",
        data: {
          voucher: null
        }
      });
    }

    // Tính toán giảm giá (%)
    const orderAmount = total_amount || 0;
    let discountAmount = (orderAmount * voucher.discount_percentage) / 100;
    
    // Áp dụng max discount nếu có
    if (voucher.max_discount_amount > 0 && discountAmount > voucher.max_discount_amount) {
      discountAmount = voucher.max_discount_amount;
    }
    
    const finalAmount = Math.max(0, orderAmount - discountAmount);

    return res.status(200).json({
      success: true,
      message: "Voucher hợp lệ",
      data: {
        voucher: {
          id: voucher._id,
          code: voucher.voucher_code,
          title: voucher.title,
          discount_percentage: voucher.discount_percentage,
          discount_amount: discountAmount,
          final_amount: finalAmount
        }
      }
    });

  } catch (err) {
    console.error("Lỗi validate voucher:", err);
    return res.status(500).json({
      success: false,
      message: "Lỗi server: " + err.message
    });
  }
});

// ==================== REPORTS APIs ====================

// GET /api/reports/top-revenue?limit=5&startDate=2024-01-01&endDate=2024-12-31
router.get('/reports/top-revenue', async (req, res) => {
  try {
    const { limit = 5, startDate, endDate } = req.query;
    
    // Tạo query filter - chỉ lấy đơn hàng đã giao
    const filter = { status: { $in: ['đã giao', 'đã nhận', 'delivered'] } };
    
    if (startDate && endDate) {
      filter.createdAt = {
        $gte: new Date(startDate),
        $lte: new Date(endDate + 'T23:59:59.999Z')
      };
    }
    
    // Lấy tất cả đơn hàng trong khoảng thời gian
    const orders = await Orders.find(filter);
    
    // Nhóm theo ngày và tính tổng doanh thu
    const revenueByDate = {};
    let totalRevenueAll = 0;
    let totalOrderCountAll = 0;
    
    orders.forEach(order => {
      const date = new Date(order.createdAt).toISOString().split('T')[0];
      if (!revenueByDate[date]) {
        revenueByDate[date] = {
          date: date,
          total_revenue: 0,
          order_count: 0
        };
      }
      revenueByDate[date].total_revenue += order.total_price || 0;
      revenueByDate[date].order_count += 1;
      
      // Tính tổng cho toàn bộ khoảng thời gian
      totalRevenueAll += order.total_price || 0;
      totalOrderCountAll += 1;
    });
    
    // Chuyển thành array và sắp xếp theo doanh thu giảm dần
    const topRevenue = Object.values(revenueByDate)
      .sort((a, b) => b.total_revenue - a.total_revenue)
      .slice(0, parseInt(limit));
    
    // Thêm item tổng vào đầu danh sách nếu có dữ liệu
    if (topRevenue.length > 0 && startDate && endDate) {
      const totalItem = {
        date: `Tổng (${startDate} - ${endDate})`,
        total_revenue: totalRevenueAll,
        order_count: totalOrderCountAll
      };
      topRevenue.unshift(totalItem); // Thêm vào đầu
    }
    
    // Format date để hiển thị
    topRevenue.forEach(item => {
      const date = new Date(item.date);
      item.date = date.toLocaleDateString('vi-VN', { 
        day: '2-digit', 
        month: '2-digit', 
        year: 'numeric' 
      });
    });
    
    return res.json({
      status: 200,
      success: true,
      message: 'Lấy top doanh thu thành công',
      data: topRevenue
    });
  } catch (err) {
    console.error('Lỗi lấy top revenue:', err);
    return res.status(500).json({
      status: 500,
      success: false,
      message: 'Lỗi server: ' + err.message,
      data: null
    });
  }
});

// GET /api/reports/top-customers?limit=5&startDate=2024-01-01&endDate=2024-12-31
router.get('/reports/top-customers', async (req, res) => {
  try {
    const { limit = 5, startDate, endDate } = req.query;
    
    // Tạo query filter - chỉ lấy đơn hàng đã giao
    const filter = { status: { $in: ['đã giao', 'đã nhận', 'delivered'] } };
    
    if (startDate && endDate) {
      filter.createdAt = {
        $gte: new Date(startDate),
        $lte: new Date(endDate + 'T23:59:59.999Z')
      };
    }
    
    // Lấy tất cả đơn hàng trong khoảng thời gian và populate user
    const orders = await Orders.find(filter).populate('user_id', 'name email phone');
    
    // Nhóm theo user và tính tổng chi tiêu
    const customerStats = {};
    orders.forEach(order => {
      const userId = order.user_id?._id || order.user_id;
      if (!userId) return;
      
      const userIdStr = userId.toString();
      
      if (!customerStats[userIdStr]) {
        customerStats[userIdStr] = {
          user_id: userIdStr,
          user_name: order.user_id?.name || 'N/A',
          user_email: order.user_id?.email || 'N/A',
          user_phone: order.user_id?.phone || 'N/A',
          total_spent: 0,
          order_count: 0
        };
      }
      customerStats[userIdStr].total_spent += order.total_price || 0;
      customerStats[userIdStr].order_count += 1;
    });
    
    // Chuyển thành array và sắp xếp theo tổng chi tiêu giảm dần
    const topCustomers = Object.values(customerStats)
      .sort((a, b) => b.total_spent - a.total_spent)
      .slice(0, parseInt(limit));
    
    return res.json({
      status: 200,
      success: true,
      message: 'Lấy top khách hàng thành công',
      data: topCustomers
    });
  } catch (err) {
    console.error('Lỗi lấy top customers:', err);
    return res.status(500).json({
      status: 500,
      success: false,
      message: 'Lỗi server: ' + err.message,
      data: null
    });
  }
});

// GET /api/reports/top-products?limit=5&startDate=2024-01-01&endDate=2024-12-31
router.get('/reports/top-products', async (req, res) => {
  try {
    const { limit = 5, startDate, endDate } = req.query;
    
    // Tạo query filter cho orders - chỉ lấy đơn hàng đã giao
    const orderFilter = { status: { $in: ['đã giao', 'đã nhận', 'delivered'] } };
    
    if (startDate && endDate) {
      orderFilter.createdAt = {
        $gte: new Date(startDate),
        $lte: new Date(endDate + 'T23:59:59.999Z')
      };
    }
    
    // Lấy tất cả đơn hàng trong khoảng thời gian
    const orders = await Orders.find(orderFilter);
    const orderIds = orders.map(order => order._id);
    
    // Lấy tất cả order details của các đơn hàng này
    const orderDetails = await OrderDetails.find({ 
      order_id: { $in: orderIds } 
    }).populate('product_id', 'name image price');
    
    // Nhóm theo product và tính tổng số lượng bán
    const productStats = {};
    orderDetails.forEach(detail => {
      const productId = detail.product_id?._id || detail.product_id;
      if (!productId) return;
      
      const productIdStr = productId.toString();
      
      if (!productStats[productIdStr]) {
        productStats[productIdStr] = {
          product_id: productIdStr,
          product_name: detail.product_id?.name || 'N/A',
          product_image: detail.product_id?.image || '',
          product_price: detail.product_id?.price || 0,
          total_quantity: 0,
          total_revenue: 0
        };
      }
      productStats[productIdStr].total_quantity += detail.quantity || 0;
      productStats[productIdStr].total_revenue += detail.subtotal || 0;
    });
    
    // Chuyển thành array và sắp xếp theo số lượng bán giảm dần
    const topProducts = Object.values(productStats)
      .sort((a, b) => b.total_quantity - a.total_quantity)
      .slice(0, parseInt(limit));
    
    return res.json({
      status: 200,
      success: true,
      message: 'Lấy top sản phẩm thành công',
      data: topProducts
    });
  } catch (err) {
    console.error('Lỗi lấy top products:', err);
    return res.status(500).json({
      status: 500,
      success: false,
      message: 'Lỗi server: ' + err.message,
      data: null
    });
  }
});

module.exports = router;
