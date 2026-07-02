package com.velvasoftware.pixelrootapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.velvasoftware.pixelrootapp.databinding.ActivityForgotPasswordBinding;
import com.velvasoftware.pixelrootapp.network.api.AuthApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.ForgotPasswordRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();
    }

    private void setupListeners() {
        binding.btnSendCode.setOnClickListener(v -> attemptSendCode());
        binding.backToLoginText.setOnClickListener(v -> finish());
    }

    private void attemptSendCode() {
        String correo = binding.emailEditText.getText().toString().trim();

        if (correo.isEmpty()) {
            Toast.makeText(this, "Ingresa tu correo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSendCode.setEnabled(false);

        AuthApi authApi = RetrofitClient.getAuthApi();
        authApi.forgotPassword(new ForgotPasswordRequest(correo)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                binding.btnSendCode.setEnabled(true);
                ApiResponse<Void> body = response.body();

                if (response.isSuccessful() && body != null && body.isStatus()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Te enviamos un código a tu correo", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ForgotPasswordActivity.this, EmailVerificationActivity.class);
                    intent.putExtra("correo", correo);
                    intent.putExtra("flujo", "recuperar_contrasena");
                    startActivity(intent);
                } else {
                    String message = body != null ? body.getMessage() : "No se pudo enviar el código";
                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                binding.btnSendCode.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}