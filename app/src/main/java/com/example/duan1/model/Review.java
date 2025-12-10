package com.example.duan1.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

@JsonAdapter(ReviewTypeAdapter.class)
public class Review {
    @SerializedName("_id")
    private String id;
    @SerializedName("order_id")
    private String orderId; // Mã đơn hàng (DA1, DA2, ...) hoặc _id của order
    @SerializedName("user_id")
    private String userId;
    private String userName; // Tên user (từ populated data)
    private String userEmail; // Email user (từ populated data)
    private String userPhone; // Phone user (từ populated data)
    private String orderObjectId; // _id của order để query order details
    private int rating;
    private String comment;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;

    public Review() {
    }

    public Review(String id, String orderId, String userId, int rating, String comment) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public String getOrderObjectId() {
        return orderObjectId;
    }

    public void setOrderObjectId(String orderObjectId) {
        this.orderObjectId = orderObjectId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }
}

