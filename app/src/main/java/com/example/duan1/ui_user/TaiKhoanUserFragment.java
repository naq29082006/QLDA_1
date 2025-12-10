package com.example.duan1.ui_user;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.duan1.Dangnhap;
import com.example.duan1.R;
import com.example.duan1.model.Response;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class TaiKhoanUserFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone;
    private Button btnLogout, btnChangePassword;
    private SharedPreferences sharedPreferences;
    private ApiServices apiServices;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tai_khoan_user, container, false);

        // Khởi tạo Retrofit
        apiServices = RetrofitClient.getInstance().getApiServices();

        // Ánh xạ views
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvPhone = view.findViewById(R.id.tvPhone);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);

        // SharedPreferences
        sharedPreferences = getActivity().getSharedPreferences("UserData", getActivity().MODE_PRIVATE);

        // Load thông tin user
        loadUserInfo();

        // Xử lý đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Xử lý logout
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(getActivity(), Dangnhap.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    private void loadUserInfo() {
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String phone = sharedPreferences.getString("phone", "");

        tvName.setText("Tên: " + name);
        tvEmail.setText("Email: " + email);
        tvPhone.setText("SĐT: " + phone);
    }

    private void showChangePasswordDialog() {
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_doi_mat_khau);
        
        // Set width cho dialog
        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
        dialog.getWindow().setAttributes(params);

        EditText edtOldPassword = dialog.findViewById(R.id.edtOldPassword);
        EditText edtNewPassword = dialog.findViewById(R.id.edtNewPassword);
        EditText edtConfirmPassword = dialog.findViewById(R.id.edtConfirmPassword);
        Button btnChange = dialog.findViewById(R.id.btnChange);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnChange.setOnClickListener(v -> {
            String oldPassword = edtOldPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getActivity(), "Mật khẩu mới và xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(getActivity(), "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(oldPassword, newPassword, dialog);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void changePassword(String oldPassword, String newPassword, Dialog dialog) {
        String userId = sharedPreferences.getString("id_taikhoan", "");
        if (userId.isEmpty()) {
            Toast.makeText(getActivity(), "Không tìm thấy thông tin user", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", oldPassword);
        body.put("newPassword", newPassword);

        apiServices.changePassword(userId, body).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<Void> res = response.body();
                    if (res.isSuccess()) {
                        Toast.makeText(getActivity(), res.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        
                        // Cập nhật mật khẩu trong SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("password", newPassword);
                        editor.apply();
                    } else {
                        Toast.makeText(getActivity(), res.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMsg = "Lỗi khi đổi mật khẩu";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e("API Error", "Response code: " + response.code() + ", Message: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                Toast.makeText(getActivity(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("API Error", "Failure: " + t.getMessage(), t);
            }
        });
    }
}

