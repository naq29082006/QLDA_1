package com.example.duan1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.duan1.model.Response;
import com.example.duan1.model.User;
import com.example.duan1.services.ApiServices;
import com.example.duan1.ui_admin.ManchinhAdmin;
import com.example.duan1.ui_user.ManchinhUser;
import com.example.duan1.utils.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class Dangnhap extends AppCompatActivity {
    EditText edtEmail,edtPass;
    Button btnLogin;
    TextView tvDangki, tvForgotPassword;
    private ApiServices api;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap);

        api = RetrofitClient.getInstance().getApiServices();
        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        btnLogin = findViewById(R.id.btnLogin);
        tvDangki = findViewById(R.id.tvReg);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        
        tvDangki.setOnClickListener(v -> {
            Intent intent = new Intent(Dangnhap.this, Dangki.class);
            startActivityForResult(intent, 1);
        });
        
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        // Load thông tin nếu có
        loadUserInfo();

        btnLogin.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String password = edtPass.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);

            api.login(body).enqueue(new Callback<Response<User>>() {
                @Override
                public void onResponse(Call<Response<User>> call, retrofit2.Response<Response<User>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Response<User> res = response.body();

                        if (res.getData() != null) {
                            User user = res.getData();

                            // Debug: Log thông tin user
                            Log.d("Login", "User ID: " + user.getId());
                            Log.d("Login", "User Email: " + user.getEmail());
                            Log.d("Login", "User Name: " + user.getName());

                            // Lưu token + user info
                            SharedPreferences sharedPref = getSharedPreferences("UserData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("token", res.getToken());
                            editor.putString("refreshToken", res.getRefreshToken());
                            editor.putString("email", user.getEmail());
                            editor.putString("password", user.getPass()); // Lưu mật khẩu
                            editor.putString("phone", user.getPhone());
                            editor.putString("name", user.getName());
                            
                            // Lưu user ID - kiểm tra null
                            String userId = user.getId();
                            if (userId != null && !userId.isEmpty()) {
                                editor.putString("id_taikhoan", userId);
                                Log.d("Login", "Saved user ID: " + userId);
                            } else {
                                Log.e("Login", "User ID is null or empty!");
                            }
                            
                            editor.apply();
                            saveUserInfo(email, password);

                            Toast.makeText(Dangnhap.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                            if (user.getRole().equals("admin")) {
                                Intent intent = new Intent(Dangnhap.this, ManchinhAdmin.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                Intent intent = new Intent(Dangnhap.this, ManchinhUser.class);
                                startActivity(intent);
                                finish();
                            }

                        } else {
                            Toast.makeText(Dangnhap.this, "Đăng nhập thất bại: " + res.getMessenger(), Toast.LENGTH_SHORT).show();
                            Log.e("Lỗi", response.toString());

                        }
                    } else {
                        String errorBody = response.errorBody() != null ? response.errorBody().toString() : "Không có thông tin lỗi";
                        Log.e("Lỗi", errorBody);
                        Toast.makeText(Dangnhap.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Response<User>> call, Throwable t) {
                    Log.e("Lỗi",  t.toString());
                    Toast.makeText(Dangnhap.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });


        });


    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String username = data.getStringExtra("email");
            String password = data.getStringExtra("password");
            edtEmail.setText(username);
            edtPass.setText(password);
        }
    }
    private void saveUserInfo(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }

    private void loadUserInfo() {
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPass = sharedPreferences.getString("password", "");
        edtEmail.setText(savedEmail);
        edtPass.setText(savedPass);
    }

    private void showForgotPasswordDialog() {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_quen_mat_khau);
        
        // Set width cho dialog
        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.getWindow().setAttributes(params);

        android.widget.EditText edtEmail = dialog.findViewById(R.id.edtEmailForgot);
        android.widget.EditText edtCode = dialog.findViewById(R.id.edtCode);
        android.widget.EditText edtNewPassword = dialog.findViewById(R.id.edtNewPassword);
        android.widget.Button btnSendCode = dialog.findViewById(R.id.btnSendCode);
        android.widget.Button btnVerifyCode = dialog.findViewById(R.id.btnVerifyCode);
        android.widget.Button btnResetPassword = dialog.findViewById(R.id.btnResetPassword);
        android.widget.Button btnCancel = dialog.findViewById(R.id.btnCancel);
        android.widget.LinearLayout layoutStep1 = dialog.findViewById(R.id.layoutStep1);
        android.widget.LinearLayout layoutStep2 = dialog.findViewById(R.id.layoutStep2);
        android.widget.LinearLayout layoutStep3 = dialog.findViewById(R.id.layoutStep3);
        android.widget.TextView tvStepInfo = dialog.findViewById(R.id.tvStepInfo);

        // Ẩn các bước ban đầu
        layoutStep2.setVisibility(android.view.View.GONE);
        layoutStep3.setVisibility(android.view.View.GONE);
        tvStepInfo.setText("Bước 1/3: Nhập email của bạn");

        String[] userEmail = {""}; // Lưu email để dùng ở các bước sau

        // Bước 1: Gửi code
        btnSendCode.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> body = new HashMap<>();
            body.put("email", email);

            api.forgotPassword(body).enqueue(new Callback<Response<Map<String, String>>>() {
                @Override
                public void onResponse(Call<Response<Map<String, String>>> call, retrofit2.Response<Response<Map<String, String>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Response<Map<String, String>> res = response.body();
                        if (res.isSuccess()) {
                            userEmail[0] = email;
                            Toast.makeText(Dangnhap.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                            // Chuyển sang bước 2
                            layoutStep1.setVisibility(android.view.View.GONE);
                            layoutStep2.setVisibility(android.view.View.VISIBLE);
                            tvStepInfo.setText("Bước 2/3: Nhập mã xác nhận đã gửi về email");
                        } else {
                            Toast.makeText(Dangnhap.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = "Lỗi khi gửi mã xác nhận";
                        if (response.errorBody() != null) {
                            try {
                                errorMsg = response.errorBody().string();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(Dangnhap.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Response<Map<String, String>>> call, Throwable t) {
                    Toast.makeText(Dangnhap.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Bước 2: Xác nhận code
        btnVerifyCode.setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã xác nhận", Toast.LENGTH_SHORT).show();
                return;
            }

            if (code.length() != 6) {
                Toast.makeText(this, "Mã xác nhận phải có 6 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify code - đảm bảo code là string và trim
            String emailToSend = userEmail[0].trim().toLowerCase();
            String codeToSend = code.trim();
            
            Log.d("ForgotPassword", "Verifying code - Email: " + emailToSend + ", Code: " + codeToSend + ", Code length: " + codeToSend.length());
            
            Map<String, String> body = new HashMap<>();
            body.put("email", emailToSend);
            body.put("code", codeToSend);

            api.verifyResetCode(body).enqueue(new Callback<Response<Void>>() {
                @Override
                public void onResponse(Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                    Log.d("ForgotPassword", "Response code: " + response.code() + ", Success: " + response.isSuccessful());
                    if (response.isSuccessful() && response.body() != null) {
                        Response<Void> res = response.body();
                        Log.d("ForgotPassword", "Response success: " + res.isSuccess() + ", Message: " + res.getMessage());
                        if (res.isSuccess()) {
                            // Code đúng, chuyển sang bước 3
                            layoutStep2.setVisibility(android.view.View.GONE);
                            layoutStep3.setVisibility(android.view.View.VISIBLE);
                            tvStepInfo.setText("Bước 3/3: Nhập mật khẩu mới");
                        } else {
                            Toast.makeText(Dangnhap.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = "Mã xác nhận không đúng";
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("ForgotPassword", "Error body: " + errorBody);
                                if (errorBody.contains("không đúng") || errorBody.contains("không hợp lệ")) {
                                    errorMsg = "Mã xác nhận không đúng";
                                } else if (errorBody.contains("hết hạn")) {
                                    errorMsg = "Mã xác nhận đã hết hạn";
                                }
                            } catch (Exception e) {
                                Log.e("ForgotPassword", "Error reading error body", e);
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(Dangnhap.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Response<Void>> call, Throwable t) {
                    Toast.makeText(Dangnhap.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Bước 3: Đặt lại mật khẩu
        btnResetPassword.setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();

            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, String> body = new HashMap<>();
            body.put("email", userEmail[0]);
            body.put("code", code);
            body.put("newPassword", newPassword);

            api.resetPassword(body).enqueue(new Callback<Response<Void>>() {
                @Override
                public void onResponse(Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Response<Void> res = response.body();
                        if (res.isSuccess()) {
                            Toast.makeText(Dangnhap.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            
                            // Set email và password mới vào form đăng nhập
                            Dangnhap.this.edtEmail.setText(userEmail[0]);
                            Dangnhap.this.edtPass.setText(newPassword);
                            
                            // Lưu vào SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email", userEmail[0]);
                            editor.putString("password", newPassword);
                            editor.apply();
                        } else {
                            Toast.makeText(Dangnhap.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errorMsg = "Lỗi khi đặt lại mật khẩu";
                        if (response.errorBody() != null) {
                            try {
                                errorMsg = response.errorBody().string();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(Dangnhap.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Response<Void>> call, Throwable t) {
                    Toast.makeText(Dangnhap.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}