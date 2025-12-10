package com.example.duan1.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

public class Product {
    @SerializedName("_id")
    private String id;
    @SerializedName("product_id")
    private String productId;
    private String name;
    private String description;
    private double price;
    private String image;
    
    @SerializedName("category_id")
    @JsonAdapter(CategoryIdDeserializer.class)
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

    // Constructor với productId
    public Product(String id, String productId, String name, String description, double price, String image, String categoryId) {
        this.id = id;
        this.productId = productId;
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

    public String getProductId() {
        return productId != null ? productId : id;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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
        // Format: 250.000đ, 1.000.000đ
        // Làm tròn về số nguyên gần nhất để tránh lỗi làm tròn
        long priceLong = Math.round(price);
        // Format số với dấu chấm phân cách hàng nghìn
        // Sử dụng DecimalFormat để đảm bảo format đúng
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(priceLong).replace(",", ".") + "đ";
    }

    // Custom deserializer để xử lý category_id có thể là string hoặc object
    public static class CategoryIdDeserializer implements JsonDeserializer<String> {
        @Override
        public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            if (json.isJsonPrimitive()) {
                // Nếu là string, trả về trực tiếp
                return json.getAsString();
            } else if (json.isJsonObject()) {
                // Nếu là object (do populate), lấy _id
                JsonObject categoryObj = json.getAsJsonObject();
                if (categoryObj.has("_id")) {
                    return categoryObj.get("_id").getAsString();
                }
            }
            return null;
        }
    }
}

