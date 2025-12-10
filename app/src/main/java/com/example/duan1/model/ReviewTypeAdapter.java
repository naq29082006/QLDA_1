package com.example.duan1.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ReviewTypeAdapter implements JsonDeserializer<Review> {
    @Override
    public Review deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Review review = new Review();

        // Parse _id
        if (jsonObject.has("_id")) {
            JsonElement idElement = jsonObject.get("_id");
            if (idElement.isJsonObject()) {
                review.setId(idElement.getAsJsonObject().get("$oid").getAsString());
            } else {
                review.setId(idElement.getAsString());
            }
        }

        // Parse order_id (có thể là string hoặc object)
        // Lưu cả order_id (mã đơn như DA1) và orderObjectId (_id của order) để dùng sau
        if (jsonObject.has("order_id")) {
            JsonElement orderIdElement = jsonObject.get("order_id");
            if (orderIdElement.isJsonObject()) {
                JsonObject orderObj = orderIdElement.getAsJsonObject();
                // Nếu là object (populated), lấy order_id field (mã đơn như DA1, DA2)
                if (orderObj.has("order_id")) {
                    review.setOrderId(orderObj.get("order_id").getAsString());
                }
                // Lưu _id của order để query order details
                if (orderObj.has("_id")) {
                    JsonElement orderId = orderObj.get("_id");
                    if (orderId.isJsonObject()) {
                        review.setOrderObjectId(orderId.getAsJsonObject().get("$oid").getAsString());
                    } else {
                        review.setOrderObjectId(orderId.getAsString());
                    }
                }
                // Nếu không có order_id trong object, dùng _id làm fallback
                if (review.getOrderId() == null && orderObj.has("_id")) {
                    JsonElement orderId = orderObj.get("_id");
                    if (orderId.isJsonObject()) {
                        review.setOrderId(orderId.getAsJsonObject().get("$oid").getAsString());
                    } else {
                        review.setOrderId(orderId.getAsString());
                    }
                }
            } else {
                // Nếu là string, đó là _id của order
                String orderIdStr = orderIdElement.getAsString();
                review.setOrderId(orderIdStr);
                review.setOrderObjectId(orderIdStr);
            }
        }

        // Parse user_id (có thể là string hoặc object)
        if (jsonObject.has("user_id")) {
            JsonElement userIdElement = jsonObject.get("user_id");
            if (userIdElement.isJsonObject()) {
                JsonObject userObj = userIdElement.getAsJsonObject();
                // Nếu là object (populated), lấy thông tin user
                if (userObj.has("_id")) {
                    JsonElement userId = userObj.get("_id");
                    if (userId.isJsonObject()) {
                        review.setUserId(userId.getAsJsonObject().get("$oid").getAsString());
                    } else {
                        review.setUserId(userId.getAsString());
                    }
                }
                // Lấy thông tin user từ populated data
                if (userObj.has("name")) {
                    review.setUserName(userObj.get("name").getAsString());
                }
                if (userObj.has("email")) {
                    review.setUserEmail(userObj.get("email").getAsString());
                }
                if (userObj.has("phone")) {
                    review.setUserPhone(userObj.get("phone").getAsString());
                }
            } else {
                review.setUserId(userIdElement.getAsString());
            }
        }

        // Parse rating
        if (jsonObject.has("rating")) {
            review.setRating(jsonObject.get("rating").getAsInt());
        }

        // Parse comment
        if (jsonObject.has("comment")) {
            JsonElement commentElement = jsonObject.get("comment");
            review.setComment(commentElement.isJsonNull() ? null : commentElement.getAsString());
        }

        // Parse created_at
        if (jsonObject.has("created_at")) {
            JsonElement createdAtElement = jsonObject.get("created_at");
            review.setCreatedAt(createdAtElement.isJsonNull() ? null : createdAtElement.getAsString());
        }

        // Parse updated_at
        if (jsonObject.has("updated_at")) {
            JsonElement updatedAtElement = jsonObject.get("updated_at");
            review.setUpdatedAt(updatedAtElement.isJsonNull() ? null : updatedAtElement.getAsString());
        }

        return review;
    }
}

