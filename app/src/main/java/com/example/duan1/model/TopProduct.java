package com.example.duan1.model;

import com.google.gson.annotations.SerializedName;

public class TopProduct {
    @SerializedName("product_id")
    private String productId;
    @SerializedName("product_name")
    private String productName;
    @SerializedName("product_image")
    private String productImage;
    @SerializedName("product_price")
    private double productPrice;
    @SerializedName("total_quantity")
    private int totalQuantity;
    @SerializedName("total_revenue")
    private double totalRevenue;

    public TopProduct() {
    }

    public TopProduct(String productId, String productName, String productImage, double productPrice, int totalQuantity, double totalRevenue) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.productPrice = productPrice;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public String getFormattedPrice() {
        long priceLong = Math.round(productPrice);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(priceLong).replace(",", ".") + "đ";
    }

    public String getFormattedRevenue() {
        long revenueLong = Math.round(totalRevenue);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(revenueLong).replace(",", ".") + "đ";
    }
}

