package com.example.duan1.model;

import com.google.gson.annotations.SerializedName;

public class OrderDetail {
    @SerializedName("_id")
    private String id;
    @SerializedName("order_detail_id")
    private String orderDetailId;
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("product_id")
    private String productId;
    private int quantity;
    private double price;
    private double subtotal;

    // Các trường để hiển thị (không lưu trong DB)
    private Product product;

    public OrderDetail() {
    }

    public OrderDetail(String id, String orderDetailId, String orderId, String productId, int quantity, double price, double subtotal) {
        this.id = id;
        this.orderDetailId = orderDetailId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderDetailId() {
        return orderDetailId != null ? orderDetailId : id;
    }

    public void setOrderDetailId(String orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getFormattedPrice() {
        // Làm tròn về số nguyên gần nhất
        long priceLong = Math.round(price);
        // Format số với dấu chấm phân cách hàng nghìn
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(priceLong).replace(",", ".") + "đ";
    }

    public String getFormattedSubtotal() {
        // Làm tròn về số nguyên gần nhất
        long subtotalLong = Math.round(subtotal);
        // Format số với dấu chấm phân cách hàng nghìn
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        return df.format(subtotalLong).replace(",", ".") + "đ";
    }
}

