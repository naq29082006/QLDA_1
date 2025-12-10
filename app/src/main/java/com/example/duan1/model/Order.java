package com.example.duan1.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

public class Order {
    @SerializedName("_id")
    private String id;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("user_id")
    @JsonAdapter(UserIdDeserializer.class)
    private String userId;
    private String status;
    @SerializedName("total_price")
    private double totalPrice;
    private double subtotal;
    @SerializedName("receiver_name")
    private String receiverName;
    @SerializedName("receiver_address")
    private String receiverAddress;
    @SerializedName("receiver_phone")
    private String receiverPhone;
    @SerializedName("voucher_code")
    private String voucherCode;
    @SerializedName("discount_amount")
    private double discountAmount;
    @SerializedName("voucher_title")
    private String voucherTitle;
    @SerializedName("cancel_reason")
    private String cancelReason;
    @SerializedName(value = "createdAt", alternate = {"created_at"})
    private String createdAt;
    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    private String updatedAt;

    public Order() {
    }

    public Order(String id, String orderId, String userId, String status, double totalPrice, double subtotal) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.subtotal = subtotal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getVoucherTitle() {
        return voucherTitle;
    }

    public void setVoucherTitle(String voucherTitle) {
        this.voucherTitle = voucherTitle;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public int getStatusColor() {
        if (status == null) return 0xFF757575;
        switch (status.toLowerCase()) {
            case "đã nhận":
            case "đã giao":
            case "delivered":
                return 0xFF4CAF50; // Green
            case "chờ xác nhận":
            case "đang chờ":
            case "pending":
                return 0xFFFF9800; // Orange
            case "chờ lấy hàng":
            case "đang chuẩn bị":
            case "preparing":
                return 0xFF2196F3; // Blue
            case "đang giao":
            case "delivering":
                return 0xFF9C27B0; // Purple
            case "đã hủy":
            case "người dùng hủy":
            case "user đã hủy":
            case "admin đã hủy":
            case "hủy hàng":
            case "cancelled":
                return 0xFFF44336; // Red
            default:
                return 0xFF757575; // Gray
        }
    }

    public int getIconColor() {
        if (status == null) return 0xFF757575;
        switch (status.toLowerCase()) {
            case "đã nhận":
            case "đã giao":
            case "delivered":
                return 0xFF2196F3; // Blue
            case "chờ xác nhận":
            case "đang chờ":
            case "pending":
                return 0xFFFFD700; // Yellow/Gold
            case "chờ lấy hàng":
            case "đang chuẩn bị":
            case "preparing":
                return 0xFF2196F3; // Blue
            case "đang giao":
            case "delivering":
                return 0xFF9C27B0; // Purple
            case "đã hủy":
            case "người dùng hủy":
            case "user đã hủy":
            case "admin đã hủy":
            case "hủy hàng":
            case "cancelled":
                return 0xFFF44336; // Red
            default:
                return 0xFF757575; // Gray
        }
    }

    // Custom deserializer để xử lý user_id có thể là string hoặc object
    public static class UserIdDeserializer implements JsonDeserializer<String> {
        @Override
        public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            if (json.isJsonPrimitive()) {
                // Nếu là string, trả về trực tiếp
                return json.getAsString();
            } else if (json.isJsonObject()) {
                // Nếu là object (do populate), lấy _id
                JsonObject userObj = json.getAsJsonObject();
                if (userObj.has("_id")) {
                    return userObj.get("_id").getAsString();
                }
            }
            return null;
        }
    }
}
