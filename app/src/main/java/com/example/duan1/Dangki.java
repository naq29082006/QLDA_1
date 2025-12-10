package com.example.duan1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.duan1.model.Response;
import com.example.duan1.model.User;
import com.example.duan1.services.ApiServices;
import com.example.duan1.utils.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class Dangki extends AppCompatActivity {
    EditText edtEmail,edtPass,edtPhone,edtName;
    Button btnDangki;
    TextView tvDangnhap;
    private ApiServices api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangki);
        edtEmail = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPass);
        edtPhone = findViewById(R.id.edtPhone);
        edtName = findViewById(R.id.edtName);
        btnDangki = findViewById(R.id.btnDangki);
        tvDangnhap = findViewById(R.id.tvLogin);

        api = RetrofitClient.getInstance().getApiServices();

        tvDangnhap.setOnClickListener(v -> {
            Intent intent = new Intent(Dangki.this, Dangnhap.class);
            startActivity(intent);
            finish();
        });
        btnDangki.setOnClickListener(v -> {
            Map<String, String> body = new HashMap<>();
            body.put("email", edtEmail.getText().toString().trim());
            body.put("password", edtPass.getText().toString().trim());
            body.put("name", edtName.getText().toString().trim());
            body.put("phone", edtPhone.getText().toString().trim());
            api.register(body)
                    .enqueue(new Callback<Response<User>>() {
                        @Override
                        public void onResponse(Call<Response<User>> call, retrofit2.Response<Response<User>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Toast.makeText(Dangki.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("email", edtEmail.getText().toString());
                                resultIntent.putExtra("password", edtPass.getText().toString());
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                Toast.makeText(Dangki.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                                Log.e("Lỗi", response.toString());
                            }
                        }

                        @Override
                        public void onFailure(Call<Response<User>> call, Throwable t) {
                            Toast.makeText(Dangki.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("Lỗi", "onFailure", t);
                        }
                    });
        });

    }
}