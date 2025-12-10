package com.example.duan1.services;

import com.example.duan1.model.CartItem;
import com.example.duan1.model.DanhMuc;
import com.example.duan1.model.Order;
import com.example.duan1.model.OrderDetail;
import com.example.duan1.model.Product;
import com.example.duan1.model.Response;
import com.example.duan1.model.Review;
import com.example.duan1.model.TopCustomer;
import com.example.duan1.model.TopProduct;
import com.example.duan1.model.TopRevenue;
import com.example.duan1.model.User;
import com.example.duan1.model.Voucher;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiServices {
    String Url = "http://10.24.55.27:3000/";
    
    // ==================== AUTH APIs ====================
    @POST("api/register")
    Call<Response<User>> register(@Body Map<String, String> body);
    
    @POST("api/login")
    Call<Response<User>> login(@Body Map<String, String> body);
    
    @POST("api/forgot-password")
    Call<Response<Map<String, String>>> forgotPassword(@Body Map<String, String> body);
    
    @POST("api/verify-reset-code")
    Call<Response<Void>> verifyResetCode(@Body Map<String, String> body);
    
    @POST("api/reset-password")
    Call<Response<Void>> resetPassword(@Body Map<String, String> body);

    // ==================== CATEGORIES APIs ====================
    @GET("api/get-all-categories")
    Call<Response<List<DanhMuc>>> getAllCategories();
    
    @GET("api/get-id-categories/{id}")
    Call<Response<DanhMuc>> getCategoryById(@Path("id") String id);
    
    @POST("api/create-categories")
    Call<Response<DanhMuc>> createCategory(@Body Map<String, String> body);
    
    @PUT("api/update-id-categories/{id}")
    Call<Response<DanhMuc>> updateCategory(@Path("id") String id, @Body Map<String, String> body);
    
    @DELETE("api/delete-id-categories/{id}")
    Call<Response<Void>> deleteCategory(@Path("id") String id);

    // ==================== PRODUCTS APIs ====================
    @GET("api/get-all-products")
    Call<Response<List<Product>>> getAllProducts();
    
    @GET("api/get-id-products/{id}")
    Call<Response<Product>> getProductById(@Path("id") String id);
    
    @Multipart
    @POST("api/create-products")
    Call<Response<Product>> createProduct(
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("price") RequestBody price,
            @Part("category_id") RequestBody categoryId,
            @Part MultipartBody.Part image
    );
    
    @Multipart
    @PUT("api/update-products/{id}")
    Call<Response<Product>> updateProduct(
            @Path("id") String id,
            @Part("name") RequestBody name,
            @Part("description") RequestBody description,
            @Part("price") RequestBody price,
            @Part("category_id") RequestBody categoryId,
            @Part MultipartBody.Part image
    );
    
    @DELETE("api/delete-products/{id}")
    Call<Response<Void>> deleteProduct(@Path("id") String id);
    
    @GET("api/get-products-search/{keyword}")
    Call<Response<List<Product>>> searchProducts(@Path("keyword") String keyword);
    
    @GET("api/get-products-by-category/{categoryId}")
    Call<Response<List<Product>>> getProductsByCategory(@Path("categoryId") String categoryId);

    // ==================== USERS APIs ====================
    @GET("api/get-all-users")
    Call<Response<List<User>>> getAllUsers();
    
    @GET("api/get-id-users/{id}")
    Call<Response<User>> getUserById(@Path("id") String id);
    
    @PUT("api/update-users/{id}")
    Call<Response<User>> updateUser(@Path("id") String id, @Body Map<String, String> body);
    
    @PUT("api/change-password/{id}")
    Call<Response<Void>> changePassword(@Path("id") String id, @Body Map<String, String> body);
    
    @DELETE("api/delete-users/{id}")
    Call<Response<Void>> deleteUser(@Path("id") String id);

    // ==================== CART APIs ====================
    @GET("api/get-cart/{userId}")
    Call<Response<List<CartItem>>> getCart(@Path("userId") String userId);
    
    @POST("api/add-to-cart")
    Call<Response<CartItem>> addToCart(@Body Map<String, String> body);
    
    @PUT("api/update-cart/{id}")
    Call<Response<CartItem>> updateCart(@Path("id") String id, @Body Map<String, String> body);
    
    @DELETE("api/delete-cart/{id}")
    Call<Response<Void>> deleteCart(@Path("id") String id);

    // ==================== ORDERS APIs ====================
    @POST("api/create-order")
    Call<Response<Order>> createOrder(@Body Map<String, Object> body);
    
    @GET("api/get-orders/{userId}")
    Call<Response<List<Order>>> getOrders(@Path("userId") String userId);
    
    @GET("api/get-all-orders")
    Call<Response<List<Order>>> getAllOrders();
    
    @PUT("api/update-order-status/{id}")
    Call<Response<Order>> updateOrderStatus(@Path("id") String id, @Body Map<String, String> body);
    
    @GET("api/get-order-by-id/{id}")
    Call<Response<Order>> getOrderById(@Path("id") String id);
    
    @GET("api/get-order-details/{orderId}")
    Call<Response<List<OrderDetail>>> getOrderDetails(@Path("orderId") String orderId);
    
    @PUT("api/cancel-order/{id}")
    Call<Response<Order>> cancelOrder(@Path("id") String id, @Body Map<String, String> body);

    // ==================== REVIEWS APIs ====================
    @POST("api/create-review")
    Call<Response<Review>> createReview(@Body Map<String, String> body);
    
    @GET("api/get-all-reviews")
    Call<Response<List<Review>>> getAllReviews();
    
    @GET("api/get-review-by-order/{orderId}")
    Call<Response<Review>> getReviewByOrder(@Path("orderId") String orderId);

    // ==================== VOUCHER APIs ====================
    @GET("api/get-all-vouchers")
    Call<Response<List<Voucher>>> getAllVouchers();
    
    @GET("api/get-voucher-by-id/{id}")
    Call<Response<Voucher>> getVoucherById(@Path("id") String id);
    
    @POST("api/create-voucher")
    Call<Response<Voucher>> createVoucher(@Body Map<String, Object> body);
    
    @PUT("api/update-voucher/{id}")
    Call<Response<Voucher>> updateVoucher(@Path("id") String id, @Body Map<String, Object> body);
    
    @DELETE("api/delete-voucher/{id}")
    Call<Response<Void>> deleteVoucher(@Path("id") String id);
    
    @POST("api/validate-voucher")
    Call<Response<Map<String, Object>>> validateVoucher(@Body Map<String, Object> body);

    // ==================== REPORTS APIs ====================
    @GET("api/reports/top-revenue")
    Call<Response<List<TopRevenue>>> getTopRevenue(
            @Query("limit") int limit,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );
    
    @GET("api/reports/top-customers")
    Call<Response<List<TopCustomer>>> getTopCustomers(
            @Query("limit") int limit,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );
    
    @GET("api/reports/top-products")
    Call<Response<List<TopProduct>>> getTopProducts(
            @Query("limit") int limit,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

}
