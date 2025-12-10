package com.example.duan1.model;

import com.google.gson.annotations.SerializedName;

public class TopCustomer {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("user_email")
    private String userEmail;
    @SerializedName("user_phone")
    private String userPhone;
    @SerializedName("total_spent")
    private double totalSpent;
    @SerializedName("order_count")
    private int orderCount;

    public TopCustomer() {
    }

    public TopCustomer(String userId, String userName, String userEmail, String userPhone, double totalSpent, int orderCount) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.totalSpent = totalSpent;
        this.orderCount = orderCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public String getFormattedTotalSpent() {
        long spentLong = Math.round(totalSpent);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(spentLong).replace(",", ".") + "đ";
    }
}

