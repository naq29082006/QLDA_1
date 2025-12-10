package com.example.duan1.model;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("_id")
    private String id;
    @SerializedName("cart_item_id")
    private String cartItemId;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("product_id")
    private String productId;
    private int quantity;
    private double subtotal;
    @SerializedName("add_at")
    private String addAt;

    // Product object khi được populate từ server (không có @SerializedName để tránh conflict)
    private transient Product product;

    public CartItem() {
    }

    public CartItem(String id, String cartItemId, String userId, String productId, int quantity, double subtotal, String addAt) {
        this.id = id;
        this.cartItemId = cartItemId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.subtotal = subtotal;
        this.addAt = addAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCartItemId() {
        return cartItemId != null ? cartItemId : id;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public String getAddAt() {
        return addAt;
    }

    public void setAddAt(String addAt) {
        this.addAt = addAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getFormattedSubtotal() {
        // Làm tròn về số nguyên gần nhất
        long subtotalLong = Math.round(subtotal);
        // Format số với dấu chấm phân cách hàng nghìn
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(subtotalLong).replace(",", ".") + "đ";
    }

    // Tính lại subtotal dựa trên giá sản phẩm và số lượng
    public void calculateSubtotal() {
        if (product != null) {
            this.subtotal = product.getPrice() * quantity;
        }
    }
}

