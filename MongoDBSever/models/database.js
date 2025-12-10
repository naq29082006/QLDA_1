const mongoose = require("mongoose");
const Schema = mongoose.Schema;

/* ==========================
   USERS
========================== */
const UserSchema = new Schema({
  name: { type: String, required: true },
  phone: { type: String },
  email: { type: String, required: true, unique: true },
  password: { type: String, required: true },
  role: { type: String, enum: ['admin', 'user'], default: 'user' },
}, { timestamps: true });

const Users = mongoose.model("users", UserSchema);

/* ==========================
   CATEGORIES
========================== */
const CategorySchema = new Schema({
  name: { type: String, required: true },
  description: { type: String }
}, { timestamps: true });

const Categories = mongoose.model("categories", CategorySchema);

/* ==========================
   PRODUCTS
========================== */
const ProductSchema = new Schema({
  name: { type: String, required: true },
  description: { type: String },
  price: { type: Number, required: true },
  image: { type: String },
  category_id: { type: Schema.Types.ObjectId, ref: "categories", required: true }
}, { timestamps: true });

const Products = mongoose.model("products", ProductSchema);

/* ==========================
   CART ITEMS
========================== */
const CartItemSchema = new Schema({
  user_id: { type: Schema.Types.ObjectId, ref: "users", required: true },
  product_id: { type: Schema.Types.ObjectId, ref: "products", required: true },
  quantity: { type: Number, required: true },
  subtotal: { type: Number, required: true },
}, { timestamps: true });

const CartItems = mongoose.model("cart_items", CartItemSchema);

/* ==========================
   ORDERS
========================== */
const OrderSchema = new Schema({
  order_id: { type: String, unique: true },
  user_id: { type: Schema.Types.ObjectId, ref: "users", required: true },
  total_price: { type: Number, required: true },
  status: { type: String, default: "pending" },
  subtotal: { type: Number },
  receiver_name: { type: String, required: true },
  receiver_address: { type: String, required: true },
  receiver_phone: { type: String, required: true },
  voucher_code: { type: String }, // Mã voucher đã áp dụng
  discount_amount: { type: Number, default: 0 }, // Số tiền giảm giá
  voucher_title: { type: String }, // Tiêu đề voucher
  cancel_reason: { type: String }, // Lý do hủy đơn hàng (khi admin hủy)
}, { timestamps: true });

const Orders = mongoose.model("orders", OrderSchema);

/* ==========================
   ORDER DETAILS
========================== */
const OrderDetailSchema = new Schema({
  order_id: { type: Schema.Types.ObjectId, ref: "orders", required: true },
  product_id: { type: Schema.Types.ObjectId, ref: "products", required: true },
  quantity: { type: Number, required: true },
  price: { type: Number, required: true },
  subtotal: { type: Number, required: true }
}, { timestamps: true });

const OrderDetails = mongoose.model("order_details", OrderDetailSchema);

/* ==========================
   REVIEWS
========================== */
const ReviewSchema = new Schema({
  order_id: { type: Schema.Types.ObjectId, ref: "orders", required: true },
  user_id: { type: Schema.Types.ObjectId, ref: "users", required: true },
  rating: { type: Number, min: 1, max: 5, required: true },
  comment: { type: String },
}, { timestamps: true });

const Reviews = mongoose.model("reviews", ReviewSchema);

/* ==========================
   VOUCHERS
========================== */
const VoucherSchema = new Schema({
  voucher_code: { type: String, required: true, unique: true, uppercase: true },
  title: { type: String, required: true }, // Tiêu đề voucher (ví dụ: "Mừng ngày 12/12")
  discount_percentage: { type: Number, required: true, min: 0, max: 100 }, // % giảm giá
  max_discount_amount: { type: Number, default: 0, min: 0 }, // Số tiền giảm tối đa (VND)
  start_date: { type: Date, required: true }, // Ngày bắt đầu
  end_date: { type: Date, required: true }, // Ngày kết thúc
  is_active: { type: Boolean, default: true } // Trạng thái hoạt động
}, { timestamps: true });

const Vouchers = mongoose.model("vouchers", VoucherSchema);

/* ==========================
   EXPORT
========================== */
module.exports = {
  Users,
  Categories,
  Products,
  CartItems,
  Orders,
  OrderDetails,
  Reviews,
  Vouchers
};
