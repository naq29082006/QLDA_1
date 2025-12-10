package com.example.duan1.model;

import com.google.gson.annotations.SerializedName;

public class TopRevenue {
    @SerializedName("date")
    private String date;
    @SerializedName("total_revenue")
    private double totalRevenue;
    @SerializedName("order_count")
    private int orderCount;

    public TopRevenue() {
    }

    public TopRevenue(String date, double totalRevenue, int orderCount) {
        this.date = date;
        this.totalRevenue = totalRevenue;
        this.orderCount = orderCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public String getFormattedRevenue() {
        long revenueLong = Math.round(totalRevenue);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(revenueLong).replace(",", ".") + "đ";
    }
}

