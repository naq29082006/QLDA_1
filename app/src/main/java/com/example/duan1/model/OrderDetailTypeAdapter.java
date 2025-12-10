package com.example.duan1.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class OrderDetailTypeAdapter implements JsonDeserializer<OrderDetail> {
    @Override
    public OrderDetail deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        OrderDetail orderDetail = new OrderDetail();

        // Deserialize các trường cơ bản
        if (jsonObject.has("_id")) {
            orderDetail.setId(jsonObject.get("_id").getAsString());
        }
        if (jsonObject.has("order_detail_id")) {
            orderDetail.setOrderDetailId(jsonObject.get("order_detail_id").getAsString());
        }
        if (jsonObject.has("order_id")) {
            JsonElement orderIdElement = jsonObject.get("order_id");
            if (orderIdElement.isJsonPrimitive()) {
                orderDetail.setOrderId(orderIdElement.getAsString());
            } else if (orderIdElement.isJsonObject()) {
                orderDetail.setOrderId(orderIdElement.getAsJsonObject().get("_id").getAsString());
            }
        }
        if (jsonObject.has("quantity")) {
            orderDetail.setQuantity(jsonObject.get("quantity").getAsInt());
        }
        if (jsonObject.has("price")) {
            orderDetail.setPrice(jsonObject.get("price").getAsDouble());
        }
        if (jsonObject.has("subtotal")) {
            orderDetail.setSubtotal(jsonObject.get("subtotal").getAsDouble());
        }

        // Xử lý product_id có thể là string hoặc object (do populate)
        if (jsonObject.has("product_id")) {
            JsonElement productIdElement = jsonObject.get("product_id");
            if (productIdElement.isJsonPrimitive()) {
                // Nếu là string
                orderDetail.setProductId(productIdElement.getAsString());
            } else if (productIdElement.isJsonObject()) {
                // Nếu là object (do populate), deserialize thành Product và lấy _id
                JsonObject productObj = productIdElement.getAsJsonObject();
                Product product = context.deserialize(productObj, Product.class);
                orderDetail.setProduct(product);
                if (productObj.has("_id")) {
                    orderDetail.setProductId(productObj.get("_id").getAsString());
                } else if (product != null && product.getId() != null) {
                    orderDetail.setProductId(product.getId());
                }
            }
        }

        return orderDetail;
    }
}

