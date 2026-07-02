package com.velvasoftware.pixelrootapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.velvasoftware.pixelrootapp.databinding.ActivityEmailVerificationBinding;
import com.velvasoftware.pixelrootapp.network.api.AuthApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.ForgotPasswordRequest;
import com.velvasoftware.pixelrootapp.network.request.VerifyResetCodeRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmailVerificationActivity extends AppCompatActivity {

    private ActivityEmailVerificationBinding binding;
    private String correo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        correo = getIntent().getStringExtra("correo");

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();
    }

    private void setupListeners() {
        binding.btnVerify.setOnClickListener(v -> attemptVerify());
        binding.resendText.setOnClickListener(v -> resendCode());
    }

    private void attemptVerify() {
        if (correo == null || correo.isEmpty()) {
            Toast.makeText(this, "Falta el correo, vuelve a intentarlo desde el inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        String codigo = binding.codeEditText.getText().toString().trim();
        if (codigo.isEmpty()) {
            Toast.makeText(this, "Ingresa el código que te enviamos", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnVerify.setEnabled(false);

        AuthApi authApi = RetrofitClient.getAuthApi();
        authApi.verifyResetCode(new VerifyResetCodeRequest(correo, codigo)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                binding.btnVerify.setEnabled(true);
                ApiResponse<Void> body = response.body();

                if (response.isSuccessful() && body != null && body.isStatus()) {
                    Intent intent = new Intent(EmailVerificationActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("correo", correo);
                    intent.putExtra("codigo", codigo);
                    startActivity(intent);
                    finish();
                } else {
                    String message = body != null ? body.getMessage() : "Código inválido o expirado";
                    Toast.makeText(EmailVerificationActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                binding.btnVerify.setEnabled(true);
                Toast.makeText(EmailVerificationActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendCode() {
        if (correo == null || correo.isEmpty()) return;

        AuthApi authApi = RetrofitClient.getAuthApi();
        authApi.forgotPassword(new ForgotPasswordRequest(correo)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                ApiResponse<Void> body = response.body();
                String message = (body != null) ? body.getMessage() : "No se pudo reenviar el código";
                Toast.makeText(EmailVerificationActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                Toast.makeText(EmailVerificationActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}