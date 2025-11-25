package com.example.duan1.model;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("_id")
    private String id;
    private String name;
    private String description;
    private double price;
    private String image;
    @SerializedName("category_id")
    private String categoryId;
    private int quantity; // Số lượng bán (cho top products)
    private int colorResId; // Màu cho icon vuông

    public Product() {
    }

    public Product(String id, String name, String description, double price, String image, String categoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
        this.categoryId = categoryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public int getColorResId() {
        return colorResId;
    }

    public void setColorResId(int colorResId) {
        this.colorResId = colorResId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getFormattedPrice() {
        // Format: 250.000đ
        long priceLong = (long) price;
        if (priceLong >= 1000) {
            // Chia cho 1000 để lấy phần nghìn, rồi thêm ".000đ"
            long thousands = priceLong / 1000;
            return String.format("%,d.000đ", thousands).replace(",", ".");
        } else {
            return String.format("%d.000đ", priceLong);
        }
    }
}

