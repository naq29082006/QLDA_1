package com.example.duan1.model;

import com.google.gson.annotations.SerializedName;

public class Order {
    @SerializedName("_id")
    private String id;
    @SerializedName("order_id")
    private String orderId;
    private String status;
    @SerializedName("total_price")
    private double totalPrice;
    private String receiverName;
    private String receiverAddress;
    private String receiverPhone;
    private String createdAt;

    public Order() {
    }

    public Order(String id, String orderId, String status, double totalPrice) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.totalPrice = totalPrice;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getStatusColor() {
        switch (status.toLowerCase()) {
            case "đã giao":
            case "delivered":
                return 0xFF4CAF50; // Green
            case "đang chờ":
            case "pending":
                return 0xFFFF9800; // Orange
            case "đang chuẩn bị":
            case "preparing":
                return 0xFF2196F3; // Blue
            case "đang giao":
            case "delivering":
                return 0xFF9C27B0; // Purple
            default:
                return 0xFF757575; // Gray
        }
    }

    public int getIconColor() {
        switch (status.toLowerCase()) {
            case "đã giao":
            case "delivered":
                return 0xFF2196F3; // Blue
            case "đang chờ":
            case "pending":
                return 0xFFFFD700; // Yellow/Gold
            default:
                return 0xFF757575; // Gray
        }
    }
}

