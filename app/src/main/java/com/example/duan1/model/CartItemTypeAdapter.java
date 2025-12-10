package com.example.duan1.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class CartItemTypeAdapter implements JsonDeserializer<CartItem> {
    @Override
    public CartItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        CartItem cartItem = new CartItem();

        // Parse các field cơ bản
        if (jsonObject.has("_id")) {
            cartItem.setId(jsonObject.get("_id").getAsString());
        }
        if (jsonObject.has("cart_item_id")) {
            cartItem.setCartItemId(jsonObject.get("cart_item_id").getAsString());
        }
        if (jsonObject.has("user_id")) {
            JsonElement userIdElement = jsonObject.get("user_id");
            if (userIdElement.isJsonPrimitive()) {
                cartItem.setUserId(userIdElement.getAsString());
            } else if (userIdElement.isJsonObject()) {
                cartItem.setUserId(userIdElement.getAsJsonObject().get("_id").getAsString());
            }
        }
        if (jsonObject.has("quantity")) {
            cartItem.setQuantity(jsonObject.get("quantity").getAsInt());
        }
        if (jsonObject.has("subtotal")) {
            cartItem.setSubtotal(jsonObject.get("subtotal").getAsDouble());
        }
        if (jsonObject.has("add_at")) {
            cartItem.setAddAt(jsonObject.get("add_at").getAsString());
        }

        // Xử lý product_id - có thể là string hoặc object (khi populate)
        if (jsonObject.has("product_id")) {
            JsonElement productIdElement = jsonObject.get("product_id");
            if (productIdElement.isJsonPrimitive()) {
                // Nếu là string
                cartItem.setProductId(productIdElement.getAsString());
            } else if (productIdElement.isJsonObject()) {
                // Nếu là object (do populate), parse thành Product
                JsonObject productObj = productIdElement.getAsJsonObject();
                Product product = context.deserialize(productObj, Product.class);
                cartItem.setProduct(product);
                // Lấy _id làm productId
                if (productObj.has("_id")) {
                    cartItem.setProductId(productObj.get("_id").getAsString());
                } else if (product != null && product.getId() != null) {
                    cartItem.setProductId(product.getId());
                }
            }
        }

        return cartItem;
    }
}

